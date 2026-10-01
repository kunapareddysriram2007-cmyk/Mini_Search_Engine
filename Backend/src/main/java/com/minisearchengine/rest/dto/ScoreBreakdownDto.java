package com.minisearchengine.rest.dto;

import java.util.List;
import java.util.Map;

public record ScoreBreakdownDto(
    List<String> queryTerms,
    List<String> matchedTerms,
    Map<String, Integer> termFrequencies,
    Map<String, Double> tfidfContributions,
    double cosineSimilarity,
    int finalRelevance,
    int termsMatched,
    int totalQueryTerms,
    int termFrequency,
    double tfidf,
    double title,
    double phrase,
    double proximity
) {}

