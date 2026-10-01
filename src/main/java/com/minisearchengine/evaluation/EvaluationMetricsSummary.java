package com.minisearchengine.evaluation;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Encapsulates global aggregate IR metrics (Mean Precision, Recall, F1, MRR, MAP)
 * computed across the entire evaluation benchmark.
 */
public class EvaluationMetricsSummary {

    private final int totalQueries;
    private final double meanPrecisionAt1;
    private final double meanPrecisionAt3;
    private final double meanPrecisionAt5;
    private final double meanPrecisionAt10;

    private final double meanRecallAt5;
    private final double meanRecallAt10;

    private final double meanF1At5;
    private final double meanF1At10;

    private final double meanReciprocalRank; // MRR
    private final double meanAveragePrecision; // MAP
    private final double averageLatencyMs;

    private final CustomArrayList<QueryEvaluationResult> queryResults;

    public EvaluationMetricsSummary(
            int totalQueries,
            double meanPrecisionAt1,
            double meanPrecisionAt3,
            double meanPrecisionAt5,
            double meanPrecisionAt10,
            double meanRecallAt5,
            double meanRecallAt10,
            double meanF1At5,
            double meanF1At10,
            double meanReciprocalRank,
            double meanAveragePrecision,
            double averageLatencyMs,
            CustomArrayList<QueryEvaluationResult> queryResults) {
        this.totalQueries = totalQueries;
        this.meanPrecisionAt1 = meanPrecisionAt1;
        this.meanPrecisionAt3 = meanPrecisionAt3;
        this.meanPrecisionAt5 = meanPrecisionAt5;
        this.meanPrecisionAt10 = meanPrecisionAt10;
        this.meanRecallAt5 = meanRecallAt5;
        this.meanRecallAt10 = meanRecallAt10;
        this.meanF1At5 = meanF1At5;
        this.meanF1At10 = meanF1At10;
        this.meanReciprocalRank = meanReciprocalRank;
        this.meanAveragePrecision = meanAveragePrecision;
        this.averageLatencyMs = averageLatencyMs;
        this.queryResults = queryResults;
    }

    public int getTotalQueries() { return totalQueries; }
    public double getMeanPrecisionAt1() { return meanPrecisionAt1; }
    public double getMeanPrecisionAt3() { return meanPrecisionAt3; }
    public double getMeanPrecisionAt5() { return meanPrecisionAt5; }
    public double getMeanPrecisionAt10() { return meanPrecisionAt10; }
    public double getMeanRecallAt5() { return meanRecallAt5; }
    public double getMeanRecallAt10() { return meanRecallAt10; }
    public double getMeanF1At5() { return meanF1At5; }
    public double getMeanF1At10() { return meanF1At10; }
    public double getMeanReciprocalRank() { return meanReciprocalRank; }
    public double getMeanAveragePrecision() { return meanAveragePrecision; }
    public double getAverageLatencyMs() { return averageLatencyMs; }
    public CustomArrayList<QueryEvaluationResult> getQueryResults() { return queryResults; }
}
