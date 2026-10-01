package com.minisearchengine.rest.dto;

import java.util.List;

public record SearchRequest(
    String query,
    String matchType,
    String algorithm,
    String docType,
    String category,
    String language,
    String exactPhrase,
    List<String> includeTerms,
    List<String> excludeTerms,
    String sortBy,
    Integer page,
    Integer pageSize
) {
    public SearchRequest(
        String query,
        String matchType,
        String algorithm,
        String docType,
        String sortBy,
        Integer page,
        Integer pageSize
    ) {
        this(query, matchType, algorithm, docType, "all", "all", null, null, null, sortBy, page, pageSize);
    }
}

