package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.core.ds.CustomHashMap;
import com.minisearchengine.core.ds.CustomPriorityQueue;
import com.minisearchengine.core.ds.MergeSort;

/**
 * Mathematically grounded Vector Space Model (VSM) ranking engine using TF-IDF vectors,
 * Inverse Document Frequency (IDF), Euclidean Vector Norms, and Cosine Similarity.
 *
 * Scoring approach:
 *   Query -> Normalization -> Tokenization -> Candidate Retrieval via Inverted Index
 *   -> TF calculation -> IDF calculation -> TF-IDF vectors -> Cosine Similarity
 *   -> Relevance score (0-100%) -> Deterministic result ranking via stable MergeSort.
 *
 * Strict constraint: ZERO java.util.* imports anywhere in this file.
 */
public class Ranker {

    private static final class ScoredResult {
        final CorpusDocument doc;
        final int matchCount;
        final int[] matchPositions;
        final ScoreBreakdown breakdown;
        final int relevancePct;
        final double cosineSimilarity;

        ScoredResult(
                CorpusDocument doc,
                int matchCount,
                int[] matchPositions,
                ScoreBreakdown breakdown,
                int relevancePct,
                double cosineSimilarity) {
            this.doc = doc;
            this.matchCount = matchCount;
            this.matchPositions = matchPositions;
            this.breakdown = breakdown;
            this.relevancePct = relevancePct;
            this.cosineSimilarity = cosineSimilarity;
        }
    }

    private InvertedIndex invertedIndex;
    private final TextNormalizer normalizer;
    private final Tokenizer tokenizer;

    public Ranker() {
        this.normalizer = new TextNormalizer();
        this.tokenizer = new Tokenizer();
    }

    public Ranker(InvertedIndex invertedIndex) {
        this();
        this.invertedIndex = invertedIndex;
    }

    public void setInvertedIndex(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public InvertedIndex getInvertedIndex() {
        return invertedIndex;
    }

    /**
     * Standard smoothed Inverse Document Frequency (IDF) formulation:
     *   IDF(t) = ln(1.0 + N / df(t))   for df(t) > 0 and N > 0
     *   IDF(t) = 0.0                   for df(t) == 0 or N <= 0
     */
    public double computeIdf(int df, int totalDocs) {
        if (df <= 0 || totalDocs <= 0) {
            return 0.0;
        }
        return Math.log(1.0 + ((double) totalDocs / (double) df));
    }

    /**
     * Computes the Cosine Similarity between a query token stream and a document:
     *   Cosine(q, d) = (q · d) / (||q|| * ||d||)
     *
     * Handles zero vectors safely without division-by-zero.
     * Returns 0.0 if there is zero term overlap between query and document.
     */
    public double computeCosineSimilarity(
            CustomArrayList<String> queryTerms,
            CorpusDocument doc,
            InvertedIndex index,
            int totalDocs) {
        if (doc == null) return 0.0;
        return computeCosineSimilarity(queryTerms, doc.getContent(), index, totalDocs);
    }

    /**
     * Computes Cosine Similarity between query terms and raw document content text.
     */
    public double computeCosineSimilarity(
            CustomArrayList<String> queryTerms,
            String docContent,
            InvertedIndex index,
            int totalDocs) {

        if (queryTerms == null || queryTerms.isEmpty() || docContent == null || docContent.isEmpty() || totalDocs <= 0) {
            return 0.0;
        }

        String normalizedDoc = normalizer.normalize(docContent);
        CustomArrayList<String> docTokens = tokenizer.tokenize(normalizedDoc);
        if (docTokens.isEmpty()) {
            return 0.0;
        }

        // Count term frequencies in doc
        CustomHashMap<String, Integer> docTf = new CustomHashMap<>();
        CustomArrayList<String> distinctDocTerms = new CustomArrayList<>();
        for (int i = 0; i < docTokens.size(); i++) {
            String term = docTokens.get(i);
            Integer count = docTf.get(term);
            if (count == null) {
                docTf.put(term, 1);
                distinctDocTerms.add(term);
            } else {
                docTf.put(term, count + 1);
            }
        }

        // Count term frequencies in query
        CustomHashMap<String, Integer> queryTf = new CustomHashMap<>();
        CustomArrayList<String> distinctQueryTerms = new CustomArrayList<>();
        for (int i = 0; i < queryTerms.size(); i++) {
            String rawTerm = queryTerms.get(i);
            String term = normalizer.normalize(rawTerm);
            if (term.isEmpty()) continue;
            Integer count = queryTf.get(term);
            if (count == null) {
                queryTf.put(term, 1);
                distinctQueryTerms.add(term);
            } else {
                queryTf.put(term, count + 1);
            }
        }

        if (distinctQueryTerms.isEmpty()) {
            return 0.0;
        }

        // Check if there is any term overlap between query and document
        boolean hasOverlap = false;
        for (int i = 0; i < distinctQueryTerms.size(); i++) {
            if (docTf.containsKey(distinctQueryTerms.get(i))) {
                hasOverlap = true;
                break;
            }
        }
        if (!hasOverlap) {
            return 0.0;
        }

        InvertedIndex effectiveIndex = index;
        if (effectiveIndex == null) {
            effectiveIndex = new InvertedIndex();
            effectiveIndex.indexDocument(new Document(1, "", docContent));
        }

        // Compute query vector weights and query norm ||q||
        double queryNormSq = 0.0;
        CustomHashMap<String, Double> queryWeights = new CustomHashMap<>();
        for (int i = 0; i < distinctQueryTerms.size(); i++) {
            String term = distinctQueryTerms.get(i);
            int tf = queryTf.get(term);
            int df = effectiveIndex.getPostings(term).size();
            double idf = computeIdf(df, totalDocs);
            double w = tf * idf;
            queryWeights.put(term, w);
            queryNormSq += w * w;
        }
        double queryNorm = Math.sqrt(queryNormSq);
        if (queryNorm == 0.0) {
            return 0.0; // Prevent division by zero
        }

        // Compute document vector norm ||d||
        double docNormSq = 0.0;
        for (int i = 0; i < distinctDocTerms.size(); i++) {
            String term = distinctDocTerms.get(i);
            int tf = docTf.get(term);
            int df = effectiveIndex.getPostings(term).size();
            double idf = computeIdf(df, totalDocs);
            double w = tf * idf;
            docNormSq += w * w;
        }
        double docNorm = Math.sqrt(docNormSq);
        if (docNorm == 0.0) {
            return 0.0; // Prevent division by zero
        }

        // Compute dot product q · d
        double dotProduct = 0.0;
        for (int i = 0; i < distinctQueryTerms.size(); i++) {
            String term = distinctQueryTerms.get(i);
            Integer dCount = docTf.get(term);
            if (dCount != null && dCount > 0) {
                double wQ = queryWeights.get(term);
                int df = effectiveIndex.getPostings(term).size();
                double idf = computeIdf(df, totalDocs);
                double wD = dCount * idf;
                dotProduct += wQ * wD;
            }
        }

        double cosine = dotProduct / (queryNorm * docNorm);
        if (cosine < 0.0) cosine = 0.0;
        if (cosine > 1.0) cosine = 1.0;
        return cosine;
    }

    public CustomArrayList<SearchResultItem> rankAndPaginate(
            CustomArrayList<CorpusDocument> candidates,
            CustomArrayList<SearchDispatcher.MatchResult> matchResults,
            QueryParser.ParsedQuery query,
            String sortBy,
            int page,
            int pageSize,
            int totalCorpusDocs) {
        return rankAndPaginate(candidates, matchResults, query, sortBy, page, pageSize, totalCorpusDocs, this.invertedIndex);
    }

    public CustomArrayList<SearchResultItem> rankAndPaginate(
            CustomArrayList<CorpusDocument> candidates,
            CustomArrayList<SearchDispatcher.MatchResult> matchResults,
            QueryParser.ParsedQuery query,
            String sortBy,
            int page,
            int pageSize,
            int totalCorpusDocs,
            InvertedIndex index) {

        if (candidates == null || candidates.size() == 0 || query == null || query.getQueryTerms().size() == 0) {
            return new CustomArrayList<>();
        }

        InvertedIndex effectiveIndex = index != null ? index : this.invertedIndex;
        if (effectiveIndex == null) {
            effectiveIndex = new InvertedIndex();
            for (int i = 0; i < candidates.size(); i++) {
                effectiveIndex.indexDocument(candidates.get(i).toDocument());
            }
        }

        int effectiveTotalDocs = totalCorpusDocs > 0 ? totalCorpusDocs : candidates.size();
        CustomArrayList<ScoredResult> scoredList = new CustomArrayList<>();

        for (int i = 0; i < candidates.size(); i++) {
            CorpusDocument doc = candidates.get(i);
            SearchDispatcher.MatchResult mr = matchResults.get(i);

            // Fast rejection rule: if no term matches and no title match, relevance is strictly 0
            if (mr.getMatches() == 0 && !mr.isTitleMatched()) {
                continue;
            }

            double cosine = computeCosineSimilarity(query.getQueryTerms(), doc.getContent(), effectiveIndex, effectiveTotalDocs);

            double score = cosine;
            if (score <= 0.0) {
                if (mr.getMatches() > 0 && mr.getAvgSimilarity() > 0.0) {
                    // For fuzzy matches with spelling variations, score based on edit similarity and term coverage
                    double coverage = mr.getTermCoverage() > 0.0 ? mr.getTermCoverage() : 0.5;
                    double titleBoost = mr.getTitleScore() > 0.0 ? 0.25 * mr.getTitleScore() : 0.0;
                    score = (0.65 * mr.getAvgSimilarity() * coverage) + titleBoost;
                } else if (mr.isTitleMatched()) {
                    score = 0.5 * mr.getTitleScore();
                } else {
                    continue;
                }
            } else if (mr.getAvgSimilarity() < 1.0) {
                score *= mr.getAvgSimilarity();
            }

            int relevancePct = (int) Math.round(score * 100.0);
            if (relevancePct > 100) relevancePct = 100;
            if (relevancePct <= 0) {
                continue;
            }

            CustomArrayList<String> qTerms = query.getQueryTerms();
            CustomArrayList<String> mTerms = new CustomArrayList<>();
            CustomHashMap<String, Integer> termFrequencies = new CustomHashMap<>();
            CustomHashMap<String, Double> tfidfContribs = new CustomHashMap<>();

            String normalizedDocText = normalizer.normalize(doc.getContent());
            CustomArrayList<String> docWordTokens = tokenizer.tokenize(normalizedDocText);
            CustomHashMap<String, Integer> docWordCounts = new CustomHashMap<>();
            for (int wi = 0; wi < docWordTokens.size(); wi++) {
                String w = docWordTokens.get(wi);
                Integer c = docWordCounts.get(w);
                docWordCounts.put(w, (c == null) ? 1 : c + 1);
            }

            for (int qi = 0; qi < qTerms.size(); qi++) {
                String qTerm = qTerms.get(qi);
                Integer count = docWordCounts.get(qTerm);
                int tf = count != null ? count : 0;
                termFrequencies.put(qTerm, tf);

                int df = effectiveIndex.getPostings(qTerm).size();
                double idf = computeIdf(df, effectiveTotalDocs);
                tfidfContribs.put(qTerm, tf * idf);

                if (tf > 0 || SearchDispatcher.containsWord(doc.getTitle().toLowerCase(), qTerm)) {
                    mTerms.add(qTerm);
                }
            }

            ScoreBreakdown breakdown = new ScoreBreakdown(
                    qTerms,
                    mTerms,
                    termFrequencies,
                    tfidfContribs,
                    cosine,
                    relevancePct,
                    mTerms.size(),
                    qTerms.size(),
                    cosine,
                    mr.getTitleScore(),
                    mr.isPhraseMatched() ? 1.0 : 0.0,
                    mr.getPositions().length > 1 ? 1.0 : 0.5
            );
            scoredList.add(new ScoredResult(doc, mr.getMatches(), mr.getPositions(), breakdown, relevancePct, cosine));
        }

        // Sort candidates deterministically using stable MergeSort
        CustomPriorityQueue.CustomComparator<ScoredResult> sortComp;
        if ("alphabetical".equalsIgnoreCase(sortBy)) {
            sortComp = new CustomPriorityQueue.CustomComparator<ScoredResult>() {
                @Override
                public int compare(ScoredResult a, ScoredResult b) {
                    return a.doc.getTitle().compareToIgnoreCase(b.doc.getTitle());
                }
            };
        } else if ("newest".equalsIgnoreCase(sortBy)) {
            sortComp = new CustomPriorityQueue.CustomComparator<ScoredResult>() {
                @Override
                public int compare(ScoredResult a, ScoredResult b) {
                    return b.doc.getDate().compareTo(a.doc.getDate());
                }
            };
        } else {
            // relevance (default)
            sortComp = new CustomPriorityQueue.CustomComparator<ScoredResult>() {
                @Override
                public int compare(ScoredResult a, ScoredResult b) {
                    int c = Integer.compare(b.relevancePct, a.relevancePct);
                    if (c != 0) return c;
                    int cosC = Double.compare(b.cosineSimilarity, a.cosineSimilarity);
                    if (cosC != 0) return cosC;
                    int mc = Integer.compare(b.matchCount, a.matchCount);
                    if (mc != 0) return mc;
                    int tc = a.doc.getTitle().compareToIgnoreCase(b.doc.getTitle());
                    if (tc != 0) return tc;
                    return Integer.compare(a.doc.getId(), b.doc.getId());
                }
            };
        }

        MergeSort.sort(scoredList, sortComp);

        // Paginate results
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(scoredList.size(), startIndex + pageSize);

        CustomArrayList<SearchResultItem> resultPage = new CustomArrayList<>();
        if (startIndex >= 0 && startIndex < scoredList.size()) {
            for (int i = startIndex; i < endIndex; i++) {
                ScoredResult sr = scoredList.get(i);
                String snippet = SnippetExtractor.extractSnippet(
                        sr.doc.getContent(),
                        query.getQueryTerms(),
                        sr.matchPositions
                );

                SearchResultItem item = new SearchResultItem(
                        sr.doc,
                        sr.matchCount,
                        sr.relevancePct,
                        query.getResolvedAlgorithmDisplayName(),
                        snippet,
                        sr.breakdown,
                        sr.matchPositions
                );
                resultPage.add(item);
            }
        }

        return resultPage;
    }

    public int countMatchingResults(CustomArrayList<SearchDispatcher.MatchResult> matchResults) {
        int count = 0;
        for (int i = 0; i < matchResults.size(); i++) {
            SearchDispatcher.MatchResult mr = matchResults.get(i);
            if ((mr.getMatches() > 0 || mr.isTitleMatched()) && (mr.getTermCoverage() > 0.0 || mr.getTitleScore() > 0.0)) {
                count++;
            }
        }
        return count;
    }

    public int countTotalMatches(CustomArrayList<SearchDispatcher.MatchResult> matchResults) {
        int total = 0;
        for (int i = 0; i < matchResults.size(); i++) {
            total += matchResults.get(i).getMatches();
        }
        return total;
    }
}
