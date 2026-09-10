package com.minisearchengine.engine;

/**
 * Text normalization for the Mini Search Engine.
 *
 * Normalization rules (applied in this order):
 *   1. Null input  → treated as empty string.
 *   2. Lowercase   → every character folded to lower case via Character.toLowerCase().
 *   3. Punctuation removal → any character that is NOT a letter, digit, or whitespace
 *                            is replaced with a space. This preserves word boundaries
 *                            across punctuation (e.g. "end.start" → "end start").
 *   4. Whitespace collapsing → one or more consecutive whitespace characters
 *                              (space, tab, newline, carriage-return) are collapsed to
 *                              a single ASCII space ' '.
 *   5. Trim → leading and trailing spaces are removed.
 *
 * Rationale for each rule:
 *   - Lowercasing ensures case-insensitive matching ("The" == "the").
 *   - Punctuation removal splits hyphenated words and strips trailing periods/commas
 *     without silently merging adjacent words.
 *   - Whitespace collapsing makes subsequent tokenization predictable: split on ' '.
 *   - Trim avoids an empty leading/trailing token after split.
 *
 * Characters NOT altered: digits, Unicode letters (handled by Character.isLetter()).
 *
 * No java.util.* imports; no regex. All operations use char[] manipulation.
 */
public class TextNormalizer {

    /**
     * Normalizes the input text according to the documented rules.
     *
     * @param text Raw input string (may be null or empty).
     * @return Normalized string ready for tokenization.
     */
    public String normalize(String text) {
        if (text == null || text.isEmpty()) return "";

        // Step 1: lowercase into a char array
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = Character.toLowerCase(chars[i]);
        }

        // Step 2: replace non-alphanumeric, non-whitespace chars with space
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
                chars[i] = ' ';
            }
        }

        // Step 3: collapse consecutive whitespace + trim via a single pass
        char[] result = new char[chars.length];
        int    out    = 0;
        boolean prevSpace = true; // treat start as space so leading spaces are skipped

        for (int i = 0; i < chars.length; i++) {
            if (Character.isWhitespace(chars[i])) {
                if (!prevSpace) {
                    result[out++] = ' ';
                    prevSpace = true;
                }
            } else {
                result[out++] = chars[i];
                prevSpace = false;
            }
        }

        // Trim trailing space
        if (out > 0 && result[out - 1] == ' ') out--;

        return new String(result, 0, out);
    }
}