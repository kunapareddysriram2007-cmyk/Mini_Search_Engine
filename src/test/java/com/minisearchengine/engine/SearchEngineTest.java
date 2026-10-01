package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SearchEngineTest {

    private SearchEngine searchEngine;

    @BeforeEach
    public void setUp() throws IOException {
        searchEngine = new SearchEngine();
        searchEngine.buildIndex(new ClasspathCorpusLoader());
    }

    @Test
    public void testIndexBuildSuccess() {
        assertTrue(searchEngine.isIndexed());
        assertTrue(searchEngine.getCorpusDocumentCount() >= 45, "Should have indexed at least 45 Wikipedia documents");
        assertTrue(searchEngine.getTotalTokensCount() > 100);
    }

    @Test
    public void testSmartSearchMode() {
        SearchQuery query = new SearchQuery("operating system", "exact", "smart", "all", "relevance", 1, 10);
        SearchEngineResult result = searchEngine.search(query);

        assertNotNull(result);
        assertEquals("Smart Search (VSM)", result.getAlgorithmUsed());
        assertTrue(result.getResults().size() > 0);
        SearchResultItem top = result.getResults().get(0);
        assertTrue(top.getRelevance() > 0);
        assertNotNull(top.getScoreBreakdown());
        assertTrue(top.getScoreBreakdown().getTfidf() > 0);
    }

    @Test
    public void testExactPatternSearchMode() {
        SearchQuery query = new SearchQuery("Binary Search Tree", "exact", "exact-pattern", "all", "relevance", 1, 10);
        SearchEngineResult result = searchEngine.search(query);

        assertNotNull(result);
        assertEquals("Exact Pattern (KMP)", result.getAlgorithmUsed());
        assertTrue(result.getResults().size() > 0);
        SearchResultItem top = result.getResults().get(0);
        assertEquals("Binary Search Tree", top.getDocument().getTitle());
        assertTrue(top.getMatches() > 0);
        assertTrue(top.getMatchPositions().length > 0, "Exact pattern mode must return match positions");
    }

    @Test
    public void testMultiPatternSearchMode() {
        SearchQuery query = new SearchQuery("binary tree search graph", "partial", "multi-pattern", "all", "relevance", 1, 10);
        SearchEngineResult result = searchEngine.search(query);

        assertNotNull(result);
        assertEquals("Multi-Pattern (Aho-Corasick)", result.getAlgorithmUsed());
        assertTrue(result.getResults().size() > 0);
        SearchResultItem top = result.getResults().get(0);
        assertTrue(top.getMatches() > 0);
    }

    @Test
    public void testFuzzySearchModeWithTypo() {
        SearchQuery query = new SearchQuery("machne learning", "fuzzy", "fuzzy", "all", "relevance", 1, 10);
        SearchEngineResult result = searchEngine.search(query);

        assertNotNull(result);
        assertEquals("Fuzzy Search (Levenshtein)", result.getAlgorithmUsed());
        assertEquals("machine learning", result.getDidYouMean(), "Should suggest corrected spelling");
    }

    @Test
    public void testAlgorithmResolutionDifferentiation() {
        SearchQuery smartQ = new SearchQuery("operating system", "exact", "smart", "all", "relevance", 1, 5);
        SearchQuery exactQ = new SearchQuery("operating system", "exact", "exact-pattern", "all", "relevance", 1, 5);
        SearchQuery multiQ = new SearchQuery("operating system", "partial", "multi-pattern", "all", "relevance", 1, 5);
        SearchQuery fuzzyQ = new SearchQuery("operating system", "fuzzy", "fuzzy", "all", "relevance", 1, 5);

        assertEquals("Smart Search (VSM)", searchEngine.search(smartQ).getAlgorithmUsed());
        assertEquals("Exact Pattern (KMP)", searchEngine.search(exactQ).getAlgorithmUsed());
        assertEquals("Multi-Pattern (Aho-Corasick)", searchEngine.search(multiQ).getAlgorithmUsed());
        assertEquals("Fuzzy Search (Levenshtein)", searchEngine.search(fuzzyQ).getAlgorithmUsed());
    }

    @Test
    public void testRabinKarpSearch() {
        SearchQuery query = new SearchQuery("a", "exact", "rabin-karp", "all", "relevance", 1, 5);
        SearchEngineResult result = searchEngine.search(query);

        assertNotNull(result);
        assertEquals("Rabin-Karp", result.getAlgorithmUsed());
    }

    @Test
    public void testGetDocumentBySlugOrId() {
        CorpusDocument doc0 = searchEngine.getCorpusDocuments().get(0);
        CorpusDocument found = searchEngine.getDocumentBySlugOrId(doc0.getSlugId());
        assertNotNull(found);
        assertEquals(doc0.getTitle(), found.getTitle());
    }

    @Test
    public void testWorkOsDoesNotMatchDocumentContainingOnlyPurpose() {
        // Document whose content contains only "purpose"
        CorpusDocument doc = new CorpusDocument(
                999,
                "purpose-doc",
                "Purpose",
                "corpus/test/purpose-doc.txt",
                "Article",
                "General",
                "purpose",
                "The purpose of this component.",
                new CustomArrayList<>(),
                1,
                7,
                "2024-01-01"
        );

        QueryParser parser = new QueryParser();
        SearchQuery query = new SearchQuery("work os", "partial", "aho-corasick", "all", "relevance", 1, 10);
        QueryParser.ParsedQuery parsed = parser.parse(query);

        SearchDispatcher dispatcher = new SearchDispatcher();
        SearchDispatcher.MatchResult mr = dispatcher.dispatchMatch(doc, parsed);

        // Neither "work" nor "os" is present as a word in "purpose"
        assertEquals(0, mr.getMatches(), "Must not match 'os' as an interior substring of 'purpose'");
        assertEquals(0.0, mr.getTermCoverage(), 0.001, "Term coverage must be 0 when no query terms match");
        assertEquals(0.0, mr.getTitleScore(), 0.001, "Title score must be 0 when title 'Purpose' contains no query terms");
        assertFalse(mr.isPhraseMatched(), "Phrase match must be false");

        // When ranked, relevance must be 0 and candidate must not be returned
        Ranker ranker = new Ranker();
        CustomArrayList<CorpusDocument> candidates = new CustomArrayList<>();
        candidates.add(doc);
        CustomArrayList<SearchDispatcher.MatchResult> matchResults = new CustomArrayList<>();
        matchResults.add(mr);

        CustomArrayList<SearchResultItem> ranked = ranker.rankAndPaginate(candidates, matchResults, parsed, "relevance", 1, 10, 1);
        assertEquals(0, ranked.size(), "Document containing only 'purpose' must not be returned for 'work os'");
        assertEquals(0, ranker.countMatchingResults(matchResults), "Matching count must be 0");
    }

    @Test
    public void testWordBoundaryPreventsSubstringFalsePositives() {
        assertTrue(SearchDispatcher.isWordBoundary("operating system (os) is", 18, 20), "(os) should be recognized at word boundary");
        assertFalse(SearchDispatcher.isWordBoundary("purpose", 4, 6), "'os' inside 'purpose' must not be a word boundary");
        assertFalse(SearchDispatcher.isWordBoundary("network", 3, 7), "'work' inside 'network' must not be a word boundary");
        assertTrue(SearchDispatcher.isWordBoundary("work on os", 0, 4), "'work' at start should be a word boundary");

        assertTrue(SearchDispatcher.containsWord("operating system (os)", "os"));
        assertFalse(SearchDispatcher.containsWord("purpose", "os"));
        assertFalse(SearchDispatcher.containsWord("network", "work"));
    }

    @Test
    public void testCriticalCorrectnessRuleLexicalRelevanceZeroWhenNoTermsMatch() {
        CorpusDocument unrelatedDoc = new CorpusDocument(
                998,
                "unrelated",
                "Database Indexing",
                "corpus/test/indexing.txt",
                "Article",
                "General",
                "B-tree and hash index structures.",
                "Overview",
                new CustomArrayList<>(),
                5,
                33,
                "2024-01-01"
        );

        QueryParser parser = new QueryParser();
        SearchQuery query = new SearchQuery("quantum entanglement", "partial", "aho-corasick", "all", "relevance", 1, 10);
        QueryParser.ParsedQuery parsed = parser.parse(query);

        SearchDispatcher dispatcher = new SearchDispatcher();
        SearchDispatcher.MatchResult mr = dispatcher.dispatchMatch(unrelatedDoc, parsed);

        Ranker ranker = new Ranker();
        CustomArrayList<CorpusDocument> candidates = new CustomArrayList<>();
        candidates.add(unrelatedDoc);
        CustomArrayList<SearchDispatcher.MatchResult> matchResults = new CustomArrayList<>();
        matchResults.add(mr);

        CustomArrayList<SearchResultItem> ranked = ranker.rankAndPaginate(candidates, matchResults, parsed, "relevance", 1, 10, 1);
        assertEquals(0, ranked.size(), "Document with none of the query terms must never receive non-zero score");
    }

    private CorpusDocument createTestDoc(int id, String title, String content) {
        return new CorpusDocument(
                id,
                "doc-" + id,
                title,
                "corpus/test/" + id + ".txt",
                "Article",
                "General",
                content,
                "Overview of " + title,
                new CustomArrayList<>(),
                content.isEmpty() ? 0 : content.split("\\s+").length,
                content.length(),
                "2024-01-01"
        );
    }

    @Test
    public void testDocsABCDRankingForWorkOs() {
        // Specifically requested test scenario:
        // Doc A: "work operating system"
        // Doc B: "operating systems and computer architecture"
        // Doc C: "purpose of education"
        // Doc D: "workplace operating system"
        CorpusDocument docA = createTestDoc(1, "Doc A", "work operating system");
        CorpusDocument docB = createTestDoc(2, "Doc B", "operating systems and computer architecture");
        CorpusDocument docC = createTestDoc(3, "Doc C", "purpose of education");
        CorpusDocument docD = createTestDoc(4, "Doc D", "workplace operating system");

        InvertedIndex index = new InvertedIndex();
        index.indexDocument(docA.toDocument());
        index.indexDocument(docB.toDocument());
        index.indexDocument(docC.toDocument());
        index.indexDocument(docD.toDocument());

        Ranker ranker = new Ranker(index);
        QueryParser parser = new QueryParser();
        SearchQuery query = new SearchQuery("work os", "partial", "aho-corasick", "all", "relevance", 1, 10);
        QueryParser.ParsedQuery parsed = parser.parse(query);

        // 1. Direct mathematical verification of Cosine Similarity
        double cosineA = ranker.computeCosineSimilarity(parsed.getQueryTerms(), docA.getContent(), index, 4);
        double cosineB = ranker.computeCosineSimilarity(parsed.getQueryTerms(), docB.getContent(), index, 4);
        double cosineC = ranker.computeCosineSimilarity(parsed.getQueryTerms(), docC.getContent(), index, 4);
        double cosineD = ranker.computeCosineSimilarity(parsed.getQueryTerms(), docD.getContent(), index, 4);

        // Doc A: overlap on "work". ln(1 + 4/1) = ln(5) ≈ 1.6094. ||q|| = 1.6094, ||dA|| ≈ 2.1249.
        // cosineA = 1.6094 / 2.1249 ≈ 0.7574.
        assertEquals(0.7574, cosineA, 0.005, "Doc A cosine similarity should be approx 0.7574");

        // Zero query-term overlap must yield strictly 0.0 cosine similarity
        assertEquals(0.0, cosineB, 0.0001, "Doc B has zero query-term overlap; cosine must be 0.0");
        assertEquals(0.0, cosineC, 0.0001, "Doc C has zero query-term overlap; cosine must be 0.0");
        assertEquals(0.0, cosineD, 0.0001, "Doc D ('workplace' != 'work') has zero query-term overlap; cosine must be 0.0");

        // 2. Full Ranker pipeline verification
        SearchDispatcher dispatcher = new SearchDispatcher();
        CustomArrayList<CorpusDocument> candidates = new CustomArrayList<>();
        candidates.add(docA);
        candidates.add(docB);
        candidates.add(docC);
        candidates.add(docD);

        CustomArrayList<SearchDispatcher.MatchResult> matchResults = new CustomArrayList<>();
        matchResults.add(dispatcher.dispatchMatch(docA, parsed));
        matchResults.add(dispatcher.dispatchMatch(docB, parsed));
        matchResults.add(dispatcher.dispatchMatch(docC, parsed));
        matchResults.add(dispatcher.dispatchMatch(docD, parsed));

        CustomArrayList<SearchResultItem> ranked = ranker.rankAndPaginate(
                candidates, matchResults, parsed, "relevance", 1, 10, 4, index
        );

        // Only Doc A must be returned; Docs B, C, D must be eliminated
        assertEquals(1, ranked.size(), "Only Doc A should be returned as a relevant candidate");
        SearchResultItem resultA = ranked.get(0);
        assertEquals("Doc A", resultA.getDocument().getTitle());
        assertEquals(76, resultA.getRelevance(), "Relevance percentage should be rounded cosine: 76%");
    }

    @Test
    public void testExactOneWordMatch() {
        CorpusDocument docA = createTestDoc(1, "Doc A", "work operating system");
        CorpusDocument docB = createTestDoc(2, "Doc B", "operating systems and computer architecture");

        InvertedIndex index = new InvertedIndex();
        index.indexDocument(docA.toDocument());
        index.indexDocument(docB.toDocument());

        Ranker ranker = new Ranker(index);
        QueryParser parser = new QueryParser();
        QueryParser.ParsedQuery parsed = parser.parse(new SearchQuery("work", "exact", "kmp", "all", "relevance", 1, 10));

        double cosineA = ranker.computeCosineSimilarity(parsed.getQueryTerms(), docA.getContent(), index, 2);
        double cosineB = ranker.computeCosineSimilarity(parsed.getQueryTerms(), docB.getContent(), index, 2);

        assertTrue(cosineA > 0.0, "Doc A matching 'work' must have positive cosine similarity");
        assertEquals(0.0, cosineB, 0.0001, "Doc B without 'work' must have 0.0 cosine similarity");
    }

    @Test
    public void testMultiWordQueryRanking() {
        CorpusDocument docA = createTestDoc(1, "Doc A", "work operating system");
        CorpusDocument docB = createTestDoc(2, "Doc B", "operating systems and computer architecture");

        InvertedIndex index = new InvertedIndex();
        index.indexDocument(docA.toDocument());
        index.indexDocument(docB.toDocument());

        Ranker ranker = new Ranker(index);
        QueryParser parser = new QueryParser();
        QueryParser.ParsedQuery parsed = parser.parse(new SearchQuery("work operating", "partial", "aho-corasick", "all", "relevance", 1, 10));

        double cosineA = ranker.computeCosineSimilarity(parsed.getQueryTerms(), docA.getContent(), index, 2);
        double cosineB = ranker.computeCosineSimilarity(parsed.getQueryTerms(), docB.getContent(), index, 2);

        // Doc A matches both terms ("work", "operating"); Doc B matches only one ("operating")
        assertTrue(cosineA > cosineB, "Doc matching 2 terms must rank higher than doc matching 1 term");
        assertTrue(cosineB > 0.0, "Doc B matching 'operating' must have positive cosine similarity");
    }

    @Test
    public void testRepeatedQueryTerms() {
        CorpusDocument docA = createTestDoc(1, "Doc A", "work operating system");

        InvertedIndex index = new InvertedIndex();
        index.indexDocument(docA.toDocument());

        Ranker ranker = new Ranker(index);
        QueryParser parser = new QueryParser();
        QueryParser.ParsedQuery parsed = parser.parse(new SearchQuery("work work os", "partial", "aho-corasick", "all", "relevance", 1, 10));

        double cosine = ranker.computeCosineSimilarity(parsed.getQueryTerms(), docA.getContent(), index, 1);
        assertTrue(cosine > 0.0 && cosine <= 1.0, "Cosine similarity must remain strictly in (0.0, 1.0] with repeated query terms");
    }

    @Test
    public void testEmptyQueryEdgeCase() {
        Ranker ranker = new Ranker();
        QueryParser parser = new QueryParser();
        QueryParser.ParsedQuery parsed = parser.parse(new SearchQuery("", "exact", "kmp", "all", "relevance", 1, 10));

        double cosine = ranker.computeCosineSimilarity(parsed.getQueryTerms(), "work operating system", null, 1);
        assertEquals(0.0, cosine, 0.0001, "Empty query must produce 0.0 cosine similarity");
    }

    @Test
    public void testUnknownWordsEdgeCase() {
        CorpusDocument docA = createTestDoc(1, "Doc A", "work operating system");
        InvertedIndex index = new InvertedIndex();
        index.indexDocument(docA.toDocument());

        Ranker ranker = new Ranker(index);
        QueryParser parser = new QueryParser();
        QueryParser.ParsedQuery parsed = parser.parse(new SearchQuery("nonexistenttermxyz", "exact", "kmp", "all", "relevance", 1, 10));

        double cosine = ranker.computeCosineSimilarity(parsed.getQueryTerms(), docA.getContent(), index, 1);
        assertEquals(0.0, cosine, 0.0001, "Unknown term not in index must yield 0.0 cosine similarity without division-by-zero");
    }

    @Test
    public void testEmptyDocumentTextEdgeCase() {
        Ranker ranker = new Ranker();
        QueryParser parser = new QueryParser();
        QueryParser.ParsedQuery parsed = parser.parse(new SearchQuery("work os", "exact", "kmp", "all", "relevance", 1, 10));

        double cosine = ranker.computeCosineSimilarity(parsed.getQueryTerms(), "", null, 1);
        assertEquals(0.0, cosine, 0.0001, "Empty document text must yield 0.0 cosine similarity without division-by-zero");
    }

    @Test
    public void testAllZeroVectorEdgeCase() {
        Ranker ranker = new Ranker();
        QueryParser parser = new QueryParser();
        QueryParser.ParsedQuery parsed = parser.parse(new SearchQuery("unknownterm", "exact", "kmp", "all", "relevance", 1, 10));

        double cosine = ranker.computeCosineSimilarity(parsed.getQueryTerms(), "", null, 0);
        assertEquals(0.0, cosine, 0.0001, "All zero vectors must safely yield 0.0 without ArithmeticException");
    }

    @Test
    public void testCaseDifferencesEquivalence() {
        CorpusDocument docA = createTestDoc(1, "Doc A", "work operating system");
        InvertedIndex index = new InvertedIndex();
        index.indexDocument(docA.toDocument());

        Ranker ranker = new Ranker(index);
        QueryParser parser = new QueryParser();

        QueryParser.ParsedQuery parsedLower = parser.parse(new SearchQuery("work os", "exact", "kmp", "all", "relevance", 1, 10));
        QueryParser.ParsedQuery parsedUpper = parser.parse(new SearchQuery("WORK OS", "exact", "kmp", "all", "relevance", 1, 10));
        QueryParser.ParsedQuery parsedMixed = parser.parse(new SearchQuery("Work Os", "exact", "kmp", "all", "relevance", 1, 10));

        double cosLower = ranker.computeCosineSimilarity(parsedLower.getQueryTerms(), docA.getContent(), index, 1);
        double cosUpper = ranker.computeCosineSimilarity(parsedUpper.getQueryTerms(), docA.getContent(), index, 1);
        double cosMixed = ranker.computeCosineSimilarity(parsedMixed.getQueryTerms(), docA.getContent(), index, 1);

        assertEquals(cosLower, cosUpper, 0.0001, "Uppercase query must produce identical score to lowercase query");
        assertEquals(cosLower, cosMixed, 0.0001, "Mixed case query must produce identical score to lowercase query");
    }

    @Test
    public void testPunctuationDifferencesEquivalence() {
        CorpusDocument docA = createTestDoc(1, "Doc A", "work operating system");
        InvertedIndex index = new InvertedIndex();
        index.indexDocument(docA.toDocument());

        Ranker ranker = new Ranker(index);
        QueryParser parser = new QueryParser();

        QueryParser.ParsedQuery parsedClean = parser.parse(new SearchQuery("work os", "exact", "kmp", "all", "relevance", 1, 10));
        QueryParser.ParsedQuery parsedPunct = parser.parse(new SearchQuery("work, os!", "exact", "kmp", "all", "relevance", 1, 10));

        double cosClean = ranker.computeCosineSimilarity(parsedClean.getQueryTerms(), docA.getContent(), index, 1);
        double cosPunct = ranker.computeCosineSimilarity(parsedPunct.getQueryTerms(), docA.getContent(), index, 1);

        assertEquals(cosClean, cosPunct, 0.0001, "Punctuation in query must be normalized away to produce identical score");
    }

    @Test
    public void testDeterministicTieBreakingWithMergeSort() {
        // Two documents with identical relevance and match count:
        // docX (Title "Alpha", id 10) vs docY (Title "Beta", id 20)
        CorpusDocument docAlpha = createTestDoc(10, "Alpha", "work operating system");
        CorpusDocument docBeta = createTestDoc(20, "Beta", "work operating system");

        InvertedIndex index = new InvertedIndex();
        index.indexDocument(docAlpha.toDocument());
        index.indexDocument(docBeta.toDocument());

        Ranker ranker = new Ranker(index);
        QueryParser parser = new QueryParser();
        QueryParser.ParsedQuery parsed = parser.parse(new SearchQuery("work", "exact", "kmp", "all", "relevance", 1, 10));

        SearchDispatcher dispatcher = new SearchDispatcher();
        CustomArrayList<CorpusDocument> candidates = new CustomArrayList<>();
        // Add Beta first to verify sort orders Alpha first alphabetically
        candidates.add(docBeta);
        candidates.add(docAlpha);

        CustomArrayList<SearchDispatcher.MatchResult> matchResults = new CustomArrayList<>();
        matchResults.add(dispatcher.dispatchMatch(docBeta, parsed));
        matchResults.add(dispatcher.dispatchMatch(docAlpha, parsed));

        CustomArrayList<SearchResultItem> ranked = ranker.rankAndPaginate(
                candidates, matchResults, parsed, "relevance", 1, 10, 2, index
        );

        assertEquals(2, ranked.size());
        assertEquals("Alpha", ranked.get(0).getDocument().getTitle(), "Ties must be broken deterministically by title");
        assertEquals("Beta", ranked.get(1).getDocument().getTitle());
    }

    @Test
    public void testExplainableSearchScoreBreakdownCalculations() {
        CorpusDocument docA = createTestDoc(1, "Doc A", "machine learning is great for machine intelligence");
        CorpusDocument docB = createTestDoc(2, "Doc B", "deep learning algorithms in neural networks");

        InvertedIndex index = new InvertedIndex();
        index.indexDocument(docA.toDocument());
        index.indexDocument(docB.toDocument());

        Ranker ranker = new Ranker(index);
        QueryParser parser = new QueryParser();
        QueryParser.ParsedQuery parsed = parser.parse(new SearchQuery("machine learning", "exact", "smart", "all", "relevance", 1, 10));

        SearchDispatcher dispatcher = new SearchDispatcher();
        CustomArrayList<CorpusDocument> candidates = new CustomArrayList<>();
        candidates.add(docA);
        candidates.add(docB);

        CustomArrayList<SearchDispatcher.MatchResult> matchResults = new CustomArrayList<>();
        matchResults.add(dispatcher.dispatchMatch(docA, parsed));
        matchResults.add(dispatcher.dispatchMatch(docB, parsed));

        CustomArrayList<SearchResultItem> ranked = ranker.rankAndPaginate(
                candidates, matchResults, parsed, "relevance", 1, 10, 2, index
        );

        assertTrue(ranked.size() >= 2);

        // Check Doc A: contains "machine" (tf=2) and "learning" (tf=1)
        SearchResultItem resA = null;
        SearchResultItem resB = null;
        for (int i = 0; i < ranked.size(); i++) {
            SearchResultItem item = ranked.get(i);
            if (item.getDocument().getTitle().equals("Doc A")) resA = item;
            if (item.getDocument().getTitle().equals("Doc B")) resB = item;
        }

        assertNotNull(resA);
        assertNotNull(resB);

        ScoreBreakdown sbA = resA.getScoreBreakdown();
        assertNotNull(sbA, "ScoreBreakdown must be generated for Smart Search result");
        assertEquals(2, sbA.getQueryTerms().size());
        assertEquals(2, sbA.getMatchedTerms().size());
        assertEquals(2, sbA.getTermFrequencies().get("machine"));
        assertEquals(1, sbA.getTermFrequencies().get("learning"));
        assertTrue(sbA.getCosineSimilarity() > 0.0);
        assertEquals((int) Math.round(sbA.getCosineSimilarity() * 100), sbA.getFinalRelevancePct());
        assertNotNull(sbA.getTfidfContributions().get("machine"));
        assertNotNull(sbA.getTfidfContributions().get("learning"));

        // Check Doc B: contains "learning" (tf=1), but NOT "machine" (tf=0)
        ScoreBreakdown sbB = resB.getScoreBreakdown();
        assertNotNull(sbB);
        assertEquals(2, sbB.getQueryTerms().size());
        assertEquals(1, sbB.getMatchedTerms().size());
        assertEquals("learning", sbB.getMatchedTerms().get(0));
        assertEquals(0, sbB.getTermFrequencies().get("machine"));
        assertEquals(1, sbB.getTermFrequencies().get("learning"));
        assertTrue(sbB.getCosineSimilarity() > 0.0);
        assertTrue(sbA.getCosineSimilarity() > sbB.getCosineSimilarity(), "Doc A with 2 matching terms must have higher cosine similarity than Doc B with 1 term");
    }

    @Test
    public void testMetadataFilteringAndAdvancedSearch() throws Exception {
        SearchEngine engine = new SearchEngine();
        engine.buildIndex(new ClasspathCorpusLoader());

        // 1. Filter by category
        SearchQuery csQuery = new SearchQuery(
                "operating", "exact", "smart", "all", "computer-science", "all", null, null, null, "relevance", 1, 10
        );
        SearchEngineResult csResult = engine.search(csQuery);
        assertTrue(csResult.getTotalResults() > 0);
        for (int i = 0; i < csResult.getResults().size(); i++) {
            assertEquals("computer-science", csResult.getResults().get(i).getDocument().getCategory());
        }

        // 2. Filter by language
        SearchQuery langQuery = new SearchQuery(
                "адыгэ", "exact", "smart", "all", "all", "Adyghe", null, null, null, "relevance", 1, 10
        );
        SearchEngineResult langResult = engine.search(langQuery);
        assertTrue(langResult.getTotalResults() > 0);
        for (int i = 0; i < langResult.getResults().size(); i++) {
            assertEquals("Adyghe", langResult.getResults().get(i).getDocument().getLanguage());
        }

        // 3. Advanced search: Exclude term (-process)
        CustomArrayList<String> exclList = new CustomArrayList<>();
        exclList.add("process");
        SearchQuery queryWithExclusion = new SearchQuery(
                "operating system", "exact", "smart", "all", "all", "all", null, null,
                exclList, "relevance", 1, 10
        );

        SearchEngineResult exclResult = engine.search(queryWithExclusion);
        for (int i = 0; i < exclResult.getResults().size(); i++) {
            SearchResultItem item = exclResult.getResults().get(i);
            assertFalse(item.getDocument().getContent().toLowerCase().contains("process")
                    || item.getDocument().getTitle().toLowerCase().contains("process"),
                    "Excluded term must not appear in result candidates");
        }

        // 4. Advanced search: Exact phrase
        SearchQuery exactPhraseQuery = new SearchQuery(
                "memory management", "exact", "smart", "all", "all", "all", "virtual memory", null, null, "relevance", 1, 10
        );
        SearchEngineResult phraseResult = engine.search(exactPhraseQuery);
        for (int i = 0; i < phraseResult.getResults().size(); i++) {
            SearchResultItem item = phraseResult.getResults().get(i);
            assertTrue(item.getDocument().getContent().toLowerCase().contains("virtual memory")
                    || item.getDocument().getTitle().toLowerCase().contains("virtual memory"),
                    "Exact phrase must be present in every returned candidate");
        }
    }

    @Test
    public void testAlgorithmExecutionDetailsAcrossModes() {
        // 1. Smart Search mode
        SearchQuery smartQ = new SearchQuery("machine learning", "exact", "smart", "all", "relevance", 1, 10);
        SearchEngineResult smartRes = searchEngine.search(smartQ);
        AlgorithmExecutionDetails smartDetails = smartRes.getAlgorithmExecutionDetails();
        assertNotNull(smartDetails);
        assertEquals("TF-IDF + Cosine Similarity", smartDetails.getAlgorithmName());
        assertEquals("smart", smartDetails.getAlgorithmFamily());
        assertEquals(searchEngine.getCorpusDocumentCount(), smartDetails.getDocumentsIndexed());
        assertTrue(smartDetails.getCandidateDocuments() > 0);
        assertTrue(smartDetails.getResultsReturned() > 0);

        // 2. KMP Exact Pattern mode
        SearchQuery kmpQ = new SearchQuery("algorithm", "exact", "exact-pattern", "all", "relevance", 1, 10);
        SearchEngineResult kmpRes = searchEngine.search(kmpQ);
        AlgorithmExecutionDetails kmpDetails = kmpRes.getAlgorithmExecutionDetails();
        assertNotNull(kmpDetails);
        assertEquals("KMP", kmpDetails.getAlgorithmName());
        assertEquals("kmp", kmpDetails.getAlgorithmFamily());
        assertEquals("algorithm", kmpDetails.getPattern());
        assertTrue(kmpDetails.getTotalOccurrences() > 0);
        assertTrue(kmpDetails.getComparisons() > 0, "KMP must track real character comparisons");

        // 3. Aho-Corasick Multi-Pattern mode
        SearchQuery ahoQ = new SearchQuery("machine, learning, algorithm", "partial", "multi-pattern", "all", "relevance", 1, 10);
        SearchEngineResult ahoRes = searchEngine.search(ahoQ);
        AlgorithmExecutionDetails ahoDetails = ahoRes.getAlgorithmExecutionDetails();
        assertNotNull(ahoDetails);
        assertEquals("Aho-Corasick", ahoDetails.getAlgorithmName());
        assertEquals("aho-corasick", ahoDetails.getAlgorithmFamily());
        assertTrue(ahoDetails.getPatterns().size() >= 2);
        assertTrue(ahoDetails.getAutomatonStates() > 0, "Aho-Corasick must track trie automaton states");

        // 4. Fuzzy Levenshtein mode
        SearchQuery fuzzyQ = new SearchQuery("machne", "fuzzy", "fuzzy", "all", "relevance", 1, 10);
        SearchEngineResult fuzzyRes = searchEngine.search(fuzzyQ);
        AlgorithmExecutionDetails fuzzyDetails = fuzzyRes.getAlgorithmExecutionDetails();
        assertNotNull(fuzzyDetails);
        assertEquals("Edit Distance", fuzzyDetails.getAlgorithmName());
        assertEquals("edit-distance", fuzzyDetails.getAlgorithmFamily());
        assertEquals("machne", fuzzyDetails.getOriginalQuery());
        assertEquals("machine", fuzzyDetails.getSuggestedTerm());
        assertEquals(1, fuzzyDetails.getEditDistance());
    }

    @Test
    public void testWorkOsRankingCorrectness() throws Exception {
        SearchEngine testEngine = new SearchEngine();
        testEngine.buildIndex(new CorpusLoader() {
            @Override
            public com.minisearchengine.core.ds.CustomArrayList<CorpusDocument> loadCorpus() {
                com.minisearchengine.core.ds.CustomArrayList<CorpusDocument> list = new com.minisearchengine.core.ds.CustomArrayList<>();
                list.add(new CorpusDocument(1, "work-operating-system", "Work Operating System", "corpus/work-os.txt", "Article", "os", "English",
                        "This document focuses on work operating system architectures, kernel processes, and memory management.", "Work OS Overview", new com.minisearchengine.core.ds.CustomArrayList<>(), 50, 200, "2024-01-01"));
                list.add(new CorpusDocument(2, "operating-systems", "Operating Systems", "corpus/os.txt", "Article", "os", "English",
                        "Modern operating systems manage hardware resources, process scheduling, and virtual memory.", "OS Overview", new com.minisearchengine.core.ds.CustomArrayList<>(), 50, 200, "2024-01-01"));
                list.add(new CorpusDocument(3, "purpose-only", "General Purpose", "corpus/purpose.txt", "Article", "general", "English",
                        "The sole purpose of this document is testing non-matching terms and text verification.", "Purpose Overview", new com.minisearchengine.core.ds.CustomArrayList<>(), 50, 200, "2024-01-01"));
                list.add(new CorpusDocument(4, "botany-photosynthesis", "Photosynthesis in Plants", "corpus/botany.txt", "Article", "science", "English",
                        "Photosynthesis is a biological process used by plants to convert light energy into chemical energy.", "Botany Overview", new com.minisearchengine.core.ds.CustomArrayList<>(), 50, 200, "2024-01-01"));
                return list;
            }
        });

        // Search for "work os"
        SearchQuery query = new SearchQuery("work os", "partial", "smart", "all", "relevance", 1, 10);
        SearchEngineResult res = testEngine.search(query);

        // Verify that "work operating system" and "operating systems" rank at top
        assertTrue(res.getTotalResults() >= 1, "Should match documents containing work / operating systems");
        SearchResultItem top = res.getResults().get(0);
        assertEquals("work-operating-system", top.getDocument().getSlugId(), "Top result must be work operating system");
        assertTrue(top.getRelevance() > 0, "Top result relevance must be > 0");

        // Verify that document with ONLY "purpose" is NEVER returned with artificial relevance
        for (int i = 0; i < res.getResults().size(); i++) {
            SearchResultItem item = res.getResults().get(i);
            assertNotEquals("purpose-only", item.getDocument().getSlugId(), "Document containing only 'purpose' must not be returned for 'work os'");
            assertNotEquals("botany-photosynthesis", item.getDocument().getSlugId(), "Unrelated botany document must not be returned");
        }

        // Directly verify Ranker cosine similarity for "purpose" document is exactly 0.0
        Ranker ranker = new Ranker(testEngine.getInvertedIndex());
        com.minisearchengine.core.ds.CustomArrayList<String> queryTerms = new com.minisearchengine.core.ds.CustomArrayList<>();
        queryTerms.add("work");
        queryTerms.add("os");
        double purposeCosine = ranker.computeCosineSimilarity(queryTerms, "The sole purpose of this document is testing non-matching terms.", testEngine.getInvertedIndex(), 4);
        assertEquals(0.0, purposeCosine, 1e-6, "Cosine similarity for 'purpose' must be strictly 0.0");
    }

    @Test
    public void testEdgeCasesAndRobustness() {
        // 1. Null query
        SearchQuery nullQ = new SearchQuery(null, "exact", "smart", "all", "relevance", 1, 10);
        SearchEngineResult nullRes = searchEngine.search(nullQ);
        assertEquals(0, nullRes.getResults().size());

        // 2. Empty query
        SearchQuery emptyQ = new SearchQuery("", "exact", "smart", "all", "relevance", 1, 10);
        SearchEngineResult emptyRes = searchEngine.search(emptyQ);
        assertEquals(0, emptyRes.getResults().size());

        // 3. Whitespace only
        SearchQuery spaceQ = new SearchQuery("     ", "exact", "smart", "all", "relevance", 1, 10);
        SearchEngineResult spaceRes = searchEngine.search(spaceQ);
        assertEquals(0, spaceRes.getResults().size());

        // 4. Punctuation only
        SearchQuery punctQ = new SearchQuery("!@#$%^&*()_+=-~`{}[]|;:',.<>?/", "exact", "smart", "all", "relevance", 1, 10);
        SearchEngineResult punctRes = searchEngine.search(punctQ);
        assertEquals(0, punctRes.getResults().size());

        // 5. Extremely large query
        StringBuilder longQuery = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            longQuery.append("algorithm operating system memory database network ");
        }
        SearchQuery largeQ = new SearchQuery(longQuery.toString(), "partial", "smart", "all", "relevance", 1, 10);
        SearchEngineResult largeRes = searchEngine.search(largeQ);
        assertNotNull(largeRes);
        assertTrue(largeRes.getResults().size() > 0);
    }
}


