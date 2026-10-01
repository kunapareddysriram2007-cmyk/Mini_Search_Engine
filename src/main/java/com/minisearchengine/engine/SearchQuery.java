package com.minisearchengine.engine;

/**
 * Value object encapsulating search query parameters.
 */
public final class SearchQuery {

    private final String queryText;
    private final String matchType;  // "exact" | "partial" | "fuzzy"
    private final String algorithm;  // "auto" | "kmp" | "z-algo" | "aho-corasick" | "rabin-karp" | "edit-distance" | "suffix" | "inverted-index"
    private final String docType;    // "all" | "Wikipedia" | "Article" | "Reference"
    private final String category;   // "all" | "computer-science" | "data-structures" | "algorithms" | "abkhazian" | ...
    private final String language;   // "all" | "English" | "Abkhazian" | "Acehnese" | "Adyghe"
    private final String exactPhrase; // optional exact phrase filter
    private final com.minisearchengine.core.ds.CustomArrayList<String> includeTerms; // terms that MUST be present
    private final com.minisearchengine.core.ds.CustomArrayList<String> excludeTerms; // terms that MUST NOT be present
    private final String sortBy;     // "relevance" | "newest" | "alphabetical"
    private final int page;
    private final int pageSize;

    public SearchQuery(
            String queryText,
            String matchType,
            String algorithm,
            String docType,
            String sortBy,
            int page,
            int pageSize) {
        this(queryText, matchType, algorithm, docType, "all", "all", null, null, null, sortBy, page, pageSize);
    }

    public SearchQuery(
            String queryText,
            String matchType,
            String algorithm,
            String docType,
            String category,
            String language,
            String exactPhrase,
            com.minisearchengine.core.ds.CustomArrayList<String> includeTerms,
            com.minisearchengine.core.ds.CustomArrayList<String> excludeTerms,
            String sortBy,
            int page,
            int pageSize) {
        this.queryText = (queryText == null) ? "" : queryText.trim();
        this.matchType = (matchType == null || matchType.isEmpty()) ? "exact" : matchType.toLowerCase();
        this.algorithm = (algorithm == null || algorithm.isEmpty()) ? "auto" : algorithm.toLowerCase();
        this.docType = (docType == null || docType.isEmpty()) ? "all" : docType;
        this.category = (category == null || category.isEmpty()) ? "all" : category;
        this.language = (language == null || language.isEmpty()) ? "all" : language;
        this.exactPhrase = (exactPhrase != null && !exactPhrase.trim().isEmpty()) ? exactPhrase.trim() : null;
        this.includeTerms = (includeTerms != null) ? includeTerms : new com.minisearchengine.core.ds.CustomArrayList<>();
        this.excludeTerms = (excludeTerms != null) ? excludeTerms : new com.minisearchengine.core.ds.CustomArrayList<>();
        this.sortBy = (sortBy == null || sortBy.isEmpty()) ? "relevance" : sortBy.toLowerCase();
        this.page = Math.max(1, page);
        this.pageSize = Math.max(1, Math.min(50, pageSize));
    }

    public String getQueryText() { return queryText; }
    public String getMatchType() { return matchType; }
    public String getAlgorithm() { return algorithm; }
    public String getDocType() { return docType; }
    public String getCategory() { return category; }
    public String getLanguage() { return language; }
    public String getExactPhrase() { return exactPhrase; }
    public com.minisearchengine.core.ds.CustomArrayList<String> getIncludeTerms() { return includeTerms; }
    public com.minisearchengine.core.ds.CustomArrayList<String> getExcludeTerms() { return excludeTerms; }
    public String getSortBy() { return sortBy; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
}

