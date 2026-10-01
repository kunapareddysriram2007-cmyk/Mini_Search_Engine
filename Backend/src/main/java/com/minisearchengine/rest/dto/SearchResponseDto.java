package com.minisearchengine.rest.dto;

import java.util.List;

public record SearchResponseDto(
    String query,
    int totalResults,
    int page,
    int pageSize,
    int totalPages,
    long searchTimeMs,
    String algorithmUsed,
    String indexType,
    int documentsScanned,
    int totalMatches,
    String didYouMean,
    List<Integer> lpsArray,
    List<ResultItemDto> results,
    AlgorithmDetailsDto algorithmDetails
) {
    public SearchResponseDto(
        String query,
        int totalResults,
        int page,
        int pageSize,
        int totalPages,
        long searchTimeMs,
        String algorithmUsed,
        String indexType,
        int documentsScanned,
        int totalMatches,
        String didYouMean,
        List<Integer> lpsArray,
        List<ResultItemDto> results
    ) {
        this(query, totalResults, page, pageSize, totalPages, searchTimeMs, algorithmUsed, indexType, documentsScanned, totalMatches, didYouMean, lpsArray, results, null);
    }
}
