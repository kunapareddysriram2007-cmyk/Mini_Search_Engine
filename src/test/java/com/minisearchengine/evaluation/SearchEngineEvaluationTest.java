package com.minisearchengine.evaluation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test verifying the evaluation pipeline and benchmark metrics.
 */
public class SearchEngineEvaluationTest {

    private static SearchEngineEvaluator evaluator;
    private static EvaluationMetricsSummary summary;

    @BeforeAll
    public static void setUp() throws IOException {
        evaluator = new SearchEngineEvaluator();
        evaluator.init();
        summary = evaluator.evaluate();
    }

    @Test
    @DisplayName("Verify evaluation dataset query count is at least 50")
    public void testQueryCount() {
        assertNotNull(summary, "Evaluation summary should not be null");
        assertTrue(summary.getTotalQueries() >= 50, "Should evaluate at least 50 queries, actual: " + summary.getTotalQueries());
    }

    @Test
    @DisplayName("Verify Mean Reciprocal Rank (MRR) exceeds baseline threshold")
    public void testMeanReciprocalRank() {
        assertNotNull(summary);
        assertTrue(summary.getMeanReciprocalRank() >= 0.70,
                "MRR should be >= 0.70, actual: " + summary.getMeanReciprocalRank());
    }

    @Test
    @DisplayName("Verify Mean Average Precision (MAP) is positive and robust")
    public void testMeanAveragePrecision() {
        assertNotNull(summary);
        assertTrue(summary.getMeanAveragePrecision() >= 0.50,
                "MAP should be >= 0.50, actual: " + summary.getMeanAveragePrecision());
    }

    @Test
    @DisplayName("Verify Precision@5 and Recall@5 are valid")
    public void testPrecisionAndRecall() {
        assertNotNull(summary);
        assertTrue(summary.getMeanPrecisionAt5() > 0.0, "Mean P@5 should be > 0.0");
        assertTrue(summary.getMeanRecallAt5() >= 0.60, "Mean Recall@5 should be >= 0.60, actual: " + summary.getMeanRecallAt5());
    }

    @Test
    @DisplayName("Verify markdown report generation and export")
    public void testReportGeneration() throws IOException {
        String report = evaluator.generateMarkdownReport(summary);
        assertNotNull(report);
        assertTrue(report.contains("# Mini Search Engine — Evaluation Benchmark Report"));
        assertTrue(report.contains("Mean Reciprocal Rank"));
        assertTrue(report.contains("Executive Summary"));

        String outputPath = "evaluation_report.md";
        evaluator.saveReportToFile(outputPath, summary);
        File reportFile = new File(outputPath);
        assertTrue(reportFile.exists(), "evaluation_report.md should be written to disk");
        assertTrue(reportFile.length() > 0, "evaluation_report.md should not be empty");
    }
}
