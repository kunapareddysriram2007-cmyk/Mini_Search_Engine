package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Splits a normalized string into tokens (terms).
 *
 * Contract:
 *   - Input must already be normalized (lowercased, punctuation removed,
 *     whitespace collapsed) as produced by {@link TextNormalizer}.
 *   - Tokens are separated by single ASCII spaces.
 *   - Empty input returns an empty token list.
 *   - A token of length zero is never added (guards against double-space edge cases).
 *
 * No java.util.* imports; no String.split(). Uses a single pass over the char array.
 *
 * Time:  O(n) where n = length of normalized text.
 * Space: O(n) for token storage.
 */
public class Tokenizer {

    /**
     * Tokenizes the normalized text into individual word tokens.
     *
     * @param normalizedText Already-normalized text (output of {@link TextNormalizer}).
     * @return CustomArrayList<String> of tokens in order of appearance.
     */
    public CustomArrayList<String> tokenize(String normalizedText) {
        CustomArrayList<String> tokens = new CustomArrayList<>();

        if (normalizedText == null || normalizedText.isEmpty()) return tokens;

        char[] chars   = normalizedText.toCharArray();
        int    start   = 0;
        int    len     = chars.length;

        for (int i = 0; i <= len; i++) {
            // A space or end-of-string delimits a token
            if (i == len || chars[i] == ' ') {
                int tokenLen = i - start;
                if (tokenLen > 0) {
                    tokens.add(new String(chars, start, tokenLen));
                }
                start = i + 1;
            }
        }

        return tokens;
    }
}