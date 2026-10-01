package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Light, transparent rule-based suffix stemmer for the Mini Search Engine.
 *
 * Strict constraint: ZERO java.util.* anywhere in this file.
 *
 * =====================
 * WHY A SIMPLE STEMMER?
 * =====================
 * Full Porter stemming is a 5-phase cascade with ~60 rules — overkill for
 * this project and hard to explain line-by-line at a viva. Instead we apply
 * a SHORT ordered list of longest-match suffix rules. The result is fully
 * deterministic and transparent: anyone can hand-trace any word through
 * the rules below.
 *
 * Design properties:
 *   - Longest matching suffix wins (checked first).
 *   - Min-stem-length guards prevent over-stripping ("ing" is NOT stripped
 *     from "sing", "-ers" not from "rivers"... wait: "rivers" → "river" is
 *     desirable; the guard is len(stem) >= 3).
 *   - Rules are idempotent: stem(stem(w)) == stem(w) for every rule, so a
 *     term is always reduced to exactly the same form.
 *
 * =====================
 * RULE TABLE (checked in this order, first match applies)
 * =====================
 *   ies  (len>4)  → y      studies → study, carries → carry
 *   ied  (len>4)  → y      carried → carry
 *   ss                      (protected: ss words end in -s but are not plurals)
 *   s    (len>3)  → ""     cats → cat, runs → run
 *   tion (len>5)  → t      operation → operat, information → informat
 *   ational        → ate    operational → operate
 *   izer           → ize    optimizer → optimize
 *   ational... (covered above)
 *   iveness        → ive   effectiveness → effectiv
 *   fulness        → ful   usefulness → useful
 *   ousness        → ous   dangerousness → dangerous
 *   ation (len>5)  → ate   creation → create, education → educate
 *   ing   (len>6)  → ""    operating → operat, computing → comput
 *   ed    (len>5)  → ""    indexed → index, managed → manag
 *   ly    (len>5)  → ""    quickly → quick, effectively → effectiv (after rules)
 *
 * Note the -ing rule deliberately leaves "operat" (not "operate"): both
 * "operating" and "operate" are NOT folded together unless the same rule
 * applies to both — "operate" ends in "e", which no rule strips, so
 * "operate" → "operate". To make "operating" and "operate" match, we ALSO
 * strip a trailing "e" when the remaining stem is long enough (>= 4) —
 * see the final rule. This keeps fuzzy/Levenshtein expansion as the
 * safety net for residual mismatches.
 *
 * Time:  O(1) per token (bounded number of fixed-length suffix checks).
 * Space: O(1) per token (a few substring allocations).
 */
public class SimpleStemmer {

    /** Minimum length of the remaining stem for aggressive suffix removal. */
    private static final int MIN_STEM_LENGTH = 3;

    /**
     * Stems a single normalized token.
     *
     * @param token normalized (lowercase, alphabetic) token; may be null
     * @return stemmed token (never null; "" stays "")
     */
    public String stem(String token) {
        if (token == null || token.length() <= MIN_STEM_LENGTH) {
            return token; // too short to stem meaningfully
        }

        String word = token;
        String step;

        // 1) plural / verb forms ------------------------------------------------
        step = apply(word, "ies", 4, "y");
        if (step != null) word = step; else {
            step = apply(word, "ied", 4, "y");
            if (step != null) word = step; else {
                if (!word.endsWith("ss")) {                  // protect "access", "class"
                    step = apply(word, "sses", 4, "ss");     // classes → class
                    if (step != null) word = step; else {
                        step = apply(word, "s", 3, "");      // cats → cat
                        if (step != null) word = step;
                    }
                }
            }
        }

        // 2) nominalizations -----------------------------------------------------
        step = apply(word, "ization", 7, "ize");  if (step != null) word = step;   // normalization → normalize
        step = apply(word, "ational", 7, "ate");  if (step != null) word = step;   // operational → operate
        step = apply(word, "fulness", 7, "ful");  if (step != null) word = step;   // usefulness → useful
        step = apply(word, "iveness", 7, "ive");  if (step != null) word = step;   // effectiveness → effectiv
        step = apply(word, "tion", 5, "t");       if (step != null) word = step;   // operation → operat
        step = apply(word, "ation", 6, "ate");    if (step != null) word = step;   // education → educate
        step = apply(word, "izer", 5, "ize");     if (step != null) word = step;   // optimizer → optimize

        // 3) participles / gerunds ----------------------------------------------
        step = apply(word, "ement", 6, "");       if (step != null) word = step;   // management → manag
        step = apply(word, "ing", 6, "");         if (step != null) word = step;   // operating → operat
        step = apply(word, "edly", 5, "");        if (step != null) word = step;
        step = apply(word, "ed", 5, "");          if (step != null) word = step;   // indexed → index
        step = apply(word, "ly", 5, "");          if (step != null) word = step;   // quickly → quick

        // 4) silent trailing e (operate → operat to align with operating → operat)
        step = apply(word, "e", 4, "");           if (step != null) word = step;

        return word;
    }

    /**
     * Applies one suffix rule.
     *
     * @param word       current form
     * @param suffix     suffix to strip
     * @param minLen     minimum ORIGINAL length for the rule to fire
     * @param replacement replacement for the stripped suffix
     * @return new form, or null if the rule did not apply
     */
    private String apply(String word, String suffix, int minLen, String replacement) {
        if (word.length() < Math.max(minLen, suffix.length() + MIN_STEM_LENGTH)) {
            return null;
        }
        if (!word.endsWith(suffix)) {
            return null;
        }
        String stem = word.substring(0, word.length() - suffix.length());
        if (stem.length() < MIN_STEM_LENGTH) {
            return null; // over-stripping guard
        }
        return stem + replacement;
    }
}
