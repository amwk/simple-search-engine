package com.findwise;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class IndexEntryImpl implements IndexEntry, Serializable {
    /**
     * Document id
     */
    private String id;

    /**
     * Score calculated with TFIDF algorithm
     */
    private double score;

}
