package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.core.ds.CustomHashSet;

/**
 * Stop-word filter for the Mini Search Engine.
 *
 * Strict constraint: ZERO java.util.* anywhere in this file.
 *
 * =====================
 * WHAT ARE STOP WORDS?
 * =====================
 * Extremely frequent, low-information words ("the", "is", "at", "of", ...)
 * that appear in nearly every document. Indexing them inflates the index
 * and adds noise to ranking (their IDF is near zero anyway). Removing them:
 *   - shrinks the index,
 *   - speeds up candidate generation,
 *   - slightly improves ranking precision.
 *
 * The list (~40 common English stop words) is deliberately SMALL and
 * transparent: for a course project, every filtering decision must be
 * explainable. Note that words like "system" are NOT stop words even
 * though they are common in this corpus — corpus-specific terms carry
 * real information here.
 *
 * =====================
 * USAGE / CONTRACT
 * =====================
 * - {@link #isStopWord(String)}: pure lookup on the ALREADY-NORMALIZED
 *   token (lowercase, no punctuation) — O(1) average via CustomHashSet.
 * - {@link #filter(CustomArrayList)}: returns a NEW list containing only
 *   non-stop tokens, preserving order (positions in the token stream are
 *   NOT compacted here — the indexing stage decides its own position
 *   policy, see TextPipeline).
 *
 * Time:  O(t) to filter t tokens (each lookup O(1) average).
 * Space: O(k) for the stop-word table (k ≈ 40) + O(t) for the output list.
 */
public class StopWordFilter {

    /** Immutable-at-runtime lookup table of English stop words. */
    private final CustomHashSet<String> stopWords;

    public StopWordFilter() {
        stopWords = new CustomHashSet<>(64);
        loadDefaults();
    }

    /**
     * Decides whether the given (normalized) token is a stop word.
     *
     * @param token normalized token (expected lowercase, punctuation-free)
     * @return true if the token should be discarded from the index
     */
    public boolean isStopWord(String token) {
        if (token == null || token.isEmpty()) {
            return true; // null/empty tokens carry no information
        }
        return stopWords.contains(token);
    }

    /**
     * Filters a token list, keeping order, dropping stop words
     * (and null/blank entries).
     *
     * @param tokens normalized token list (must not be null)
     * @return new CustomArrayList with the retained tokens
     */
    public CustomArrayList<String> filter(CustomArrayList<String> tokens) {
        if (tokens == null) {
            throw new IllegalArgumentException("tokens must not be null.");
        }
        CustomArrayList<String> kept = new CustomArrayList<>(tokens.size());
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token != null && !token.isEmpty() && !stopWords.contains(token)) {
                kept.add(token);
            }
        }
        return kept;
    }

    /** Number of stop words in the table (for statistics/logging). */
    public int size() {
        return stopWords.size();
    }

    /** Loads the default English stop-word list. */
    private void loadDefaults() {
        String[] words = {
            "a", "an", "and", "are", "as", "at", "be", "but", "by",
            "for", "from", "has", "have", "he", "her", "his", "i",
            "in", "is", "it", "its", "of", "on", "or", "she", "that",
            "the", "their", "them", "then", "there", "these", "they",
            "this", "to", "was", "we", "were", "which", "will", "with"
        };
        for (int i = 0; i < words.length; i++) {
            stopWords.add(words[i]);
        }
    }
}
