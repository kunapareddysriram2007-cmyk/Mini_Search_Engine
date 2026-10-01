package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Encapsulates full search results, performance metrics, pagination context,
 * spellcheck suggestions ("Did you mean?"), and live algorithm visualization data (LPS array).
 */
public final class SearchEngineResult {

    private final SearchQuery query;
    private final int totalResults;
    private final int page;
    private final int pageSize;
    private final int totalPages;
    private final long searchTimeMs;
    private final String algorithmUsed;
    private final String indexType;
    private final int documentsScanned;
    private final int totalMatches;
    private final CustomArrayList<SearchResultItem> results;
    private final String didYouMean;
    private final int[] lpsArray;
    private final AlgorithmExecutionDetails algorithmExecutionDetails;

    public SearchEngineResult(
            SearchQuery query,
            int totalResults,
            int page,
            int pageSize,
            int totalPages,
            long searchTimeMs,
            String algorithmUsed,
            String indexType,
            int documentsScanned,
            int totalMatches,
            CustomArrayList<SearchResultItem> results) {
        this(query, totalResults, page, pageSize, totalPages, searchTimeMs, algorithmUsed, indexType, documentsScanned, totalMatches, results, null, null, null);
    }

    public SearchEngineResult(
            SearchQuery query,
            int totalResults,
            int page,
            int pageSize,
            int totalPages,
            long searchTimeMs,
            String algorithmUsed,
            String indexType,
            int documentsScanned,
            int totalMatches,
            CustomArrayList<SearchResultItem> results,
            String didYouMean,
            int[] lpsArray) {
        this(query, totalResults, page, pageSize, totalPages, searchTimeMs, algorithmUsed, indexType, documentsScanned, totalMatches, results, didYouMean, lpsArray, null);
    }

    public SearchEngineResult(
            SearchQuery query,
            int totalResults,
            int page,
            int pageSize,
            int totalPages,
            long searchTimeMs,
            String algorithmUsed,
            String indexType,
            int documentsScanned,
            int totalMatches,
            CustomArrayList<SearchResultItem> results,
            String didYouMean,
            int[] lpsArray,
            AlgorithmExecutionDetails algorithmExecutionDetails) {
        this.query = query;
        this.totalResults = totalResults;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.searchTimeMs = searchTimeMs;
        this.algorithmUsed = (algorithmUsed == null) ? "KMP" : algorithmUsed;
        this.indexType = (indexType == null) ? "Positional Inverted Index" : indexType;
        this.documentsScanned = documentsScanned;
        this.totalMatches = totalMatches;
        this.results = (results == null) ? new CustomArrayList<>() : results;
        this.didYouMean = didYouMean;
        this.lpsArray = (lpsArray == null) ? new int[0] : lpsArray;
        this.algorithmExecutionDetails = algorithmExecutionDetails;
    }

    public SearchQuery getQuery() { return query; }
    public int getTotalResults() { return totalResults; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return totalPages; }
    public long getSearchTimeMs() { return searchTimeMs; }
    public String getAlgorithmUsed() { return algorithmUsed; }
    public String getIndexType() { return indexType; }
    public int getDocumentsScanned() { return documentsScanned; }
    public int getTotalMatches() { return totalMatches; }
    public CustomArrayList<SearchResultItem> getResults() { return results; }
    public String getDidYouMean() { return didYouMean; }
    public int[] getLpsArray() { return lpsArray; }
    public AlgorithmExecutionDetails getAlgorithmExecutionDetails() { return algorithmExecutionDetails; }
}
