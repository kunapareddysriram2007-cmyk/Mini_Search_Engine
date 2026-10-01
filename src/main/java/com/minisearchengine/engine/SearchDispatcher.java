package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.core.string.AhoCorasick;
import com.minisearchengine.core.string.KMPStringMatcher;
import com.minisearchengine.core.string.LevenshteinDistance;
import com.minisearchengine.core.string.MultiPatternMatcher;
import com.minisearchengine.core.string.RabinKarpMatcher;
import com.minisearchengine.core.string.SuffixArrayBuilder;
import com.minisearchengine.core.string.ZAlgorithmMatcher;

/**
 * Dispatcher executing real pattern matching and string search algorithms.
 * Directs search queries to KMP, Z-Algorithm, Rabin-Karp, Aho-Corasick, Suffix Array,
 * or Levenshtein Distance according to user request.
 */
public class SearchDispatcher {

    public static final class MatchResult {
        private final int matches;
        private final int[] positions;
        private final boolean titleMatched;
        private final double titleScore;        // 0.0 to 1.0
        private final double termCoverage;      // 0.0 to 1.0 (fraction of distinct query terms found)
        private final boolean phraseMatched;    // whether the full contiguous phrase was found
        private final double avgSimilarity;     // 1.0 for exact/partial, or edit similarity for fuzzy
        private final long comparisons;
        private final CustomArrayList<String> matchedTerms;

        public MatchResult(int matches, int[] positions, boolean titleMatched) {
            this(matches, positions, titleMatched, titleMatched ? 1.0 : 0.0, matches > 0 ? 1.0 : 0.0, matches > 0, 1.0, 0, new CustomArrayList<>());
        }

        public MatchResult(int matches, int[] positions, boolean titleMatched, double titleScore, double termCoverage, boolean phraseMatched, double avgSimilarity) {
            this(matches, positions, titleMatched, titleScore, termCoverage, phraseMatched, avgSimilarity, 0, new CustomArrayList<>());
        }

        public MatchResult(int matches, int[] positions, boolean titleMatched, double titleScore, double termCoverage, boolean phraseMatched, double avgSimilarity, long comparisons, CustomArrayList<String> matchedTerms) {
            this.matches = matches;
            this.positions = (positions == null) ? new int[0] : positions;
            this.titleMatched = titleMatched;
            this.titleScore = titleScore;
            this.termCoverage = termCoverage;
            this.phraseMatched = phraseMatched;
            this.avgSimilarity = avgSimilarity;
            this.comparisons = comparisons;
            this.matchedTerms = (matchedTerms == null) ? new CustomArrayList<>() : matchedTerms;
        }

        public int getMatches() { return matches; }
        public int[] getPositions() { return positions; }
        public boolean isTitleMatched() { return titleMatched; }
        public double getTitleScore() { return titleScore; }
        public double getTermCoverage() { return termCoverage; }
        public boolean isPhraseMatched() { return phraseMatched; }
        public double getAvgSimilarity() { return avgSimilarity; }
        public long getComparisons() { return comparisons; }
        public CustomArrayList<String> getMatchedTerms() { return matchedTerms; }
    }

    private final KMPStringMatcher kmpMatcher;
    private final ZAlgorithmMatcher zMatcher;
    private final RabinKarpMatcher rkMatcher;
    private final LevenshteinDistance levenshteinDistance;

    public SearchDispatcher() {
        this.kmpMatcher = new KMPStringMatcher();
        this.zMatcher = new ZAlgorithmMatcher();
        this.rkMatcher = new RabinKarpMatcher();
        this.levenshteinDistance = new LevenshteinDistance();
    }

    public MatchResult dispatchMatch(CorpusDocument doc, QueryParser.ParsedQuery parsedQuery) {
        String docContentLower = doc.getContent().toLowerCase();
        String docTitleLower = doc.getTitle().toLowerCase();
        String algoId = parsedQuery.getResolvedAlgorithmId();
        String pattern = parsedQuery.getNormalizedQuery();
        CustomArrayList<String> queryTerms = parsedQuery.getQueryTerms();

        if (pattern.isEmpty()) {
            return new MatchResult(0, new int[0], false, 0.0, 0.0, false, 1.0, 0, new CustomArrayList<>());
        }

        // 1. Evaluate Title match quality
        double titleScore = computeTitleScore(docTitleLower, pattern, queryTerms);
        boolean titleMatched = titleScore > 0.0;

        CustomArrayList<Integer> matchPosList = new CustomArrayList<>();
        CustomArrayList<String> matchedTermsList = new CustomArrayList<>();
        double termCoverage = 0.0;
        boolean phraseMatched = false;
        double avgSimilarity = 1.0;
        long comparisons = 0;

        switch (algoId) {
            case "smart":
                // 1. Smart Search: Inverted Index & Token-based multi-term coverage + VSM Cosine Similarity
                if (queryTerms.size() > 1) {
                    boolean[] termFound = new boolean[queryTerms.size()];
                    for (int t = 0; t < queryTerms.size(); t++) {
                        String qTerm = queryTerms.get(t);
                        KMPStringMatcher.KmpSearchResult kmpRes = kmpMatcher.searchWithStats(docContentLower, qTerm);
                        comparisons += kmpRes.getComparisons();
                        CustomArrayList<Integer> kmpMatches = kmpRes.getMatches();
                        for (int i = 0; i < kmpMatches.size(); i++) {
                            int pos = kmpMatches.get(i);
                            if (isWordBoundary(docContentLower, pos, pos + qTerm.length())) {
                                matchPosList.add(pos);
                                termFound[t] = true;
                            }
                        }
                    }
                    int distinctFound = 0;
                    for (int t = 0; t < queryTerms.size(); t++) {
                        if (termFound[t]) {
                            distinctFound++;
                            matchedTermsList.add(queryTerms.get(t));
                        }
                    }
                    for (int t = 0; t < queryTerms.size(); t++) {
                        if (!termFound[t] && containsWord(docTitleLower, queryTerms.get(t))) {
                            distinctFound++;
                            termFound[t] = true;
                            matchedTermsList.add(queryTerms.get(t));
                        }
                    }
                    termCoverage = queryTerms.size() > 0 ? (double) distinctFound / queryTerms.size() : 0.0;
                    phraseMatched = containsWord(docContentLower, pattern);
                } else {
                    KMPStringMatcher.KmpSearchResult kmpRes = kmpMatcher.searchWithStats(docContentLower, pattern);
                    comparisons += kmpRes.getComparisons();
                    CustomArrayList<Integer> kmpMatches = kmpRes.getMatches();
                    for (int i = 0; i < kmpMatches.size(); i++) {
                        int pos = kmpMatches.get(i);
                        if (isWordBoundary(docContentLower, pos, pos + pattern.length())) {
                            matchPosList.add(pos);
                        }
                    }
                    phraseMatched = matchPosList.size() > 0;
                    if (phraseMatched || titleMatched) {
                        matchedTermsList.add(pattern);
                    }
                    termCoverage = phraseMatched ? 1.0 : (titleMatched ? 0.8 : 0.0);
                }
                break;

            case "kmp":
                // 2. Exact Pattern Search: Strict KMP contiguous pattern matching
                String exactTarget = (parsedQuery.getExactPhrase() != null && !parsedQuery.getExactPhrase().trim().isEmpty())
                        ? parsedQuery.getExactPhrase().toLowerCase()
                        : pattern;
                KMPStringMatcher.KmpSearchResult exactKmp = kmpMatcher.searchWithStats(docContentLower, exactTarget);
                comparisons += exactKmp.getComparisons();
                CustomArrayList<Integer> exactMatches = exactKmp.getMatches();
                if (exactMatches.isEmpty() && !exactTarget.equals(pattern)) {
                    KMPStringMatcher.KmpSearchResult fallbackKmp = kmpMatcher.searchWithStats(docContentLower, pattern);
                    comparisons += fallbackKmp.getComparisons();
                    exactMatches = fallbackKmp.getMatches();
                }
                for (int i = 0; i < exactMatches.size(); i++) {
                    int pos = exactMatches.get(i);
                    if (isWordBoundary(docContentLower, pos, pos + exactTarget.length()) || isWordBoundary(docContentLower, pos, pos + pattern.length())) {
                        matchPosList.add(pos);
                    }
                }
                phraseMatched = matchPosList.size() > 0;
                if (phraseMatched || titleMatched) {
                    matchedTermsList.add(exactTarget);
                }
                termCoverage = phraseMatched ? 1.0 : (titleMatched ? 0.8 : 0.0);
                break;

            case "aho-corasick":
                // 3. Multi-Pattern Search: Aho-Corasick dictionary trie automaton
                if (queryTerms.size() > 0) {
                    AhoCorasick aho = new AhoCorasick(queryTerms);
                    CustomArrayList<MultiPatternMatcher.Match> ahoMatches = aho.searchAll(docContentLower);
                    
                    boolean[] termFound = new boolean[queryTerms.size()];
                    for (int i = 0; i < ahoMatches.size(); i++) {
                        MultiPatternMatcher.Match m = ahoMatches.get(i);
                        int start = m.getStartIndex();
                        int end = start + m.getPattern().length();
                        if (!isWordBoundary(docContentLower, start, end)) {
                            continue;
                        }
                        matchPosList.add(start);
                        for (int t = 0; t < queryTerms.size(); t++) {
                            if (queryTerms.get(t).equalsIgnoreCase(m.getPattern())) {
                                termFound[t] = true;
                            }
                        }
                    }

                    int distinctFound = 0;
                    for (int t = 0; t < queryTerms.size(); t++) {
                        if (termFound[t]) {
                            distinctFound++;
                            matchedTermsList.add(queryTerms.get(t));
                        }
                    }

                    for (int t = 0; t < queryTerms.size(); t++) {
                        if (!termFound[t] && containsWord(docTitleLower, queryTerms.get(t))) {
                            distinctFound++;
                            termFound[t] = true;
                            matchedTermsList.add(queryTerms.get(t));
                        }
                    }

                    termCoverage = queryTerms.size() > 0 ? (double) distinctFound / queryTerms.size() : 0.0;
                    phraseMatched = containsWord(docContentLower, pattern);
                }
                break;

            case "edit-distance":
                // 4. Fuzzy Search: Levenshtein distance dynamic programming over vocabulary terms
                int totalFuzzyMatches = 0;
                double simSum = 0.0;
                boolean[] fuzzyTermFound = new boolean[queryTerms.size()];

                int textLen = docContentLower.length();
                int wordStart = -1;

                for (int i = 0; i <= textLen; i++) {
                    boolean isWordChar = (i < textLen) && Character.isLetterOrDigit(docContentLower.charAt(i));
                    if (isWordChar) {
                        if (wordStart == -1) wordStart = i;
                    } else {
                        if (wordStart != -1) {
                            String word = docContentLower.substring(wordStart, i);
                            for (int t = 0; t < queryTerms.size(); t++) {
                                String qTerm = queryTerms.get(t);
                                int threshold = Math.max(1, qTerm.length() / 3);
                                if (levenshteinDistance.withinThreshold(qTerm, word, threshold)) {
                                    matchPosList.add(wordStart);
                                    totalFuzzyMatches++;
                                    fuzzyTermFound[t] = true;
                                    int dist = levenshteinDistance.distance(qTerm, word);
                                    double sim = 1.0 - ((double) dist / Math.max(qTerm.length(), word.length()));
                                    simSum += sim;
                                }
                            }
                            wordStart = -1;
                        }
                    }
                }

                int distinctFuzzy = 0;
                for (int t = 0; t < queryTerms.size(); t++) {
                    if (fuzzyTermFound[t]) {
                        distinctFuzzy++;
                        matchedTermsList.add(queryTerms.get(t));
                    }
                }

                termCoverage = queryTerms.size() > 0 ? (double) distinctFuzzy / queryTerms.size() : 0.0;
                avgSimilarity = totalFuzzyMatches > 0 ? simSum / totalFuzzyMatches : 0.75;
                phraseMatched = containsWord(docContentLower, pattern);

                int[] fuzzyPositions = new int[matchPosList.size()];
                for (int i = 0; i < matchPosList.size(); i++) {
                    fuzzyPositions[i] = matchPosList.get(i);
                }

                return new MatchResult(totalFuzzyMatches, fuzzyPositions, titleMatched, titleScore, termCoverage, phraseMatched, avgSimilarity, 0, matchedTermsList);

            case "z-algo":
                CustomArrayList<Integer> zMatches = zMatcher.search(docContentLower, pattern);
                for (int i = 0; i < zMatches.size(); i++) {
                    int pos = zMatches.get(i);
                    if (isWordBoundary(docContentLower, pos, pos + pattern.length())) {
                        matchPosList.add(pos);
                    }
                }
                phraseMatched = matchPosList.size() > 0;
                termCoverage = phraseMatched ? 1.0 : (titleMatched ? 0.8 : 0.0);
                break;

            case "rabin-karp":
                CustomArrayList<Integer> rkMatches = rkMatcher.search(docContentLower, pattern);
                for (int i = 0; i < rkMatches.size(); i++) {
                    int pos = rkMatches.get(i);
                    if (isWordBoundary(docContentLower, pos, pos + pattern.length())) {
                        matchPosList.add(pos);
                    }
                }
                phraseMatched = matchPosList.size() > 0;
                termCoverage = phraseMatched ? 1.0 : (titleMatched ? 0.8 : 0.0);
                break;

            case "suffix":
                CustomArrayList<Integer> suffixMatches;
                try {
                    SuffixArrayBuilder suffixBuilder = new SuffixArrayBuilder(docContentLower);
                    suffixMatches = suffixBuilder.search(pattern);
                } catch (Exception e) {
                    suffixMatches = kmpMatcher.search(docContentLower, pattern);
                }
                for (int i = 0; i < suffixMatches.size(); i++) {
                    int pos = suffixMatches.get(i);
                    if (isWordBoundary(docContentLower, pos, pos + pattern.length())) {
                        matchPosList.add(pos);
                    }
                }
                phraseMatched = matchPosList.size() > 0;
                termCoverage = phraseMatched ? 1.0 : (titleMatched ? 0.8 : 0.0);
                break;

            default:
                break;
        }

        int[] finalPos = new int[matchPosList.size()];
        for (int i = 0; i < matchPosList.size(); i++) {
            finalPos[i] = matchPosList.get(i);
        }

        if (parsedQuery.isQuotedPhrase() && !phraseMatched && !containsWord(docTitleLower, pattern)) {
            return new MatchResult(0, new int[0], false, 0.0, 0.0, false, 1.0, comparisons, matchedTermsList);
        }

        return new MatchResult(finalPos.length, finalPos, titleMatched, titleScore, termCoverage, phraseMatched, avgSimilarity, comparisons, matchedTermsList);
    }

    public static boolean isWordBoundary(String text, int start, int end) {
        if (text == null || start < 0 || end > text.length() || start > end) {
            return false;
        }
        boolean startOk = (start == 0) || !Character.isLetterOrDigit(text.charAt(start - 1));
        boolean endOk = (end == text.length()) || !Character.isLetterOrDigit(text.charAt(end));
        return startOk && endOk;
    }

    public static boolean containsWord(String text, String word) {
        if (text == null || word == null || word.isEmpty() || text.length() < word.length()) {
            return false;
        }
        int textLen = text.length();
        int wordLen = word.length();
        int idx = 0;
        while ((idx = text.indexOf(word, idx)) != -1) {
            if (isWordBoundary(text, idx, idx + wordLen)) {
                return true;
            }
            idx++;
        }
        return false;
    }

    private static double computeTitleScore(String titleLower, String pattern, CustomArrayList<String> queryTerms) {
        if (titleLower.equals(pattern)) {
            return 1.0; // Perfect exact title
        }
        if (titleLower.startsWith(pattern + " ") || titleLower.startsWith(pattern + ":") || titleLower.startsWith(pattern + "-")) {
            return 0.95; // Title starts with pattern
        }
        if (containsWord(titleLower, pattern)) {
            return 0.85; // Title contains full phrase as words
        }
        if (queryTerms != null && queryTerms.size() > 1) {
            int termsInTitle = 0;
            for (int i = 0; i < queryTerms.size(); i++) {
                if (containsWord(titleLower, queryTerms.get(i))) {
                    termsInTitle++;
                }
            }
            if (termsInTitle == queryTerms.size()) {
                return 0.70; // All query terms in title, though not as contiguous phrase
            }
            if (termsInTitle > 0) {
                return 0.40 * ((double) termsInTitle / queryTerms.size());
            }
        }
        return 0.0;
    }
}
