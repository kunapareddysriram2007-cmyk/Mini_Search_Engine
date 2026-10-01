package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.core.ds.CustomHashMap;

/**
 * Detailed breakdown of relevance score components for search explainability during evaluation.
 * Powers the "Why this result?" feature with 100% measured backend values.
 * Strict constraint: ZERO java.util.* imports anywhere in this file.
 */
public final class ScoreBreakdown {

    private final CustomArrayList<String> queryTerms;
    private final CustomArrayList<String> matchedTerms;
    private final CustomHashMap<String, Integer> termFrequencies;
    private final CustomHashMap<String, Double> tfidfContributions;
    private final double cosineSimilarity;
    private final int finalRelevancePct;
    private final int termsMatched;
    private final int totalQueryTerms;
    private final double tfidf;
    private final double title;
    private final double phrase;
    private final double proximity;

    public ScoreBreakdown(
            CustomArrayList<String> queryTerms,
            CustomArrayList<String> matchedTerms,
            CustomHashMap<String, Integer> termFrequencies,
            CustomHashMap<String, Double> tfidfContributions,
            double cosineSimilarity,
            int finalRelevancePct,
            int termsMatched,
            int totalQueryTerms,
            double tfidf,
            double title,
            double phrase,
            double proximity) {
        this.queryTerms = queryTerms != null ? queryTerms : new CustomArrayList<>();
        this.matchedTerms = matchedTerms != null ? matchedTerms : new CustomArrayList<>();
        this.termFrequencies = termFrequencies != null ? termFrequencies : new CustomHashMap<>();
        this.tfidfContributions = tfidfContributions != null ? tfidfContributions : new CustomHashMap<>();
        this.cosineSimilarity = cosineSimilarity;
        this.finalRelevancePct = finalRelevancePct;
        this.termsMatched = termsMatched;
        this.totalQueryTerms = Math.max(1, totalQueryTerms);
        this.tfidf = tfidf;
        this.title = title;
        this.phrase = phrase;
        this.proximity = proximity;
    }

    public ScoreBreakdown(int termsMatched, int totalQueryTerms, double tfidf, double title, double phrase, double proximity) {
        this(new CustomArrayList<>(), new CustomArrayList<>(), new CustomHashMap<>(), new CustomHashMap<>(), tfidf, (int) Math.round(tfidf * 100), termsMatched, totalQueryTerms, tfidf, title, phrase, proximity);
    }

    public ScoreBreakdown(double tfidf, double title, double phrase, double proximity) {
        this(1, 1, tfidf, title, phrase, proximity);
    }

    public CustomArrayList<String> getQueryTerms() { return queryTerms; }
    public CustomArrayList<String> getMatchedTerms() { return matchedTerms; }
    public CustomHashMap<String, Integer> getTermFrequencies() { return termFrequencies; }
    public CustomHashMap<String, Double> getTfidfContributions() { return tfidfContributions; }
    public double getCosineSimilarity() { return cosineSimilarity; }
    public int getFinalRelevancePct() { return finalRelevancePct; }
    public int getTermsMatched() { return termsMatched; }
    public int getTotalQueryTerms() { return totalQueryTerms; }
    public double getTfidf() { return tfidf; }
    public double getTitle() { return title; }
    public double getPhrase() { return phrase; }
    public double getProximity() { return proximity; }

    public double getTotalScore() {
        return (0.45 * tfidf) + (0.25 * title) + (0.15 * phrase) + (0.15 * proximity);
    }
}

