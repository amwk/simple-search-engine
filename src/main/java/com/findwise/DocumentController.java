package com.findwise;

import com.hazelcast.core.HazelcastInstance;
import io.swagger.annotations.Api;
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

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<IndexEntry> search(@RequestParam String searchTerm) {
        return searchEngine.search(searchTerm);
    }

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

    @GetMapping(value = "/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Map<String, String> getAllDocuments() {
        return hazelcastInstance.getMap(DOCUMENTS);
    }

    @PostMapping(value = "/document")
    public @ResponseBody
    String addDocument(@RequestBody String documentContent) {
        Map<String, String> documentsMap = hazelcastInstance.getMap(DOCUMENTS);
        String documentId = String.join("", "document", Long.toString(counter.getAndIncrement()));

        documentsMap.put(documentId, documentContent);
        searchEngine.indexDocument(documentId,documentContent);

        return "Document has been added!";
    }

    @GetMapping(value = "/document/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    String getDocument(@RequestParam String id) {
        Map<String, String> documentsMap = hazelcastInstance.getMap(DOCUMENTS);
        return documentsMap.get(id);
    }

    @PutMapping(value = "/document/{id}")
    public @ResponseBody
    String updateDocument(@RequestParam String id, @RequestBody String body) {
        Map<String, String> documentsMap = hazelcastInstance.getMap(DOCUMENTS);
        documentsMap.replace(id, body);
        return "Document has been updated!";
    }

    @DeleteMapping(value = "/document/{id}")
    public @ResponseBody
    String deleteDocument(@RequestParam String id) {
        Map<String, String> documentsMap = hazelcastInstance.getMap(DOCUMENTS);
        documentsMap.remove(id);
        return "Document has been deleted!";
    }
}