package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.core.ds.CustomHashMap;

/**
 * Positional Inverted Index: maps every term to a per-document list of
 * token positions at which that term appears.
 *
 * Structure:
 *   term  ->  CustomHashMap< docId, CustomArrayList<Integer positions> >
 *
 * Positions are 0-based token offsets within the document's token stream
 * (i.e. position 0 is the first token, position 1 is the second, etc.).
 *
 * This extends the plain {@link InvertedIndex} semantics by also recording
 * WHERE in each document a term occurs, enabling:
 *   - phrase query support (later module)
 *   - proximity ranking
 *   - passage retrieval
 *
 * Design decisions:
 *   - The outer map key is the normalized term.
 *   - The inner map key is the document ID (int, boxed as Integer).
 *   - A term appearing k times in a document accumulates k position entries.
 *   - Normalization and tokenization are handled internally (same rules as
 *     {@link InvertedIndex}); no raw text ever enters the maps directly.
 *
 * No java.util.* imports. No library collections.
 *
 * Space: O(N)  where N = total number of term occurrences across all documents.
 * Time per indexDocument call: O(t)  where t = token count of the document.
 */
public class PositionalInvertedIndex {

    /**
     * term -> { docId -> [pos0, pos1, ...] }
     */
    private final CustomHashMap<String, CustomHashMap<Integer, CustomArrayList<Integer>>> index;

    private final TextNormalizer normalizer;
    private final Tokenizer      tokenizer;

    public PositionalInvertedIndex() {
        this.index      = new CustomHashMap<>();
        this.normalizer = new TextNormalizer();
        this.tokenizer  = new Tokenizer();
    }

    /**
     * Normalizes, tokenizes, and positionally indexes the document's content.
     *
     * @param document The document to index (must not be null).
     */
    public void indexDocument(Document document) {
        if (document == null) throw new IllegalArgumentException("Document must not be null.");

        String normalized = normalizer.normalize(document.getContent());
        CustomArrayList<String> tokens = tokenizer.tokenize(normalized);

        int docId = document.getId();

        for (int pos = 0; pos < tokens.size(); pos++) {
            String term = tokens.get(pos);
            addPosition(term, docId, pos);
        }
    }

    /**
     * Returns the list of token positions for the given term in the specified document.
     * The term is normalized before lookup.
     *
     * @param term  Raw query term.
     * @param docId Document ID.
     * @return CustomArrayList<Integer> of 0-based positions, or empty list if not found.
     */
    public CustomArrayList<Integer> getPositions(String term, int docId) {
        if (term == null || term.isEmpty()) return new CustomArrayList<>();
        CustomHashMap<Integer, CustomArrayList<Integer>> docMap =
                index.get(normalizer.normalize(term));
        if (docMap == null) return new CustomArrayList<>();
        CustomArrayList<Integer> positions = docMap.get(docId);
        return positions != null ? positions : new CustomArrayList<>();
    }

    /**
     * Returns the posting list (distinct doc IDs) for the given term.
     * Consistent with InvertedIndex semantics.
     *
     * @param term Raw query term (will be normalized).
     * @return CustomArrayList<Integer> of doc IDs that contain the term.
     */
    public CustomArrayList<Integer> getPostings(String term) {
        if (term == null || term.isEmpty()) return new CustomArrayList<>();
        CustomHashMap<Integer, CustomArrayList<Integer>> docMap =
                index.get(normalizer.normalize(term));
        if (docMap == null) return new CustomArrayList<>();
        return docMap.keyList();
    }

    /**
     * Returns true if the term exists in the index in any document.
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

    private void addPosition(String term, int docId, int position) {
        CustomHashMap<Integer, CustomArrayList<Integer>> docMap = index.get(term);
        if (docMap == null) {
            docMap = new CustomHashMap<>();
            index.put(term, docMap);
        }
        CustomArrayList<Integer> positions = docMap.get(docId);
        if (positions == null) {
            positions = new CustomArrayList<>();
            docMap.put(docId, positions);
        }
        positions.add(position);
    }
}