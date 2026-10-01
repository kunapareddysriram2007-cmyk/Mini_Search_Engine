package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.core.ds.CustomHashSet;
import com.minisearchengine.core.string.AhoCorasick;
import com.minisearchengine.core.string.KMPStringMatcher;
import com.minisearchengine.core.string.LevenshteinDistance;

/**
 * Main SearchEngine facade bringing together Corpus Ingestion, Inverted Indexing,
 * Query Parsing, Search Algorithm Dispatching, and Multi-factor Relevance Ranking.
 */
public class SearchEngine {

    private CustomArrayList<CorpusDocument> corpusDocuments;
    private final PositionalInvertedIndex positionalIndex;
    private final InvertedIndex invertedIndex;
    private final TextPipeline textPipeline;
    private final QueryParser queryParser;
    private final SearchDispatcher searchDispatcher;
    private final Ranker ranker;

    private boolean isIndexed;
    private long lastIndexBuildTimeMs;
    private int totalTokensCount;

    public SearchEngine() {
        this.corpusDocuments = new CustomArrayList<>();
        this.positionalIndex = new PositionalInvertedIndex();
        this.invertedIndex = new InvertedIndex();
        this.textPipeline = new TextPipeline();
        this.queryParser = new QueryParser();
        this.searchDispatcher = new SearchDispatcher();
        this.ranker = new Ranker();
        this.isIndexed = false;
        this.lastIndexBuildTimeMs = 0;
        this.totalTokensCount = 0;
    }

    public synchronized void buildIndex(CorpusLoader loader) throws java.io.IOException {
        long startTime = System.currentTimeMillis();
        this.corpusDocuments = loader.loadCorpus();

        this.totalTokensCount = 0;

        for (int i = 0; i < corpusDocuments.size(); i++) {
            CorpusDocument cdoc = corpusDocuments.get(i);
            Document doc = cdoc.toDocument();

            positionalIndex.indexDocument(doc);
            invertedIndex.indexDocument(doc);

            CustomArrayList<TextPipeline.Token> tokens = textPipeline.analyze(cdoc.getContent());
            this.totalTokensCount += tokens.size();
        }

        this.lastIndexBuildTimeMs = System.currentTimeMillis() - startTime;
        this.isIndexed = true;
        this.ranker.setInvertedIndex(this.invertedIndex);
    }

    public SearchEngineResult search(SearchQuery searchQuery) {
        long startTime = System.currentTimeMillis();

        if (!isIndexed || corpusDocuments.size() == 0) {
            return new SearchEngineResult(
                    searchQuery, 0, searchQuery.getPage(), searchQuery.getPageSize(), 0,
                    0, "KMP", "Positional Inverted Index", 0, 0, new CustomArrayList<>()
            );
        }

        QueryParser.ParsedQuery parsed = queryParser.parse(searchQuery);
        CustomArrayList<String> queryTerms = parsed.getQueryTerms();

        if (queryTerms.isEmpty()) {
            return new SearchEngineResult(
                    searchQuery, 0, searchQuery.getPage(), searchQuery.getPageSize(), 0,
                    0, parsed.getResolvedAlgorithmDisplayName(), "Positional Inverted Index", 0, 0, new CustomArrayList<>()
            );
        }

        // Candidate retrieval using the existing inverted index:
        // Only documents with legitimate query-term matches become normal lexical search candidates.
        boolean isFuzzy = "edit-distance".equals(parsed.getResolvedAlgorithmId()) || "fuzzy".equalsIgnoreCase(parsed.getMatchType());

        CustomHashSet<Integer> candidateDocIds = new CustomHashSet<>();
        if (isFuzzy) {
            // For fuzzy search, every document may potentially have an edit-distance match
            for (int i = 0; i < corpusDocuments.size(); i++) {
                candidateDocIds.add(corpusDocuments.get(i).getId());
            }
        } else {
            // Lexical search: retrieve matching document IDs from inverted index
            for (int i = 0; i < queryTerms.size(); i++) {
                String term = queryTerms.get(i);
                CustomArrayList<Integer> postings = invertedIndex.getPostings(term);
                for (int p = 0; p < postings.size(); p++) {
                    candidateDocIds.add(postings.get(p));
                }
            }
            // Also check titles for query-term word matches
            for (int i = 0; i < corpusDocuments.size(); i++) {
                CorpusDocument doc = corpusDocuments.get(i);
                if (!candidateDocIds.contains(doc.getId())) {
                    String titleLower = doc.getTitle().toLowerCase();
                    for (int t = 0; t < queryTerms.size(); t++) {
                        if (SearchDispatcher.containsWord(titleLower, queryTerms.get(t))) {
                            candidateDocIds.add(doc.getId());
                            break;
                        }
                    }
                }
            }
        }

        // Filter candidates by metadata (docType, category, language), exact phrase, include & exclude terms
        CustomArrayList<CorpusDocument> filteredCandidates = new CustomArrayList<>();
        String requestedDocType = searchQuery.getDocType();
        String requestedCategory = searchQuery.getCategory();
        String requestedLanguage = searchQuery.getLanguage();
        String exactPhrase = parsed.getExactPhrase();
        CustomArrayList<String> includeTerms = parsed.getIncludeTerms();
        CustomArrayList<String> excludeTerms = parsed.getExcludeTerms();
        KMPStringMatcher kmp = new KMPStringMatcher();

        for (int i = 0; i < corpusDocuments.size(); i++) {
            CorpusDocument doc = corpusDocuments.get(i);
            if (!candidateDocIds.contains(doc.getId())) {
                continue;
            }

            // 1. Filter by docType
            if (requestedDocType != null && !"all".equalsIgnoreCase(requestedDocType)) {
                if (!doc.getDocType().equalsIgnoreCase(requestedDocType) && !doc.getCategory().equalsIgnoreCase(requestedDocType)) {
                    continue;
                }
            }

            // 2. Filter by category
            if (requestedCategory != null && !"all".equalsIgnoreCase(requestedCategory)) {
                if (!doc.getCategory().equalsIgnoreCase(requestedCategory)) {
                    continue;
                }
            }

            // 3. Filter by language
            if (requestedLanguage != null && !"all".equalsIgnoreCase(requestedLanguage)) {
                if (!doc.getLanguage().equalsIgnoreCase(requestedLanguage)) {
                    continue;
                }
            }

            String docContentLower = doc.getContent().toLowerCase();
            String docTitleLower = doc.getTitle().toLowerCase();

            // 4. Filter by exclude terms (must NOT contain any excluded terms)
            if (excludeTerms != null && excludeTerms.size() > 0) {
                boolean containsExcluded = false;
                for (int e = 0; e < excludeTerms.size(); e++) {
                    String exTerm = excludeTerms.get(e).toLowerCase();
                    if (docContentLower.contains(exTerm) || docTitleLower.contains(exTerm)
                            || SearchDispatcher.containsWord(docContentLower, exTerm)
                            || SearchDispatcher.containsWord(docTitleLower, exTerm)) {
                        containsExcluded = true;
                        break;
                    }
                }
                if (containsExcluded) {
                    continue;
                }
            }

            // 5. Filter by include terms (MUST contain all included terms)
            if (includeTerms != null && includeTerms.size() > 0) {
                boolean containsAllIncluded = true;
                for (int in = 0; in < includeTerms.size(); in++) {
                    String inTerm = includeTerms.get(in).toLowerCase();
                    if (!docContentLower.contains(inTerm) && !docTitleLower.contains(inTerm)
                            && !SearchDispatcher.containsWord(docContentLower, inTerm)
                            && !SearchDispatcher.containsWord(docTitleLower, inTerm)) {
                        containsAllIncluded = false;
                        break;
                    }
                }
                if (!containsAllIncluded) {
                    continue;
                }
            }


            // 6. Filter by exact phrase
            if (exactPhrase != null && !exactPhrase.trim().isEmpty()) {
                String epLower = exactPhrase.toLowerCase();
                String epNorm = textPipeline.getNormalizer().normalize(exactPhrase);
                boolean phraseInContent = !kmp.search(docContentLower, epLower).isEmpty()
                        || (!epNorm.isEmpty() && !epNorm.equals(epLower) && !kmp.search(docContentLower, epNorm).isEmpty());
                boolean phraseInTitle = !kmp.search(docTitleLower, epLower).isEmpty()
                        || (!epNorm.isEmpty() && !epNorm.equals(epLower) && !kmp.search(docTitleLower, epNorm).isEmpty());
                if (!phraseInContent && !phraseInTitle) {
                    continue;
                }
            }

            filteredCandidates.add(doc);
        }


        CustomArrayList<SearchDispatcher.MatchResult> matchResults = new CustomArrayList<>();
        for (int i = 0; i < filteredCandidates.size(); i++) {
            SearchDispatcher.MatchResult mr = searchDispatcher.dispatchMatch(filteredCandidates.get(i), parsed);
            matchResults.add(mr);
        }

        int matchingCount = ranker.countMatchingResults(matchResults);
        int totalMatches = ranker.countTotalMatches(matchResults);
        int totalPages = Math.max(1, (int) Math.ceil((double) matchingCount / searchQuery.getPageSize()));

        CustomArrayList<SearchResultItem> paginatedResults = ranker.rankAndPaginate(
                filteredCandidates,
                matchResults,
                parsed,
                searchQuery.getSortBy(),
                searchQuery.getPage(),
                searchQuery.getPageSize(),
                corpusDocuments.size(),
                this.invertedIndex
        );

        long searchTimeMs = System.currentTimeMillis() - startTime;

        // Spellcheck suggestion ("Did you mean: ...") using Levenshtein distance
        String didYouMean = computeDidYouMean(parsed.getQueryTerms());

        // Compute LPS (π) array for live KMP pattern visualization
        int[] lps = new int[0];
        if (!parsed.getNormalizedQuery().isEmpty()) {
            KMPStringMatcher kmpMatcher = new KMPStringMatcher();
            String lpsTarget = parsed.getQueryTerms().size() > 0 ? parsed.getQueryTerms().get(0) : parsed.getNormalizedQuery();
            lps = kmpMatcher.buildLPS(lpsTarget);
        }

        // Aggregate real algorithm execution details
        long totalComparisons = 0;
        CustomHashSet<String> distinctMatchedPatternsSet = new CustomHashSet<>();
        for (int i = 0; i < matchResults.size(); i++) {
            SearchDispatcher.MatchResult mr = matchResults.get(i);
            totalComparisons += mr.getComparisons();
            CustomArrayList<String> mTerms = mr.getMatchedTerms();
            for (int t = 0; t < mTerms.size(); t++) {
                distinctMatchedPatternsSet.add(mTerms.get(t));
            }
        }
        CustomArrayList<String> distinctMatchedPatterns = distinctMatchedPatternsSet.toList();

        int automatonStates = 0;
        if ("aho-corasick".equals(parsed.getResolvedAlgorithmId()) && queryTerms.size() > 0) {
            AhoCorasick aho = new AhoCorasick(queryTerms);
            automatonStates = aho.getNumNodes();
        }

        String originalQuery = searchQuery.getQueryText();
        String suggestedTerm = didYouMean;
        int editDist = 0;
        if (suggestedTerm != null && !suggestedTerm.trim().isEmpty()) {
            LevenshteinDistance ld = new LevenshteinDistance();
            editDist = ld.distance(originalQuery.trim().toLowerCase(), suggestedTerm.trim().toLowerCase());
        } else if ("edit-distance".equals(parsed.getResolvedAlgorithmId()) && paginatedResults.size() > 0) {
            suggestedTerm = paginatedResults.get(0).getDocument().getTitle();
            LevenshteinDistance ld = new LevenshteinDistance();
            editDist = ld.distance(originalQuery.trim().toLowerCase(), suggestedTerm.trim().toLowerCase());
        }

        String algoDisplayName;
        switch (parsed.getResolvedAlgorithmId()) {
            case "smart":
                algoDisplayName = "TF-IDF + Cosine Similarity";
                break;
            case "kmp":
                algoDisplayName = "KMP";
                break;
            case "aho-corasick":
                algoDisplayName = "Aho-Corasick";
                break;
            case "edit-distance":
                algoDisplayName = "Edit Distance";
                break;
            case "z-algo":
                algoDisplayName = "Z Algorithm";
                break;
            case "rabin-karp":
                algoDisplayName = "Rabin-Karp";
                break;
            default:
                algoDisplayName = parsed.getResolvedAlgorithmDisplayName();
                break;
        }

        AlgorithmExecutionDetails algoDetails = new AlgorithmExecutionDetails(
                algoDisplayName,
                parsed.getResolvedAlgorithmId(),
                corpusDocuments.size(),
                filteredCandidates.size(),
                matchingCount,
                searchTimeMs,
                parsed.getNormalizedQuery(),
                totalMatches,
                totalComparisons,
                queryTerms,
                distinctMatchedPatterns,
                automatonStates,
                originalQuery,
                suggestedTerm,
                editDist
        );

        return new SearchEngineResult(
                searchQuery,
                matchingCount,
                searchQuery.getPage(),
                searchQuery.getPageSize(),
                totalPages,
                searchTimeMs,
                parsed.getResolvedAlgorithmDisplayName(),
                "Positional Inverted Index",
                filteredCandidates.size(),
                totalMatches,
                paginatedResults,
                didYouMean,
                lps,
                algoDetails
        );
    }

    public boolean isIndexed() { return isIndexed; }
    public int getCorpusDocumentCount() { return corpusDocuments.size(); }
    public CustomArrayList<CorpusDocument> getCorpusDocuments() { return corpusDocuments; }
    public CorpusDocument getDocumentBySlugOrId(String identifier) {
        if (identifier == null) return null;
        for (int i = 0; i < corpusDocuments.size(); i++) {
            CorpusDocument doc = corpusDocuments.get(i);
            if (doc.getSlugId().equalsIgnoreCase(identifier) || String.valueOf(doc.getId()).equals(identifier)) {
                return doc;
            }
        }
        return null;
    }

    public PositionalInvertedIndex getPositionalIndex() { return positionalIndex; }
    public InvertedIndex getInvertedIndex() { return invertedIndex; }
    public long getLastIndexBuildTimeMs() { return lastIndexBuildTimeMs; }
    public int getTotalTokensCount() { return totalTokensCount; }

    private static final String[] CANONICAL_TERMS = {
        "operating", "system", "systems", "machine", "learning", "artificial", "intelligence",
        "algorithm", "algorithms", "binary", "search", "tree", "trees", "process", "management",
        "memory", "virtual", "paging", "scheduling", "deadlock", "network", "networks", "queue",
        "stack", "graph", "graphs", "hash", "table", "sorting", "quicksort", "mergesort", "computer",
        "science", "structures", "database", "indexing", "cache", "concurrency", "distributed",
        "thread", "threads", "mutex", "semaphore", "kernel", "storage", "filesystem", "array",
        "arrays", "heap", "trie", "suffix", "prefix", "dynamic", "programming", "depth", "breadth",
        "traversal", "shortest", "path", "dijkstra", "kruskal", "prim", "floyd", "warshall"
    };

    private String computeDidYouMean(CustomArrayList<String> queryTerms) {
        if (queryTerms == null || queryTerms.size() == 0) return null;

        LevenshteinDistance ld = new LevenshteinDistance();
        StringBuilder sb = new StringBuilder();
        boolean changed = false;

        for (int i = 0; i < queryTerms.size(); i++) {
            String term = queryTerms.get(i).toLowerCase();
            String bestMatch = term;
            int bestDist = Integer.MAX_VALUE;

            for (String canon : CANONICAL_TERMS) {
                if (canon.equalsIgnoreCase(term)) {
                    bestMatch = term;
                    bestDist = 0;
                    break;
                }
                int d = ld.distance(term, canon);
                if (d < bestDist && d <= Math.max(1, term.length() / 3 + 1)) {
                    bestDist = d;
                    bestMatch = canon;
                }
            }

            if (bestDist > 0 && bestDist <= 2 && !bestMatch.equalsIgnoreCase(term)) {
                sb.append(bestMatch);
                changed = true;
            } else {
                sb.append(term);
            }

            if (i < queryTerms.size() - 1) {
                sb.append(" ");
            }
        }

        return changed ? sb.toString() : null;
    }
}
