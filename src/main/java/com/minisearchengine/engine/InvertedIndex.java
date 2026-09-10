package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.core.ds.CustomHashMap;

/**
 * Inverted Index: maps every term to the sorted list of document IDs
 * (posting list) that contain that term.
 *
 * Design decisions:
 *   - Backed by {@link CustomHashMap}<String, CustomArrayList<Integer>>.
 *   - Each document ID appears AT MOST ONCE per term's posting list
 *     (duplicate guard inside {@code addPosting}).
 *   - Document IDs are appended in insertion order; callers must index
 *     documents in a consistent, deterministic order for reproducible results.
 *   - The full pipeline (normalize → tokenize → index) is driven by
 *     {@code indexDocument}.
 *
 * No java.util.* imports. No library collections.
 *
 * Time per document index call:
 *   O(t * d)  where t = number of distinct tokens in the document,
 *             d = current size of that term's posting list (duplicate check).
 *   In practice d stays small; each doc ID appears once per term.
 *
 * Space:
 *   O(T * D)  where T = vocabulary size, D = total postings across all terms.
 */
public class InvertedIndex {

    /** term -> list of doc IDs (each ID appears at most once). */
    private final CustomHashMap<String, CustomArrayList<Integer>> index;

    private final TextNormalizer normalizer;
    private final Tokenizer      tokenizer;

    public InvertedIndex() {
        this.index      = new CustomHashMap<>();
        this.normalizer = new TextNormalizer();
        this.tokenizer  = new Tokenizer();
    }

    /**
     * Normalizes, tokenizes, and indexes the document's content.
     * Calling this with the same document twice will add duplicate doc IDs;
     * callers are responsible for indexing each document exactly once.
     *
     * @param document The document to index (must not be null).
     */
    public void indexDocument(Document document) {
        if (document == null) throw new IllegalArgumentException("Document must not be null.");

        String normalized = normalizer.normalize(document.getContent());
        CustomArrayList<String> tokens = tokenizer.tokenize(normalized);

        int docId = document.getId();

        for (int i = 0; i < tokens.size(); i++) {
            String term = tokens.get(i);
            addPosting(term, docId);
        }
    }

    /**
     * Returns the posting list (list of doc IDs) for the given term.
     * Performs normalization on the query term so lookup is case-insensitive
     * and punctuation-tolerant.
     *
     * @param term The query term (raw; will be normalized).
     * @return CustomArrayList<Integer> of matching doc IDs, or empty list if none.
     */
    public CustomArrayList<Integer> getPostings(String term) {
        if (term == null || term.isEmpty()) return new CustomArrayList<>();
        String normalized = normalizer.normalize(term);
        CustomArrayList<Integer> result = index.get(normalized);
        return result != null ? result : new CustomArrayList<>();
    }

    /**
     * Returns true if the term exists in the index.
     *
     * @param term Raw query term (will be normalized).
     */
    public boolean containsTerm(String term) {
        if (term == null || term.isEmpty()) return false;
        return index.containsKey(normalizer.normalize(term));
    }

    /** Returns the total number of distinct terms in the vocabulary. */
    public int vocabularySize() {
        return index.size();
    }

    // ---------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------

    /**
     * Adds docId to the posting list for term.
     * Ensures each docId appears at most once per term.
     */
    private void addPosting(String term, int docId) {
        CustomArrayList<Integer> postings = index.get(term);
        if (postings == null) {
            postings = new CustomArrayList<>();
            index.put(term, postings);
        }
        // Duplicate guard: only add if not already present
        if (!postings.contains(docId)) {
            postings.add(docId);
        }
    }
}