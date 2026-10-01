package com.minisearchengine.rest.dto;

import java.util.List;

public record ResultItemDto(
    String id,
    String title,
    String path,
    String snippet,
    int relevance,
    String algorithm,
    int matches,
    String size,
    String docType,
    String category,
    String language,
    String date,
    List<Integer> matchPositions,
    ScoreBreakdownDto scoreBreakdown
) {
    public ResultItemDto(
        String id,
        String title,
        String path,
        String snippet,
        int relevance,
        String algorithm,
        int matches,
        String size,
        String docType,
        String date,
        List<Integer> matchPositions,
        ScoreBreakdownDto scoreBreakdown
    ) {
        this(id, title, path, snippet, relevance, algorithm, matches, size, docType, "general", "English", date, matchPositions, scoreBreakdown);
    }
}

