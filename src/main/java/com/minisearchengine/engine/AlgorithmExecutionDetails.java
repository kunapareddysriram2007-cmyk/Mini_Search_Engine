package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Encapsulates real runtime algorithm execution details, performance metrics,
 * comparisons, automaton states, and spellcheck/distance calculations.
 *
 * Strict constraint: Zero java.util.* imports.
 */
public final class AlgorithmExecutionDetails {

    private final String algorithmName;
    private final String algorithmFamily;
    private final int documentsIndexed;
    private final int candidateDocuments;
    private final int resultsReturned;
    private final long searchTimeMs;

    // Exact Pattern search (KMP / Rabin-Karp / Z-Algo)
    private final String pattern;
    private final int totalOccurrences;
    private final long comparisons;

    // Multi-Pattern search (Aho-Corasick)
    private final CustomArrayList<String> patterns;
    private final CustomArrayList<String> matchedPatterns;
    private final int automatonStates;

    // Fuzzy Search (Levenshtein Edit Distance)
    private final String originalQuery;
    private final String suggestedTerm;
    private final int editDistance;

    public AlgorithmExecutionDetails(
            String algorithmName,
            String algorithmFamily,
            int documentsIndexed,
            int candidateDocuments,
            int resultsReturned,
            long searchTimeMs,
            String pattern,
            int totalOccurrences,
            long comparisons,
            CustomArrayList<String> patterns,
            CustomArrayList<String> matchedPatterns,
            int automatonStates,
            String originalQuery,
            String suggestedTerm,
            int editDistance) {
        this.algorithmName = (algorithmName == null) ? "TF-IDF + Cosine Similarity" : algorithmName;
        this.algorithmFamily = (algorithmFamily == null) ? "smart" : algorithmFamily;
        this.documentsIndexed = documentsIndexed;
        this.candidateDocuments = candidateDocuments;
        this.resultsReturned = resultsReturned;
        this.searchTimeMs = searchTimeMs;
        this.pattern = pattern;
        this.totalOccurrences = totalOccurrences;
        this.comparisons = comparisons;
        this.patterns = (patterns == null) ? new CustomArrayList<>() : patterns;
        this.matchedPatterns = (matchedPatterns == null) ? new CustomArrayList<>() : matchedPatterns;
        this.automatonStates = automatonStates;
        this.originalQuery = originalQuery;
        this.suggestedTerm = suggestedTerm;
        this.editDistance = editDistance;
    }

    public String getAlgorithmName() { return algorithmName; }
    public String getAlgorithmFamily() { return algorithmFamily; }
    public int getDocumentsIndexed() { return documentsIndexed; }
    public int getCandidateDocuments() { return candidateDocuments; }
    public int getResultsReturned() { return resultsReturned; }
    public long getSearchTimeMs() { return searchTimeMs; }
    public String getPattern() { return pattern; }
    public int getTotalOccurrences() { return totalOccurrences; }
    public long getComparisons() { return comparisons; }
    public CustomArrayList<String> getPatterns() { return patterns; }
    public CustomArrayList<String> getMatchedPatterns() { return matchedPatterns; }
    public int getAutomatonStates() { return automatonStates; }
    public String getOriginalQuery() { return originalQuery; }
    public String getSuggestedTerm() { return suggestedTerm; }
    public int getEditDistance() { return editDistance; }
}
