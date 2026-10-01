package com.minisearchengine.rest.dto;

public record AlgorithmInfoDto(
    String id,
    String name,
    String fullName,
    String purpose,
    String complexity,
    String spaceComplexity,
    String description
) {}
