package com.minisearchengine;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.core.string.AhoCorasick;
import com.minisearchengine.core.string.KMPStringMatcher;
import com.minisearchengine.core.string.MultiPatternMatcher;
import com.minisearchengine.core.string.NaiveStringMatcher;
import com.minisearchengine.core.string.RabinKarpMatcher;
import com.minisearchengine.core.string.SuffixArrayBuilder;
import com.minisearchengine.core.string.ZAlgorithmMatcher;
import com.minisearchengine.engine.Document;
import com.minisearchengine.engine.InvertedIndex;
import com.minisearchengine.engine.PositionalInvertedIndex;
import com.minisearchengine.engine.TextNormalizer;
import com.minisearchengine.engine.Tokenizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Interactive CLI demonstration of the Mini Search Engine.
 *
 * Uses ONLY existing public APIs — no invented methods.
 * Zero java.util.* in algorithm engine code.
 * This file is the presentation entry point only.
 *
 * Run:  mvn exec:java -Dexec.mainClass="com.minisearchengine.Main"
 */
public class Main {

    // --- presentation constants -----------------------------------------
    private static final String LINE  =
            "========================================";
    private static final String THICK =
            "========================================" +
            "========================================";

    // --- sample corpus ---------------------------------------------------
    private static final Document[] CORPUS = {
        new Document(0, "Intro to Algorithms",
            "Algorithms and data structures are the backbone of computer science. " +
            "Efficient algorithms reduce time complexity significantly."),
        new Document(1, "String Matching Techniques",
            "KMP algorithm and Z algorithm both achieve linear time string matching. " +
            "Rabin-Karp uses a rolling hash for fast pattern search."),
        new Document(2, "Search Engines Overview",
            "A search engine uses an inverted index to map each term to matching documents. " +
            "Positional indexing further records token positions for phrase queries."),
        new Document(3, "Graph Algorithms",
            "Graph algorithms include BFS, DFS, Dijkstra, and Ford-Fulkerson. " +
            "Network flow algorithms solve many real-world optimization problems."),
        new Document(4, "Randomized Algorithms",
            "Randomized algorithms like randomized quicksort and Miller-Rabin " +
            "provide expected efficiency guarantees through probabilistic methods.")
    };

    // --- string algorithm demo text -------------------------------------
    private static final String SA_TEXT    = "algorithms and data structures in computer science";
    private static final String SA_PATTERN = "algorithm";

    public static void main(String[] args) throws IOException {
        println(THICK);
        println("           MINI SEARCH ENGINE DEMO");
        println("     TextHack — DSA Course Project Demo");
        println(THICK);

        TextNormalizer  normalizer = new TextNormalizer();
        Tokenizer       tokenizer  = new Tokenizer();
        InvertedIndex   idx        = new InvertedIndex();
        PositionalInvertedIndex pidx = new PositionalInvertedIndex();

        // ================================================================
        // SECTION 1 — DOCUMENT PROCESSING
        // ================================================================
        section("1", "DOCUMENT PROCESSING");

        println("  Loaded " + CORPUS.length + " documents:");
        for (Document doc : CORPUS) {
            println("    [Doc " + doc.getId() + "] " + doc.getTitle());
        }

        println("");
        println("  Normalization & Tokenization Pipeline:");
        for (Document doc : CORPUS) {
            String norm   = normalizer.normalize(doc.getContent());
            CustomArrayList<String> toks = tokenizer.tokenize(norm);
            println("    [Doc " + doc.getId() + "] " + toks.size()
                    + " tokens  |  sample: \"" + firstTokens(toks, 5) + "\"");
        }

        // ================================================================
        // SECTION 2 — INDEXING
        // ================================================================
        section("2", "INDEXING");

        println("  Building inverted index ...");
        for (Document doc : CORPUS) idx.indexDocument(doc);
        println("  Vocabulary size (InvertedIndex): " + idx.vocabularySize() + " distinct terms");

        println("");
        println("  Building positional inverted index ...");
        for (Document doc : CORPUS) pidx.indexDocument(doc);
        println("  Vocabulary size (PositionalIndex): " + pidx.vocabularySize() + " distinct terms");

        println("");
        println("  Sample term posting lists:");
        demoPosting(idx, "algorithm",   CORPUS);
        demoPosting(idx, "index",       CORPUS);
        demoPosting(idx, "search",      CORPUS);
        demoPosting(idx, "hash",        CORPUS);
        demoPosting(idx, "graph",       CORPUS);

        // ================================================================
        // SECTION 3 — POSITIONAL DEMO
        // ================================================================
        section("3", "POSITIONAL INDEX DEMO");

        String[] demoTerms = {"algorithm", "search", "index", "hash"};
        for (String term : demoTerms) {
            println("  Term: \"" + term + "\"");
            for (Document doc : CORPUS) {
                CustomArrayList<Integer> positions = pidx.getPositions(term, doc.getId());
                if (!positions.isEmpty()) {
                    println("    -> Doc " + doc.getId() + " [" + doc.getTitle()
                            + "]  positions: " + listToString(positions));
                }
            }
        }

        // ================================================================
        // SECTION 4 — STRING ALGORITHMS DEMO
        // ================================================================
        section("4", "STRING ALGORITHMS DEMO");

        println("  Text    : \"" + SA_TEXT + "\"");
        println("  Pattern : \"" + SA_PATTERN + "\"");
        println("");

        // Naive
        NaiveStringMatcher naive = new NaiveStringMatcher();
        CustomArrayList<Integer> naiveRes = naive.search(SA_TEXT, SA_PATTERN);
        println("  Naive String Match   : " + matchSummary(naiveRes, SA_PATTERN));

        // KMP
        KMPStringMatcher kmp = new KMPStringMatcher();
        CustomArrayList<Integer> kmpRes = kmp.search(SA_TEXT, SA_PATTERN);
        int[] lps = kmp.buildLPS(SA_PATTERN);
        println("  KMP (LPS=" + arrayToString(lps) + "): " + matchSummary(kmpRes, SA_PATTERN));

        // Z Algorithm
        ZAlgorithmMatcher zAlgo = new ZAlgorithmMatcher();
        CustomArrayList<Integer> zRes = zAlgo.search(SA_TEXT, SA_PATTERN);
        println("  Z Algorithm          : " + matchSummary(zRes, SA_PATTERN));

        // Rabin-Karp
        RabinKarpMatcher rk = new RabinKarpMatcher();
        CustomArrayList<Integer> rkRes = rk.search(SA_TEXT, SA_PATTERN);
        println("  Rabin-Karp           : " + matchSummary(rkRes, SA_PATTERN));

        // Aho-Corasick (multi-pattern)
        println("");
        println("  Aho-Corasick (multi-pattern search in: \"" + SA_TEXT + "\")");
        CustomArrayList<String> acPatterns = new CustomArrayList<>();
        acPatterns.add("algorithm");
        acPatterns.add("data");
        acPatterns.add("science");
        acPatterns.add("xyz");
        AhoCorasick ac = new AhoCorasick(acPatterns);
        CustomArrayList<MultiPatternMatcher.Match> acMatches = ac.searchAll(SA_TEXT);
        if (acMatches.isEmpty()) {
            println("    No matches found.");
        } else {
            for (int i = 0; i < acMatches.size(); i++) {
                MultiPatternMatcher.Match m = acMatches.get(i);
                println("    Pattern \"" + m.getPattern()
                        + "\" found at index " + m.getStartIndex());
            }
        }

        // Suffix Array
        println("");
        println("  Suffix Array + Kasai LCP for: \"" + SA_TEXT + "\"");
        SuffixArrayBuilder sab = new SuffixArrayBuilder(SA_TEXT);
        int[] sa  = sab.getSuffixArray();
        int[] lcp = sab.getLcpArray();
        println("    SA  (first 10): " + partialArray(sa,  10));
        println("    LCP (first 10): " + partialArray(lcp, 10));
        CustomArrayList<Integer> saSearch = sab.search(SA_PATTERN);
        println("    Searching for \"" + SA_PATTERN + "\" via SA binary search: "
                + matchSummary(saSearch, SA_PATTERN));

        // ================================================================
        // SECTION 5 — INTERACTIVE SEARCH
        // ================================================================
        section("5", "INTERACTIVE SEARCH");
        println("  Searching the inverted index.");
        println("  Type a search term and press Enter.");
        println("  Type 'exit' to quit.\n");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("  Query> ");
            System.out.flush();
            String line = reader.readLine();
            if (line == null || line.trim().equalsIgnoreCase("exit")) break;
            String query = line.trim();
            if (query.isEmpty()) continue;

            println("");
            if (!idx.containsTerm(query)) {
                println("  No documents found for: \"" + query + "\"");
            } else {
                CustomArrayList<Integer> postings = idx.getPostings(query);
                println("  Term \"" + query + "\" found in "
                        + postings.size() + " document(s):");
                for (int i = 0; i < postings.size(); i++) {
                    int docId = postings.get(i);
                    Document doc = findDoc(docId);
                    CustomArrayList<Integer> positions = pidx.getPositions(query, docId);
                    println("    -> Doc " + docId + ": \"" + doc.getTitle() + "\""
                            + "  |  positions: " + listToString(positions));
                }
            }
            println("");
        }

        // ================================================================
        // DONE
        // ================================================================
        println("");
        println(THICK);
        println("              Demo completed successfully.");
        println(THICK);
    }

    // ----------------------------------------------------------------
    // Helpers — no java.util.* used
    // ----------------------------------------------------------------

    private static void section(String num, String title) {
        println("");
        println(LINE);
        println("  [" + num + "] " + title);
        println(LINE);
    }

    private static void println(String s) {
        System.out.println(s);
    }

    /** Returns first n tokens as a space-separated preview string. */
    private static String firstTokens(CustomArrayList<String> tokens, int n) {
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(n, tokens.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) sb.append(' ');
            sb.append(tokens.get(i));
        }
        if (tokens.size() > n) sb.append(" ...");
        return sb.toString();
    }

    /** Prints posting list with document titles. */
    private static void demoPosting(InvertedIndex idx, String term, Document[] corpus) {
        CustomArrayList<Integer> postings = idx.getPostings(term);
        if (postings.isEmpty()) {
            println("    \"" + term + "\" -> (not found)");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("    \"").append(term).append("\" -> Docs [");
        for (int i = 0; i < postings.size(); i++) {
            if (i > 0) sb.append(", ");
            int docId = postings.get(i);
            sb.append(docId).append(":\"").append(corpus[docId].getTitle()).append("\"");
        }
        sb.append(']');
        println(sb.toString());
    }

    private static String matchSummary(CustomArrayList<Integer> indices, String pattern) {
        if (indices.isEmpty()) return "not found";
        StringBuilder sb = new StringBuilder();
        sb.append("found ").append(indices.size()).append(" match(es) at index(es) ");
        for (int i = 0; i < indices.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(indices.get(i));
        }
        return sb.toString();
    }

    private static String listToString(CustomArrayList<Integer> list) {
        if (list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    private static String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(arr[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    private static String partialArray(int[] arr, int n) {
        StringBuilder sb = new StringBuilder("[");
        int limit = Math.min(n, arr.length);
        for (int i = 0; i < limit; i++) {
            if (i > 0) sb.append(", ");
            sb.append(arr[i]);
        }
        if (arr.length > n) sb.append(", ...");
        sb.append(']');
        return sb.toString();
    }

    private static Document findDoc(int id) {
        for (Document doc : CORPUS) {
            if (doc.getId() == id) return doc;
        }
        return new Document(id, "Unknown", "");
    }
}