package com.minisearchengine.rest.controller;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.engine.ClasspathCorpusLoader;
import com.minisearchengine.engine.CorpusDocument;
import com.minisearchengine.engine.SearchEngine;
import com.minisearchengine.rest.dto.AlgorithmInfoDto;
import com.minisearchengine.rest.dto.HealthResponseDto;
import com.minisearchengine.rest.dto.PerformanceEntryDto;
import com.minisearchengine.rest.dto.StatisticsResponseDto;
import com.minisearchengine.rest.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class MetadataController {

    private final SearchEngine searchEngine;
    private final StatisticsService statisticsService;

    public MetadataController(SearchEngine searchEngine, StatisticsService statisticsService) {
        this.searchEngine = searchEngine;
        this.statisticsService = statisticsService;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponseDto> getHealth() {
        return ResponseEntity.ok(new HealthResponseDto(
                "UP",
                searchEngine.isIndexed(),
                searchEngine.getCorpusDocumentCount()
        ));
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponseDto> getStatistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/algorithms")
    public ResponseEntity<Map<String, List<AlgorithmInfoDto>>> getAlgorithms() {
        List<AlgorithmInfoDto> list = Arrays.asList(
                new AlgorithmInfoDto("kmp", "KMP", "Knuth-Morris-Pratt", "Linear-time exact pattern matching", "O(n + m)", "O(m)", "Preprocesses the pattern to build a failure function that avoids redundant character comparisons."),
                new AlgorithmInfoDto("z-algo", "Z Algorithm", "Z Algorithm", "String matching via Z-array prefix computation", "O(n + m)", "O(n + m)", "Constructs a Z-array where each entry Z[i] is the length of the longest substring starting at position i that matches a prefix."),
                new AlgorithmInfoDto("aho-corasick", "Aho-Corasick", "Aho-Corasick", "Simultaneous multi-pattern matching", "O(n + m + z)", "O(mk)", "Builds a finite-state automaton from a set of patterns allowing all patterns to be searched simultaneously in a single pass."),
                new AlgorithmInfoDto("rabin-karp", "Rabin-Karp", "Rabin-Karp", "Rolling hash-based substring search", "O(n + m) avg", "O(1)", "Uses a rolling hash function to quickly identify candidate match positions, then verifies character by character."),
                new AlgorithmInfoDto("edit-distance", "Edit Distance", "Levenshtein Distance", "Fuzzy matching via dynamic programming", "O(n × m)", "O(n × m)", "Computes the minimum number of single-character edits required to transform one string into another."),
                new AlgorithmInfoDto("suffix", "Suffix Array", "Suffix Array & Kasai LCP", "Fast substring search via prefix doubling & binary search", "O(m log n)", "O(n)", "Prefix-doubling suffix array with LCP array enabling binary search occurrence range queries."),
                new AlgorithmInfoDto("inverted-index", "Inverted Index", "Positional Inverted Index", "Fast document retrieval and ranking", "O(1) lookup", "O(n × t)", "Maps each unique term to a list of documents containing it with positional data.")
        );

        Map<String, List<AlgorithmInfoDto>> wrapper = new HashMap<>();
        wrapper.put("algorithms", list);
        return ResponseEntity.ok(wrapper);
    }

    @GetMapping("/algorithms/performance")
    public ResponseEntity<Map<String, List<PerformanceEntryDto>>> getAlgorithmPerformance() {
        Map<String, List<PerformanceEntryDto>> wrapper = new HashMap<>();
        wrapper.put("entries", statisticsService.getAlgorithmPerformance());
        return ResponseEntity.ok(wrapper);
    }

    @PostMapping("/index/rebuild")
    public ResponseEntity<Map<String, Object>> rebuildIndex() throws IOException {
        long start = System.currentTimeMillis();
        searchEngine.buildIndex(new ClasspathCorpusLoader());
        long elapsed = System.currentTimeMillis() - start;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "REBUILT");
        result.put("documents", searchEngine.getCorpusDocumentCount());
        result.put("buildTimeMs", elapsed);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, List<String>>> getSuggestions(@RequestParam(value = "q", defaultValue = "") String prefix) {
        Set<String> resultSet = new LinkedHashSet<>();
        String queryPrefix = prefix != null ? prefix.trim().toLowerCase() : "";

        if (!queryPrefix.isEmpty()) {
            // 1. Search document titles
            CustomArrayList<CorpusDocument> docs = searchEngine.getCorpusDocuments();
            for (int i = 0; i < docs.size(); i++) {
                String title = docs.get(i).getTitle();
                if (title.toLowerCase().startsWith(queryPrefix) || title.toLowerCase().contains(" " + queryPrefix)) {
                    resultSet.add(title);
                    if (resultSet.size() >= 8) break;
                }
            }

            // 2. Search vocabulary terms
            if (resultSet.size() < 8) {
                CustomArrayList<String> vocab = searchEngine.getPositionalIndex().getVocabulary();
                for (int i = 0; i < vocab.size(); i++) {
                    String term = vocab.get(i);
                    if (term.toLowerCase().startsWith(queryPrefix)) {
                        resultSet.add(term);
                        if (resultSet.size() >= 8) break;
                    }
                }
            }
        } else {
            // When query is empty, return top document titles from actual indexed corpus
            CustomArrayList<CorpusDocument> docs = searchEngine.getCorpusDocuments();
            for (int i = 0; i < Math.min(docs.size(), 8); i++) {
                resultSet.add(docs.get(i).getTitle());
            }
        }

        Map<String, List<String>> wrapper = new HashMap<>();
        wrapper.put("suggestions", new ArrayList<>(resultSet));
        return ResponseEntity.ok(wrapper);
    }

    @GetMapping("/filters")
    public ResponseEntity<Map<String, Object>> getFilterMetadata() {
        Set<String> categories = new LinkedHashSet<>();
        Set<String> languages = new LinkedHashSet<>();
        Set<String> docTypes = new LinkedHashSet<>();

        CustomArrayList<CorpusDocument> docs = searchEngine.getCorpusDocuments();
        for (int i = 0; i < docs.size(); i++) {
            CorpusDocument doc = docs.get(i);
            categories.add(doc.getCategory());
            languages.add(doc.getLanguage());
            docTypes.add(doc.getDocType());
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("categories", new ArrayList<>(categories));
        map.put("languages", new ArrayList<>(languages));
        map.put("docTypes", new ArrayList<>(docTypes));
        return ResponseEntity.ok(map);
    }
}

