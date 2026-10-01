package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.core.ds.CustomHashMap;

/**
 * Unified text-analysis pipeline: the SINGLE place where raw text is turned
 * into indexable terms, used identically for documents (indexing time) and
 * queries (search time) so both sides of the index always agree.
 *
 * Strict constraint: ZERO java.util.* anywhere in this file.
 *
 * =====================
 * PIPELINE STAGES
 * =====================
 *   raw text
 *     → TextNormalizer   (lowercase, punctuation→space, whitespace collapse)
 *     → Tokenizer        (split on single spaces)
 *     → StopWordFilter   (drop ~40 low-information words; positions compacted)
 *     → SimpleStemmer    (transparent suffix stripping)
 *     → Token { term, position }
 *
 * Position policy: after stop-word removal, retained tokens are renumbered
 * 0, 1, 2, ... in order (positions are COMPACTED). Both documents and
 * queries go through the same policy, so phrase/proximity checks in the
 * positional index remain consistent.
 *
 * Time:  O(n) for normalize/tokenize/filter + O(t) stemming for t tokens.
 * Space: O(t) for the token list.
 */
public class TextPipeline {

    /** One analyzed token: the stemmed term plus its compacted position. */
    public static final class Token {
        private final String term;      // stemmed, normalized — the index key
        private final String original;  // normalized but UNstemmed form
        private final int position;     // compacted 0-based token-stream position

        public Token(String term, String original, int position) {
            this.term = term;
            this.original = original;
            this.position = position;
        }

        public String getTerm()     { return term; }
        public String getOriginal() { return original; }
        public int getPosition()    { return position; }

        @Override
        public String toString() {
            return term + "@" + position;
        }
    }

    private final TextNormalizer normalizer;
    private final Tokenizer tokenizer;
    private final StopWordFilter stopWordFilter;
    private final SimpleStemmer stemmer;

    public TextPipeline() {
        this.normalizer = new TextNormalizer();
        this.tokenizer = new Tokenizer();
        this.stopWordFilter = new StopWordFilter();
        this.stemmer = new SimpleStemmer();
    }

    /**
     * Runs the full pipeline over raw text, producing position-tagged terms.
     *
     * @param rawText raw document or query text (may be null/empty)
     * @return list of Tokens (possibly empty)
     */
    public CustomArrayList<Token> analyze(String rawText) {
        CustomArrayList<Token> out = new CustomArrayList<>();
        String normalized = normalizer.normalize(rawText == null ? "" : rawText);
        CustomArrayList<String> raw = tokenizer.tokenize(normalized);

        int position = 0; // compacted position counter
        for (int i = 0; i < raw.size(); i++) {
            String token = raw.get(i);
            if (token == null || token.isEmpty() || stopWordFilter.isStopWord(token)) {
                continue; // stop word: dropped, position NOT consumed
            }
            out.add(new Token(stemmer.stem(token), token, position));
            position++;
        }
        return out;
    }

    /**
     * Convenience: just the stemmed terms, in order.
     *
     * @param rawText raw text
     * @return stemmed term list
     */
    public CustomArrayList<String> tokenize(String rawText) {
        CustomArrayList<Token> tokens = analyze(rawText);
        CustomArrayList<String> terms = new CustomArrayList<>(tokens.size());
        for (int i = 0; i < tokens.size(); i++) {
            terms.add(tokens.get(i).getTerm());
        }
        return terms;
    }

    /**
     * Term frequencies (TF) over an analyzed token list.
     *
     * @param tokens analyzed tokens (must not be null)
     * @return map term → number of occurrences
     */
    public CustomHashMap<String, Integer> termFrequencies(CustomArrayList<Token> tokens) {
        if (tokens == null) {
            throw new IllegalArgumentException("tokens must not be null.");
        }
        CustomHashMap<String, Integer> tf = new CustomHashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            String term = tokens.get(i).getTerm();
            Integer count = tf.get(term);
            tf.put(term, count == null ? 1 : count + 1);
        }
        return tf;
    }

    public TextNormalizer getNormalizer()     { return normalizer; }
    public Tokenizer getTokenizer()           { return tokenizer; }
    public StopWordFilter getStopWordFilter() { return stopWordFilter; }
    public SimpleStemmer getStemmer()         { return stemmer; }
}
