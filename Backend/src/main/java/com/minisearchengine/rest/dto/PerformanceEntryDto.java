package com.minisearchengine.rest.dto;

public record PerformanceEntryDto(
        String algorithm,
        int calls,
        double avgMs,
        double minMs,
        double maxMs
) {}
