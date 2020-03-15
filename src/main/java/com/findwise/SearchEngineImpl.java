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

    private final HazelcastInstance hazelcastInstance;

    private static final Logger log = LoggerFactory.getLogger(SearchEngineImpl.class);

    @Autowired
    public SearchEngineImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void indexDocument(String documentId, String content) {
        List<String> tokenizedContent = Arrays.asList(content.split(" "));

        tokenizedContent.stream().map(String::toLowerCase).forEach(term -> {
            Map<String, Set<IndexEntryImpl>> indexMap = hazelcastInstance.getMap("index");

            Set<IndexEntryImpl> indexEntries = indexMap.get(term);
            if (indexEntries == null) {
                Set<IndexEntryImpl> entries = Set.of(new IndexEntryImpl(documentId, 0));
                indexMap.put(term, entries);
            }else{
                indexEntries.add(new IndexEntryImpl(documentId, 0));
                indexMap.put(term, indexEntries);
            }
            log.info("IndexEntry with id {} for document {} has been created", term, documentId);

            Map<String, Set<IndexEntryImpl>> reevaluatedIndex = indexMap.entrySet().stream()
                    .filter(indexEntry ->indexEntry.getKey().equals(term))
                    .map(indexEntry->indexEntry.getValue().stream()
                            .map(entry->new IndexEntryImpl(entry.getId(),calculateScore(entry.getId(),term)))
                            .peek(entry->log.info("IndexEntry for term {} with document id {} has been updated with score {}", term, entry.getId(), entry.getScore()))
                            .collect(Collectors.toSet()))
                    .collect(Collectors.toMap(indexEntrySet->(String) term,indexEntrySet->(Set<IndexEntryImpl>)indexEntrySet));

            indexMap.putAll(reevaluatedIndex);
        });
    }

    private double calculateScore(String documentId,String term){
        double tf = tf(documentId, term);
        double idf = idf(term);
        return tf * idf;
    }

    private double idf(String term) {
        Map<String, String> documentMap = hazelcastInstance.getMap("documents");

        long allWordsCount = documentMap.values().stream()
                .map(s -> s.split(" "))
                .flatMap(Stream::of)
                .count();

        long termCount = documentMap.values().stream()
                .map(s -> s.split(" "))
                .flatMap(Stream::of)
                .filter(token -> token.equals(term))
                .count();

        return Math.log((double)allWordsCount / (double)termCount);
    }

    public double tf(String documentId, String term) {
        Map<String, String> documentMap = hazelcastInstance.getMap("documents");

        String [] document = documentMap.get(documentId).split(" ");
        double result = 0;
        for (String word : document) {
            if (term.equals(word))
                result++;
        }
        return result / document.length;
    }

    @Override
    public List<IndexEntry> search(String term) {
        Map<String, Set<IndexEntry>> indexMap = hazelcastInstance.getMap("index");
        Set<IndexEntry> documentSet = indexMap.get(term);
        List<IndexEntry> orderedDocumentList = new ArrayList<>(documentSet);
        orderedDocumentList.sort(Comparator.comparingDouble(IndexEntry::getScore).reversed());
        return orderedDocumentList;
    }
}
