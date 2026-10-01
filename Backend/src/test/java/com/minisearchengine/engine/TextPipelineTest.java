package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StopWordFilter, SimpleStemmer and TextPipeline.
 * (JUnit uses java.util internally; that is fine — the constraint applies
 * to MAIN algorithm-engine sources, not tests.)
 */
public class TextPipelineTest {

    // =====================================================================
    // StopWordFilter
    // =====================================================================

    @Test
    public void testCommonStopWordsDetected() {
        StopWordFilter f = new StopWordFilter();
        assertTrue(f.isStopWord("the"));
        assertTrue(f.isStopWord("is"));
        assertTrue(f.isStopWord("and"));
        assertTrue(f.isStopWord("of"));
        assertTrue(f.isStopWord("to"));
        assertFalse(f.isStopWord("operating"));
        assertFalse(f.isStopWord("system"));
        assertFalse(f.isStopWord("kernel"));
    }

    @Test
    public void testNullOrEmptyTokensAreStop() {
        StopWordFilter f = new StopWordFilter();
        assertTrue(f.isStopWord(null));
        assertTrue(f.isStopWord(""));
    }

    @Test
    public void testFilterPreservesOrderAndDropsStopWords() {
        StopWordFilter f = new StopWordFilter();
        CustomArrayList<String> tokens = new CustomArrayList<>();
        tokens.add("the"); tokens.add("operating"); tokens.add("system");
        tokens.add("is"); tokens.add("central");

        CustomArrayList<String> kept = f.filter(tokens);
        assertEquals(3, kept.size());
        assertEquals("operating", kept.get(0));
        assertEquals("system", kept.get(1));
        assertEquals("central", kept.get(2));
    }

    @Test
    public void testStopWordTableSize() {
        StopWordFilter f = new StopWordFilter();
        // 41 unique entries in loadDefaults() (set size proves no duplicates)
        assertEquals(41, f.size());
    }

    // =====================================================================
    // SimpleStemmer
    // =====================================================================

    @Test
    public void testPluralRules() {
        SimpleStemmer s = new SimpleStemmer();
        assertEquals("cat", s.stem("cats"));
        assertEquals("class", s.stem("classes"));
        assertEquals("study", s.stem("studies"));
        assertEquals("carry", s.stem("carries"));
        assertEquals("access", s.stem("access")); // ss protected, no -s strip
    }

    @Test
    public void testIngAndEdRules() {
        SimpleStemmer s = new SimpleStemmer();
        assertEquals("operat", s.stem("operating"));
        assertEquals("comput", s.stem("computing"));
        assertEquals("index", s.stem("indexed"));
        assertEquals("manag", s.stem("managed"));
    }

    @Test
    public void testTrailingEAlignment() {
        // "operate" → "operat" so it matches "operating" → "operat"
        SimpleStemmer s = new SimpleStemmer();
        assertEquals("operat", s.stem("operate"));
        assertEquals("operat", s.stem("operating"));
    }

    @Test
    public void testShortWordsUntouched() {
        SimpleStemmer s = new SimpleStemmer();
        assertEquals("cat", s.stem("cat"));
        assertEquals("run", s.stem("runs") != null ? s.stem("runs") : "runs");
    }

    @Test
    public void testIdempotentStemming() {
        SimpleStemmer s = new SimpleStemmer();
        String[] words = {"operating", "systems", "studies", "managed", "quickly", "education"};
        for (String w : words) {
            String once = s.stem(w);
            assertEquals(once, s.stem(once), "stem must be idempotent for " + w);
        }
    }

    @Test
    public void testNullAndEmpty() {
        SimpleStemmer s = new SimpleStemmer();
        assertNull(s.stem(null));
        assertEquals("", s.stem(""));
    }

    // =====================================================================
    // TextPipeline
    // =====================================================================

    @Test
    public void testAnalyzeStemsAndDropsStopWords() {
        TextPipeline p = new TextPipeline();
        CustomArrayList<TextPipeline.Token> tokens =
                p.analyze("The Operating Systems are managing the memory");
        // the→stop, are→stop, the→stop
        // operating→operat, systems→system, managing→manag, memory→memory
        assertEquals(4, tokens.size());
        assertEquals("operat", tokens.get(0).getTerm());
        assertEquals("system", tokens.get(1).getTerm());
        assertEquals("manag", tokens.get(2).getTerm());
        assertEquals("memory", tokens.get(3).getTerm());
    }

    @Test
    public void testPositionsCompactedAfterStopWordRemoval() {
        TextPipeline p = new TextPipeline();
        CustomArrayList<TextPipeline.Token> tokens =
                p.analyze("the cat sat on the mat and the dog ran");
        // the/on/and/the dropped → cat(0) sat(1) mat(2) dog(3) ran(4)
        assertEquals(5, tokens.size());
        assertEquals(0, tokens.get(0).getPosition());
        assertEquals(1, tokens.get(1).getPosition());
        assertEquals(2, tokens.get(2).getPosition());
        assertEquals(3, tokens.get(3).getPosition());
        assertEquals(4, tokens.get(4).getPosition());
    }

    @Test
    public void testOriginalFormPreserved() {
        TextPipeline p = new TextPipeline();
        CustomArrayList<TextPipeline.Token> tokens = p.analyze("indexing documents");
        assertEquals("index", tokens.get(0).getTerm());       // stemmed
        assertEquals("indexing", tokens.get(0).getOriginal()); // pre-stem
    }

    @Test
    public void testTermFrequencies() {
        TextPipeline p = new TextPipeline();
        CustomArrayList<TextPipeline.Token> tokens =
                p.analyze("memory memory management and memory");
        var tf = p.termFrequencies(tokens);
        assertEquals(3, tf.get("memory"));
        assertEquals(1, tf.get("manag"));
        assertEquals(2, tf.size());
    }

    @Test
    public void testEmptyAndNullInput() {
        TextPipeline p = new TextPipeline();
        assertEquals(0, p.analyze("").size());
        assertEquals(0, p.analyze(null).size());
        assertEquals(0, p.analyze("the is of and").size()); // all stop words
    }

    @Test
    public void testPunctuationAndCaseHandled() {
        TextPipeline p = new TextPipeline();
        CustomArrayList<TextPipeline.Token> tokens =
                p.analyze("Operating-System!! Kernels, run PROCESSes.");
        // operating→operat, system→system, kernel→kernel, run→run, process→process
        assertEquals(5, tokens.size());
        assertEquals("operat", tokens.get(0).getTerm());
        assertEquals("system", tokens.get(1).getTerm());
        assertEquals("process", tokens.get(4).getTerm());
    }
}
