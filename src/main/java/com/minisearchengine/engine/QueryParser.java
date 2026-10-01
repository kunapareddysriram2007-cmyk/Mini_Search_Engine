package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Parses user search queries into normalized tokens and resolves search algorithm dispatching.
 * Supports exact phrase detection via double quotes ("machine learning").
 */
public class QueryParser {

    public static final class ParsedQuery {
        private final String rawQuery;
        private final String normalizedQuery;
        private final CustomArrayList<String> queryTerms;       // normalized words
        private final CustomArrayList<String> stemmedTokens;    // stemmed terms
        private final String matchType;                         // exact | partial | fuzzy
        private final String resolvedAlgorithmId;              // kmp | z-algo | aho-corasick | rabin-karp | edit-distance | suffix | inverted-index
        private final String resolvedAlgorithmDisplayName;     // KMP | Z Algorithm | Aho-Corasick | Rabin-Karp | Edit Distance | Suffix Array | Inverted Index
        private final boolean quotedPhrase;
        private final String exactPhrase;
        private final CustomArrayList<String> includeTerms;
        private final CustomArrayList<String> excludeTerms;

        public ParsedQuery(
                String rawQuery,
                String normalizedQuery,
                CustomArrayList<String> queryTerms,
                CustomArrayList<String> stemmedTokens,
                String matchType,
                String resolvedAlgorithmId,
                String resolvedAlgorithmDisplayName) {
            this(rawQuery, normalizedQuery, queryTerms, stemmedTokens, matchType, resolvedAlgorithmId, resolvedAlgorithmDisplayName, false, null, null, null);
        }

        public ParsedQuery(
                String rawQuery,
                String normalizedQuery,
                CustomArrayList<String> queryTerms,
                CustomArrayList<String> stemmedTokens,
                String matchType,
                String resolvedAlgorithmId,
                String resolvedAlgorithmDisplayName,
                boolean quotedPhrase) {
            this(rawQuery, normalizedQuery, queryTerms, stemmedTokens, matchType, resolvedAlgorithmId, resolvedAlgorithmDisplayName, quotedPhrase, null, null, null);
        }

        public ParsedQuery(
                String rawQuery,
                String normalizedQuery,
                CustomArrayList<String> queryTerms,
                CustomArrayList<String> stemmedTokens,
                String matchType,
                String resolvedAlgorithmId,
                String resolvedAlgorithmDisplayName,
                boolean quotedPhrase,
                String exactPhrase,
                CustomArrayList<String> includeTerms,
                CustomArrayList<String> excludeTerms) {
            this.rawQuery = rawQuery;
            this.normalizedQuery = normalizedQuery;
            this.queryTerms = queryTerms != null ? queryTerms : new CustomArrayList<>();
            this.stemmedTokens = stemmedTokens != null ? stemmedTokens : new CustomArrayList<>();
            this.matchType = matchType;
            this.resolvedAlgorithmId = resolvedAlgorithmId;
            this.resolvedAlgorithmDisplayName = resolvedAlgorithmDisplayName;
            this.quotedPhrase = quotedPhrase;
            this.exactPhrase = exactPhrase;
            this.includeTerms = includeTerms != null ? includeTerms : new CustomArrayList<>();
            this.excludeTerms = excludeTerms != null ? excludeTerms : new CustomArrayList<>();
        }

        public String getRawQuery() { return rawQuery; }
        public String getNormalizedQuery() { return normalizedQuery; }
        public CustomArrayList<String> getQueryTerms() { return queryTerms; }
        public CustomArrayList<String> getStemmedTokens() { return stemmedTokens; }
        public String getMatchType() { return matchType; }
        public String getResolvedAlgorithmId() { return resolvedAlgorithmId; }
        public String getResolvedAlgorithmDisplayName() { return resolvedAlgorithmDisplayName; }
        public boolean isQuotedPhrase() { return quotedPhrase; }
        public String getExactPhrase() { return exactPhrase; }
        public CustomArrayList<String> getIncludeTerms() { return includeTerms; }
        public CustomArrayList<String> getExcludeTerms() { return excludeTerms; }
    }


    private final TextPipeline pipeline;
    private final TextNormalizer normalizer;
    private final Tokenizer tokenizer;

    public QueryParser() {
        this.pipeline = new TextPipeline();
        this.normalizer = new TextNormalizer();
        this.tokenizer = new Tokenizer();
    }

    public ParsedQuery parse(SearchQuery searchQuery) {
        String raw = searchQuery.getQueryText();
        boolean quotedPhrase = false;
        String cleanQuery = raw != null ? raw : "";
        String extractedExactPhrase = searchQuery.getExactPhrase();
        CustomArrayList<String> includeTerms = new CustomArrayList<>();
        CustomArrayList<String> excludeTerms = new CustomArrayList<>();

        // Copy any explicitly provided include/exclude terms from SearchQuery
        CustomArrayList<String> explicitIncludes = searchQuery.getIncludeTerms();
        if (explicitIncludes != null) {
            for (int i = 0; i < explicitIncludes.size(); i++) {
                String it = explicitIncludes.get(i);
                if (it != null && !it.trim().isEmpty()) {
                    includeTerms.add(normalizer.normalize(it.trim()));
                }
            }
        }
        CustomArrayList<String> explicitExcludes = searchQuery.getExcludeTerms();
        if (explicitExcludes != null) {
            for (int i = 0; i < explicitExcludes.size(); i++) {
                String et = explicitExcludes.get(i);
                if (et != null && !et.trim().isEmpty()) {
                    excludeTerms.add(normalizer.normalize(et.trim()));
                }
            }
        }

        if (raw != null) {
            String trimmed = raw.trim();
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
                quotedPhrase = true;
                cleanQuery = trimmed.substring(1, trimmed.length() - 1);
                if (extractedExactPhrase == null) {
                    extractedExactPhrase = cleanQuery;
                }
            } else {
                // Parse out +/- prefixes and embedded quotes
                StringBuilder cleanBuilder = new StringBuilder();
                String[] words = trimmed.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String w = words[i];
                    if (w.startsWith("+") && w.length() > 1) {
                        includeTerms.add(normalizer.normalize(w.substring(1)));
                    } else if (w.startsWith("-") && w.length() > 1) {
                        excludeTerms.add(normalizer.normalize(w.substring(1)));
                    } else {
                        if (cleanBuilder.length() > 0) cleanBuilder.append(" ");
                        cleanBuilder.append(w);
                    }
                }
                cleanQuery = cleanBuilder.toString();
            }
        }

        String normalized = normalizer.normalize(cleanQuery);

        CustomArrayList<String> queryTerms = tokenizer.tokenize(normalized);
        CustomArrayList<TextPipeline.Token> tokens = pipeline.analyze(cleanQuery);
        CustomArrayList<String> stemmedTokens = new CustomArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            stemmedTokens.add(tokens.get(i).getTerm());
        }

        String matchType = quotedPhrase ? "exact" : searchQuery.getMatchType();
        String algoInput = quotedPhrase ? "kmp" : searchQuery.getAlgorithm();

        String resolvedId;
        String displayName;

        if ("smart".equalsIgnoreCase(algoInput) || "smart-search".equalsIgnoreCase(algoInput) || "vsm".equalsIgnoreCase(algoInput) || "inverted-index".equalsIgnoreCase(algoInput)) {
            resolvedId = "smart";
            displayName = "Smart Search (VSM)";
        } else if ("exact-pattern".equalsIgnoreCase(algoInput)) {
            resolvedId = "kmp";
            displayName = "Exact Pattern (KMP)";
        } else if ("multi-pattern".equalsIgnoreCase(algoInput) || "multi".equalsIgnoreCase(algoInput)) {
            resolvedId = "aho-corasick";
            displayName = "Multi-Pattern (Aho-Corasick)";
        } else if ("fuzzy-search".equalsIgnoreCase(algoInput)) {
            resolvedId = "edit-distance";
            displayName = "Fuzzy Search (Levenshtein)";
        } else if ("kmp".equalsIgnoreCase(algoInput)) {
            resolvedId = "kmp";
            displayName = "KMP";
        } else if ("aho-corasick".equalsIgnoreCase(algoInput)) {
            resolvedId = "aho-corasick";
            displayName = "Aho-Corasick";
        } else if ("edit-distance".equalsIgnoreCase(algoInput) || "levenshtein".equalsIgnoreCase(algoInput)) {
            resolvedId = "edit-distance";
            displayName = "Edit Distance";
        } else if ("z-algo".equalsIgnoreCase(algoInput)) {
            resolvedId = "z-algo";
            displayName = "Z Algorithm";
        } else if ("rabin-karp".equalsIgnoreCase(algoInput)) {
            resolvedId = "rabin-karp";
            displayName = "Rabin-Karp";
        } else if ("suffix".equalsIgnoreCase(algoInput)) {
            resolvedId = "suffix";
            displayName = "Suffix Array";
        } else {
            // Auto / fallback resolution based on matchType and quotes
            if (quotedPhrase) {
                resolvedId = "kmp";
                displayName = "Exact Pattern (KMP)";
            } else if ("fuzzy".equalsIgnoreCase(matchType) || "fuzzy".equalsIgnoreCase(algoInput)) {
                resolvedId = "edit-distance";
                displayName = "Fuzzy Search (Levenshtein)";
            } else if ("exact".equalsIgnoreCase(algoInput)) {
                resolvedId = "kmp";
                displayName = "Exact Pattern (KMP)";
            } else if ("partial".equalsIgnoreCase(matchType)) {
                resolvedId = "aho-corasick";
                displayName = "Multi-Pattern (Aho-Corasick)";
            } else {
                resolvedId = "smart";
                displayName = "Smart Search (VSM)";
            }
        }

        return new ParsedQuery(
                raw,
                normalized,
                queryTerms,
                stemmedTokens,
                matchType,
                resolvedId,
                displayName,
                quotedPhrase,
                extractedExactPhrase,
                includeTerms,
                excludeTerms
        );
    }
}

