package com.minisearchengine.evaluation;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Stores real evaluation metrics and retrieval details for a single query execution.
 */
public class QueryEvaluationResult {

    private final EvaluationQuery query;
    private final int totalExpected;
    private final int totalRetrieved;
    private final int relevantRetrievedAt1;
    private final int relevantRetrievedAt3;
    private final int relevantRetrievedAt5;
    private final int relevantRetrievedAt10;

    private final double precisionAt1;
    private final double precisionAt3;
    private final double precisionAt5;
    private final double precisionAt10;

    private final double recallAt5;
    private final double recallAt10;

    private final double f1At5;
    private final double f1At10;

    private final double reciprocalRank;
    private final double averagePrecision;
    private final long searchTimeMs;

    private final CustomArrayList<String> retrievedDocPaths;
    private final CustomArrayList<Boolean> retrievedDocRelevance;

    public QueryEvaluationResult(
            EvaluationQuery query,
            int totalExpected,
            int totalRetrieved,
            int relevantRetrievedAt1,
            int relevantRetrievedAt3,
            int relevantRetrievedAt5,
            int relevantRetrievedAt10,
            double precisionAt1,
            double precisionAt3,
            double precisionAt5,
            double precisionAt10,
            double recallAt5,
            double recallAt10,
            double f1At5,
            double f1At10,
            double reciprocalRank,
            double averagePrecision,
            long searchTimeMs,
            CustomArrayList<String> retrievedDocPaths,
            CustomArrayList<Boolean> retrievedDocRelevance) {
        this.query = query;
        this.totalExpected = totalExpected;
        this.totalRetrieved = totalRetrieved;
        this.relevantRetrievedAt1 = relevantRetrievedAt1;
        this.relevantRetrievedAt3 = relevantRetrievedAt3;
        this.relevantRetrievedAt5 = relevantRetrievedAt5;
        this.relevantRetrievedAt10 = relevantRetrievedAt10;
        this.precisionAt1 = precisionAt1;
        this.precisionAt3 = precisionAt3;
        this.precisionAt5 = precisionAt5;
        this.precisionAt10 = precisionAt10;
        this.recallAt5 = recallAt5;
        this.recallAt10 = recallAt10;
        this.f1At5 = f1At5;
        this.f1At10 = f1At10;
        this.reciprocalRank = reciprocalRank;
        this.averagePrecision = averagePrecision;
        this.searchTimeMs = searchTimeMs;
        this.retrievedDocPaths = retrievedDocPaths;
        this.retrievedDocRelevance = retrievedDocRelevance;
    }

    public EvaluationQuery getQuery() { return query; }
    public int getTotalExpected() { return totalExpected; }
    public int getTotalRetrieved() { return totalRetrieved; }
    public int getRelevantRetrievedAt1() { return relevantRetrievedAt1; }
    public int getRelevantRetrievedAt3() { return relevantRetrievedAt3; }
    public int getRelevantRetrievedAt5() { return relevantRetrievedAt5; }
    public int getRelevantRetrievedAt10() { return relevantRetrievedAt10; }
    public double getPrecisionAt1() { return precisionAt1; }
    public double getPrecisionAt3() { return precisionAt3; }
    public double getPrecisionAt5() { return precisionAt5; }
    public double getPrecisionAt10() { return precisionAt10; }
    public double getRecallAt5() { return recallAt5; }
    public double getRecallAt10() { return recallAt10; }
    public double getF1At5() { return f1At5; }
    public double getF1At10() { return f1At10; }
    public double getReciprocalRank() { return reciprocalRank; }
    public double getAveragePrecision() { return averagePrecision; }
    public long getSearchTimeMs() { return searchTimeMs; }
    public CustomArrayList<String> getRetrievedDocPaths() { return retrievedDocPaths; }
    public CustomArrayList<Boolean> getRetrievedDocRelevance() { return retrievedDocRelevance; }
}
