package com.minisearchengine.rest.dto;

public record ErrorResponseDto(
    int status,
    String error,
    String message,
    String path,
    String timestamp
) {}
