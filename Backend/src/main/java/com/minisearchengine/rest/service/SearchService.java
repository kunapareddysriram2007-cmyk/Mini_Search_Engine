package com.minisearchengine.rest.service;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.engine.*;
import com.minisearchengine.rest.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final SearchEngine searchEngine;
    private final StatisticsService statisticsService;

    public SearchService(SearchEngine searchEngine, StatisticsService statisticsService) {
        this.searchEngine = searchEngine;
        this.statisticsService = statisticsService;
    }

    public SearchResponseDto executeSearch(SearchRequest req) {
        String queryStr = req.query();
        if (queryStr == null || queryStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Query string must not be empty");
        }

        CustomArrayList<String> incTerms = new CustomArrayList<>();
        if (req.includeTerms() != null) {
            for (String it : req.includeTerms()) {
                if (it != null && !it.trim().isEmpty()) incTerms.add(it.trim());
            }
        }
        CustomArrayList<String> excTerms = new CustomArrayList<>();
        if (req.excludeTerms() != null) {
            for (String et : req.excludeTerms()) {
                if (et != null && !et.trim().isEmpty()) excTerms.add(et.trim());
            }
        }

        SearchQuery query = new SearchQuery(
                queryStr,
                req.matchType(),
                req.algorithm(),
                req.docType(),
                req.category(),
                req.language(),
                req.exactPhrase(),
                incTerms,
                excTerms,
                req.sortBy(),
                req.page() != null ? req.page() : 1,
                req.pageSize() != null ? req.pageSize() : 10
        );

        SearchEngineResult result = searchEngine.search(query);
        statisticsService.recordSearch(result.getAlgorithmUsed(), result.getSearchTimeMs());

        List<ResultItemDto> items = new ArrayList<>();
        CustomArrayList<SearchResultItem> engineResults = result.getResults();

        for (int i = 0; i < engineResults.size(); i++) {
            SearchResultItem item = engineResults.get(i);
            CorpusDocument doc = item.getDocument();

            List<Integer> posList = new ArrayList<>();
            int[] posArr = item.getMatchPositions();
            for (int p : posArr) {
                posList.add(p);
            }

            ScoreBreakdown sb = item.getScoreBreakdown();
            List<String> qTerms = new ArrayList<>();
            List<String> mTerms = new ArrayList<>();
            Map<String, Integer> tfMap = new LinkedHashMap<>();
            Map<String, Double> tfidfMap = new LinkedHashMap<>();

            if (sb != null) {
                if (sb.getQueryTerms() != null) {
                    CustomArrayList<String> qList = sb.getQueryTerms();
                    for (int qi = 0; qi < qList.size(); qi++) {
                        qTerms.add(qList.get(qi));
                    }
                }
                if (sb.getMatchedTerms() != null) {
                    CustomArrayList<String> mList = sb.getMatchedTerms();
                    for (int mi = 0; mi < mList.size(); mi++) {
                        mTerms.add(mList.get(mi));
                    }
                }
                if (sb.getTermFrequencies() != null) {
                    CustomArrayList<String> tfKeys = sb.getTermFrequencies().keyList();
                    if (tfKeys != null) {
                        for (int ki = 0; ki < tfKeys.size(); ki++) {
                            String k = tfKeys.get(ki);
                            tfMap.put(k, sb.getTermFrequencies().get(k));
                        }
                    }
                }
                if (sb.getTfidfContributions() != null) {
                    CustomArrayList<String> tfidfKeys = sb.getTfidfContributions().keyList();
                    if (tfidfKeys != null) {
                        for (int ki = 0; ki < tfidfKeys.size(); ki++) {
                            String k = tfidfKeys.get(ki);
                            tfidfMap.put(k, sb.getTfidfContributions().get(k));
                        }
                    }
                }
            }

            ScoreBreakdownDto sbDto = new ScoreBreakdownDto(
                    qTerms,
                    mTerms,
                    tfMap,
                    tfidfMap,
                    sb != null ? sb.getCosineSimilarity() : (item.getRelevance() / 100.0),
                    item.getRelevance(),
                    sb != null ? sb.getTermsMatched() : (item.getMatches() > 0 ? 1 : 0),
                    sb != null ? sb.getTotalQueryTerms() : 1,
                    item.getMatches(),
                    sb != null ? sb.getTfidf() : 0.0,
                    sb != null ? sb.getTitle() : 0.0,
                    sb != null ? sb.getPhrase() : 0.0,
                    sb != null ? sb.getProximity() : 0.0
            );

            ResultItemDto itemDto = new ResultItemDto(
                    doc.getSlugId(),
                    doc.getTitle(),
                    doc.getPath(),
                    item.getSnippet(),
                    item.getRelevance(),
                    item.getAlgorithm(),
                    item.getMatches(),
                    formatSizeBytes(doc.getSizeBytes()),
                    doc.getDocType(),
                    doc.getCategory(),
                    doc.getLanguage(),
                    doc.getDate(),
                    posList,
                    sbDto
            );
            items.add(itemDto);
        }


        List<Integer> lpsList = new ArrayList<>();
        if (result.getLpsArray() != null) {
            for (int val : result.getLpsArray()) {
                lpsList.add(val);
            }
        }

        AlgorithmExecutionDetails aed = result.getAlgorithmExecutionDetails();
        AlgorithmDetailsDto aedDto = null;
        if (aed != null) {
            List<String> patterns = new ArrayList<>();
            for (int i = 0; i < aed.getPatterns().size(); i++) {
                patterns.add(aed.getPatterns().get(i));
            }
            List<String> matchedPatterns = new ArrayList<>();
            for (int i = 0; i < aed.getMatchedPatterns().size(); i++) {
                matchedPatterns.add(aed.getMatchedPatterns().get(i));
            }

            aedDto = new AlgorithmDetailsDto(
                    aed.getAlgorithmName(),
                    aed.getAlgorithmFamily(),
                    aed.getDocumentsIndexed(),
                    aed.getCandidateDocuments(),
                    aed.getResultsReturned(),
                    aed.getSearchTimeMs(),
                    aed.getPattern(),
                    aed.getTotalOccurrences(),
                    aed.getComparisons(),
                    patterns,
                    matchedPatterns,
                    aed.getAutomatonStates(),
                    aed.getOriginalQuery(),
                    aed.getSuggestedTerm(),
                    aed.getEditDistance()
            );
        }

        return new SearchResponseDto(
                result.getQuery().getQueryText(),
                result.getTotalResults(),
                result.getPage(),
                result.getPageSize(),
                result.getTotalPages(),
                result.getSearchTimeMs(),
                result.getAlgorithmUsed(),
                result.getIndexType(),
                result.getDocumentsScanned(),
                result.getTotalMatches(),
                result.getDidYouMean(),
                lpsList,
                items,
                aedDto
        );
    }

    private String formatCategory(String cat) {
        if (cat == null) return "Computer Science";
        return switch (cat.toLowerCase()) {
            case "data-structures" -> "Data Structures";
            case "algorithms" -> "Algorithms";
            case "computer-science" -> "Computer Science";
            default -> cat;
        };
    }

    private String formatSizeBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        return String.format("%.1f KB", kb);
    }
}
