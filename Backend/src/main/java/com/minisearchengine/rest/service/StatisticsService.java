package com.minisearchengine.rest.service;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.engine.CorpusDocument;
import com.minisearchengine.engine.SearchEngine;
import com.minisearchengine.rest.dto.PerformanceEntryDto;
import com.minisearchengine.rest.dto.StatisticsResponseDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatisticsService {

    private static class AlgoStats {
        int calls = 0;
        long totalMs = 0;
        double minMs = Double.MAX_VALUE;
        double maxMs = 0;

        synchronized void record(long durationMs) {
            calls++;
            totalMs += durationMs;
            if (durationMs < minMs) minMs = durationMs;
            if (durationMs > maxMs) maxMs = durationMs;
        }

        PerformanceEntryDto toDto(String algoId) {
            double avg = calls > 0 ? (double) totalMs / calls : 0.0;
            double min = minMs == Double.MAX_VALUE ? 0.0 : minMs;
            return new PerformanceEntryDto(algoId, calls, Math.round(avg * 100.0) / 100.0, min, maxMs);
        }
    }

    private final SearchEngine searchEngine;
    private int totalSearches = 0;
    private long totalSearchTimeMs = 0;
    private final Map<String, AlgoStats> algorithmStatsMap = new LinkedHashMap<>();

    public StatisticsService(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
        // Initialize standard algorithms with 0 stats
        String[] standardAlgos = {"kmp", "z-algo", "aho-corasick", "rabin-karp", "edit-distance", "suffix", "inverted-index"};
        for (String algo : standardAlgos) {
            algorithmStatsMap.put(algo, new AlgoStats());
        }
    }

    public synchronized void recordSearch(String algorithm, long durationMs) {
        this.totalSearches++;
        this.totalSearchTimeMs += durationMs;

        String normalizedAlgo = normalizeAlgoKey(algorithm);
        algorithmStatsMap.computeIfAbsent(normalizedAlgo, k -> new AlgoStats()).record(durationMs);
    }

    public synchronized void recordSearch(long durationMs) {
        recordSearch("kmp", durationMs);
    }

    private String normalizeAlgoKey(String algo) {
        if (algo == null) return "kmp";
        String lower = algo.toLowerCase();
        if (lower.contains("kmp")) return "kmp";
        if (lower.contains("z")) return "z-algo";
        if (lower.contains("aho")) return "aho-corasick";
        if (lower.contains("rabin")) return "rabin-karp";
        if (lower.contains("edit") || lower.contains("levenshtein")) return "edit-distance";
        if (lower.contains("suffix")) return "suffix";
        if (lower.contains("inverted")) return "inverted-index";
        return lower;
    }

    public StatisticsResponseDto getStatistics() {
        int docCount = searchEngine.getCorpusDocumentCount();
        int totalTokens = searchEngine.getTotalTokensCount();
        int vocabSize = searchEngine.getPositionalIndex().vocabularySize();
        int avgTokens = docCount > 0 ? totalTokens / docCount : 0;
        long indexBuildTime = searchEngine.getLastIndexBuildTimeMs();
        long avgSearchTime = totalSearches > 0 ? totalSearchTimeMs / totalSearches : 0;

        Map<String, Integer> docTypeCounts = new HashMap<>();
        CustomArrayList<CorpusDocument> docs = searchEngine.getCorpusDocuments();
        for (int i = 0; i < docs.size(); i++) {
            String dt = docs.get(i).getDocType();
            docTypeCounts.put(dt, docTypeCounts.getOrDefault(dt, 0) + 1);
        }

        return new StatisticsResponseDto(
                docCount,
                totalTokens,
                vocabSize,
                avgTokens,
                indexBuildTime,
                100.0,
                avgSearchTime,
                totalSearches,
                docTypeCounts
        );
    }

    public synchronized List<PerformanceEntryDto> getAlgorithmPerformance() {
        List<PerformanceEntryDto> entries = new ArrayList<>();
        for (Map.Entry<String, AlgoStats> entry : algorithmStatsMap.entrySet()) {
            entries.add(entry.getValue().toDto(entry.getKey()));
        }
        return entries;
    }
}
