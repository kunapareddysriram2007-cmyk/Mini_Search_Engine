package com.minisearchengine.engine;

/**
 * Single search result item returned by the search engine.
 */
public final class SearchResultItem {

    private final CorpusDocument document;
    private final int matches;
    private final int relevance;
    private final String algorithm;
    private final String snippet;
    private final ScoreBreakdown scoreBreakdown;
    private final int[] matchPositions;

    public SearchResultItem(
            CorpusDocument document,
            int matches,
            int relevance,
            String algorithm,
            String snippet,
            ScoreBreakdown scoreBreakdown,
            int[] matchPositions) {
        if (document == null) throw new IllegalArgumentException("document must not be null");
        if (algorithm == null) throw new IllegalArgumentException("algorithm must not be null");
        if (snippet == null) throw new IllegalArgumentException("snippet must not be null");
        if (scoreBreakdown == null) throw new IllegalArgumentException("scoreBreakdown must not be null");

        this.document = document;
        this.matches = matches;
        this.relevance = Math.max(0, Math.min(100, relevance));
        this.algorithm = algorithm;
        this.snippet = snippet;
        this.scoreBreakdown = scoreBreakdown;
        this.matchPositions = (matchPositions == null) ? new int[0] : matchPositions;
    }

    public CorpusDocument getDocument() { return document; }
    public int getMatches() { return matches; }
    public int getRelevance() { return relevance; }
    public String getAlgorithm() { return algorithm; }
    public String getSnippet() { return snippet; }
    public ScoreBreakdown getScoreBreakdown() { return scoreBreakdown; }
    public int[] getMatchPositions() { return matchPositions; }
}
