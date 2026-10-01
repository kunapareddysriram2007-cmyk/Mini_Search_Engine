package com.minisearchengine.rest.dto;

import java.util.Map;

public record StatisticsResponseDto(
    int totalDocuments,
    int totalTokens,
    int uniqueTerms,
    int avgTokensPerDoc,
    long indexBuildTimeMs,
    double indexCoveragePct,
    long avgSearchTimeMs,
    int totalSearches,
    Map<String, Integer> docTypeCounts
) {}
