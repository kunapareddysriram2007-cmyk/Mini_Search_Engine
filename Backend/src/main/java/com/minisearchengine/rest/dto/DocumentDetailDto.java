package com.minisearchengine.rest.dto;

import java.util.List;

public record DocumentDetailDto(
    String id,
    String title,
    String path,
    String docType,
    String category,
    String language,
    String size,
    long sizeBytes,
    int wordCount,
    String date,
    String overview,
    List<SectionDto> sections
) {
    public DocumentDetailDto(
        String id,
        String title,
        String path,
        String docType,
        String category,
        String size,
        long sizeBytes,
        int wordCount,
        String date,
        String overview,
        List<SectionDto> sections
    ) {
        this(id, title, path, docType, category, "English", size, sizeBytes, wordCount, date, overview, sections);
    }
}

