package com.minisearchengine.rest.dto;

public record HealthResponseDto(
    String status,
    boolean indexReady,
    int documents
) {}
