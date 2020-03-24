package com.findwise;

import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchEngineImpl implements SearchEngine {

    public static final String INDEX = "index";
    public static final String DOCUMENTS = "documents";
    private final HazelcastInstance hazelcastInstance;

    private static final Logger log = LoggerFactory.getLogger(SearchEngineImpl.class);

    @Autowired
    public SearchEngineImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void indexDocument(String documentId, String content) {
        List<String> tokenizedContent = Arrays.asList(content.trim().split("\\s+"));

        tokenizedContent.stream().map(String::toLowerCase).forEach(term -> {
            Map<String, Set<IndexEntryImpl>> indexMap = hazelcastInstance.getMap(INDEX);

            addNewIndexEntryForTerm(documentId, term, indexMap);
            log.info("IndexEntry with id {} for document {} has been created", term, documentId);

            Map<String, Set<IndexEntryImpl>> reevaluatedIndex = reevaluateIndex(term, indexMap);

            indexMap.putAll(reevaluatedIndex);
        });
    }

    private Map<String, Set<IndexEntryImpl>> reevaluateIndex(String term, Map<String, Set<IndexEntryImpl>> indexMap) {
        return indexMap.entrySet().stream()
                        .filter(indexEntry ->indexEntry.getKey().equals(term))
                        .map(indexEntry->indexEntry.getValue().stream()
                                .map(entry->new IndexEntryImpl(entry.getId(),calculateScore(entry.getId(),term)))
                                .peek(entry->log.info("IndexEntry for term {} with document id {} has been updated with score {}", term, entry.getId(), entry.getScore()))
                                .collect(Collectors.toSet()))
                        .collect(Collectors.toMap(indexEntrySet-> term, indexEntrySet->(Set<IndexEntryImpl>)indexEntrySet));
    }

    private void addNewIndexEntryForTerm(String documentId, String term, Map<String, Set<IndexEntryImpl>> indexMap) {
        Set<IndexEntryImpl> indexEntries = indexMap.get(term);
        if (indexEntries == null) {
            Set<IndexEntryImpl> entries = Set.of(new IndexEntryImpl(documentId, 0));
            indexMap.put(term, entries);
        }else{
            indexEntries.add(new IndexEntryImpl(documentId, 0));
            indexMap.put(term, indexEntries);
        }
    }

    private double calculateScore(String documentId,String term){
        double tf = tf(documentId, term);
        double idf = idf(term);
        return tf * idf;
    }

    private double idf(String term) {
        Map<String, String> documentMap = hazelcastInstance.getMap(DOCUMENTS);

        long allWordsCount = documentMap.values().stream()
                .map(tokens -> tokens.trim().split("\\s+"))
                .flatMap(Stream::of)
                .count();

        long termCount = documentMap.values().stream()
                .map(tokens -> tokens.trim().split("\\s+"))
                .flatMap(Stream::of)
                .filter(token -> token.equals(term))
                .count();

        return Math.log((double)allWordsCount / (double)termCount);
    }

    private double tf(String documentId, String term) {
        Map<String, String> documentMap = hazelcastInstance.getMap(DOCUMENTS);

        String [] document = documentMap.get(documentId).trim().split("\\s+");
        double result = 0;
        for (String word : document) {
            if (term.equals(word))
                result++;
        }
        return result / document.length;
    }

    @Override
    public List<IndexEntry> search(String term) {
        Map<String, Set<IndexEntry>> indexMap = hazelcastInstance.getMap(INDEX);
        Set<IndexEntry> documentSet = indexMap.get(term);
        List<IndexEntry> orderedDocumentList = new ArrayList<>(documentSet);
        orderedDocumentList.sort(Comparator.comparingDouble(IndexEntry::getScore).reversed());
        return orderedDocumentList;
    }
}
