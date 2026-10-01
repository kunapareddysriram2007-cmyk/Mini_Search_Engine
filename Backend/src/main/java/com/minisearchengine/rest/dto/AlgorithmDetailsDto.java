package com.minisearchengine.rest.dto;

import java.util.List;

public record AlgorithmDetailsDto(
    String algorithmName,
    String algorithmFamily,
    int documentsIndexed,
    int candidateDocuments,
    int resultsReturned,
    long searchTimeMs,
    String pattern,
    int totalOccurrences,
    long comparisons,
    List<String> patterns,
    List<String> matchedPatterns,
    int automatonStates,
    String originalQuery,
    String suggestedTerm,
    int editDistance
) {}
