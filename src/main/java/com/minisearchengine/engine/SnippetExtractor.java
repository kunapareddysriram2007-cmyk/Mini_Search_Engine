package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Utility for extracting text snippet windows centered around match positions.
 * Preserves exact original character casing for UI highlighting.
 */
public final class SnippetExtractor {

    private static final int DEFAULT_SNIPPET_LENGTH = 160;

    private SnippetExtractor() {}

    /**
     * Extracts a snippet centered around the first match or query terms.
     */
    public static String extractSnippet(String content, CustomArrayList<String> queryTerms, int[] matchPositions) {
        if (content == null || content.isEmpty()) return "";
        if (content.length() <= DEFAULT_SNIPPET_LENGTH) return content;

        int firstMatchIndex = -1;
        if (matchPositions != null && matchPositions.length > 0) {
            firstMatchIndex = matchPositions[0];
        } else if (queryTerms != null && queryTerms.size() > 0) {
            String lowerContent = content.toLowerCase();
            for (int i = 0; i < queryTerms.size(); i++) {
                String term = queryTerms.get(i).toLowerCase();
                if (term.length() > 2) {
                    int pos = lowerContent.indexOf(term);
                    if (pos >= 0 && (firstMatchIndex == -1 || pos < firstMatchIndex)) {
                        firstMatchIndex = pos;
                    }
                }
            }
        }

        if (firstMatchIndex < 0) firstMatchIndex = 0;

        int half = DEFAULT_SNIPPET_LENGTH / 2;
        int start = Math.max(0, firstMatchIndex - half);
        int end = Math.min(content.length(), start + DEFAULT_SNIPPET_LENGTH);

        if (end - start < DEFAULT_SNIPPET_LENGTH && start > 0) {
            start = Math.max(0, end - DEFAULT_SNIPPET_LENGTH);
        }

        // Adjust to word boundaries if possible
        if (start > 0) {
            int space = content.indexOf(' ', start);
            if (space >= 0 && space < start + 15) {
                start = space + 1;
            }
        }
        if (end < content.length()) {
            int space = content.lastIndexOf(' ', end);
            if (space > end - 15 && space > start) {
                end = space;
            }
        }

        StringBuilder sb = new StringBuilder();
        if (start > 0) sb.append("…");
        sb.append(content.substring(start, end).trim());
        if (end < content.length()) sb.append("…");

        return sb.toString();
    }
}
