package com.findwise;

import com.hazelcast.core.HazelcastInstance;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Api(tags = "Document API")
@RestController
@RequestMapping("/api")
public class DocumentController {

    private final HazelcastInstance hazelcastInstance;
    private final SearchEngine searchEngine;
    private final AtomicLong counter = new AtomicLong(1);

    public static final String DOCUMENTS = "documents";

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    public DocumentController(HazelcastInstance hazelcastInstance, SearchEngine searchEngine) {
        this.hazelcastInstance = hazelcastInstance;
        this.searchEngine = searchEngine;
    }

    @ApiOperation("Search for given term")
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<IndexEntry> search(@RequestParam String searchTerm) {
        return searchEngine.search(searchTerm);
    }

    @ApiOperation("Add a list of documents")
    @PostMapping(value = "/documents")
    public @ResponseBody
    Map<String, String> addDocuments(@RequestBody List<String> documentList) {

        Map<String, String> documentsMap = hazelcastInstance.getMap(DOCUMENTS);

        documentList.forEach(documentContent -> {
            String documentId = String.join("", "document", Long.toString(counter.getAndIncrement()));
            log.info("Following document will be added: {} : {}", documentId, documentContent);
            documentsMap.put(documentId, documentContent);
            searchEngine.indexDocument(documentId,documentContent);
        });

        return documentsMap;
    }

    @ApiOperation("Get all of documents")
    @GetMapping(value = "/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Map<String, String> getAllDocuments() {
        return hazelcastInstance.getMap(DOCUMENTS);
    }

    @ApiOperation("Add a single document")
    @PostMapping(value = "/document")
    public @ResponseBody
    String addDocument(@RequestBody String documentContent) {
        Map<String, String> documentsMap = hazelcastInstance.getMap(DOCUMENTS);
        String documentId = String.join("", "document", Long.toString(counter.getAndIncrement()));

        documentsMap.put(documentId, documentContent);
        searchEngine.indexDocument(documentId,documentContent);

        return "Document has been added!";
    }

    @ApiOperation("Get a single document")
    @GetMapping(value = "/document", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    String getDocument(@RequestParam String id) {
        Map<String, String> documentsMap = hazelcastInstance.getMap(DOCUMENTS);
        return documentsMap.get(id);
    }

    @ApiOperation("Update a single document")
    @PutMapping(value = "/document")
    public @ResponseBody
    String updateDocument(@RequestParam String id, @RequestBody String body) {
        Map<String, String> documentsMap = hazelcastInstance.getMap(DOCUMENTS);
        documentsMap.replace(id, body);
        return "Document has been updated!";
    }

    @ApiOperation("Delete a single document")
    @DeleteMapping(value = "/document")
    public @ResponseBody
    String deleteDocument(@RequestParam String id) {
        Map<String, String> documentsMap = hazelcastInstance.getMap(DOCUMENTS);
        documentsMap.remove(id);
        return "Document has been deleted!";
    }

    @ApiOperation("Delete a single document")
    @DeleteMapping(value = "/documents")
    public @ResponseBody
    String deleteDocuments() {
        Map<String, String> documentsMap = hazelcastInstance.getMap(DOCUMENTS);
        documentsMap.clear();
        return "All documents has been deleted!";
    }
}