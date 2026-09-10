package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EngineComponentsTest {

    // =========================================================
    // Section 1 — Document Model
    // =========================================================

    @Nested
    class DocumentTest {

        @Test
        public void testBasicFields() {
            Document doc = new Document(1, "Title", "Content here.");
            assertEquals(1,        doc.getId());
            assertEquals("Title",  doc.getTitle());
            assertEquals("Content here.", doc.getContent());
        }

        @Test
        public void testZeroId() {
            Document doc = new Document(0, "", "");
            assertEquals(0, doc.getId());
            assertEquals("", doc.getTitle());
            assertEquals("", doc.getContent());
        }

        @Test
        public void testNegativeIdThrows() {
            assertThrows(IllegalArgumentException.class, () -> new Document(-1, "T", "C"));
        }

        @Test
        public void testNullTitleThrows() {
            assertThrows(IllegalArgumentException.class, () -> new Document(0, null, "C"));
        }

        @Test
        public void testNullContentThrows() {
            assertThrows(IllegalArgumentException.class, () -> new Document(0, "T", null));
        }
    }

    // =========================================================
    // Section 2 — TextNormalizer
    // =========================================================

    @Nested
    class TextNormalizerTest {

        private TextNormalizer norm;

        @BeforeEach
        public void setUp() { norm = new TextNormalizer(); }

        @Test
        public void testNull()  { assertEquals("", norm.normalize(null)); }

        @Test
        public void testEmpty() { assertEquals("", norm.normalize("")); }

        @Test
        public void testLowercase() {
            assertEquals("hello world", norm.normalize("Hello World"));
        }

        @Test
        public void testPunctuationRemoved() {
            assertEquals("hello world", norm.normalize("hello, world!"));
        }

        @Test
        public void testPunctuationSplitsWords() {
            // "end.start" -> punctuation replaced by space -> "end start"
            assertEquals("end start", norm.normalize("end.start"));
        }

        @Test
        public void testWhitespaceCollapsed() {
            assertEquals("a b c", norm.normalize("a   b\t\tc\n"));
        }

        @Test
        public void testLeadingAndTrailingSpacesTrimmed() {
            assertEquals("hello", norm.normalize("   hello   "));
        }

        @Test
        public void testDigitsPreserved() {
            assertEquals("data2023", norm.normalize("Data2023"));
        }

        @Test
        public void testOnlyPunctuation() {
            assertEquals("", norm.normalize("!!!...???"));
        }

        @Test
        public void testMixedContent() {
            assertEquals("the quick brown fox jumps over the lazy dog",
                    norm.normalize("The quick, brown fox!! jumps over the lazy dog."));
        }

        @Test
        public void testHyphenatedWord() {
            // "state-of-the-art" -> three hyphens each become a space
            assertEquals("state of the art", norm.normalize("state-of-the-art"));
        }

        @Test
        public void testIdempotent() {
            String first  = norm.normalize("Hello, World!");
            String second = norm.normalize(first);
            assertEquals(first, second);
        }
    }

    // =========================================================
    // Section 3 — Tokenizer
    // =========================================================

    @Nested
    class TokenizerTest {

        private Tokenizer tok;

        @BeforeEach
        public void setUp() { tok = new Tokenizer(); }

        @Test
        public void testNull()  { assertEquals(0, tok.tokenize(null).size()); }

        @Test
        public void testEmpty() { assertEquals(0, tok.tokenize("").size()); }

        @Test
        public void testSingleToken() {
            CustomArrayList<String> t = tok.tokenize("hello");
            assertEquals(1, t.size());
            assertEquals("hello", t.get(0));
        }

        @Test
        public void testMultipleTokens() {
            CustomArrayList<String> t = tok.tokenize("the quick brown fox");
            assertEquals(4, t.size());
            assertEquals("the",   t.get(0));
            assertEquals("quick", t.get(1));
            assertEquals("brown", t.get(2));
            assertEquals("fox",   t.get(3));
        }

        @Test
        public void testReturnsOrderedTokens() {
            CustomArrayList<String> t = tok.tokenize("a b c d e");
            for (int i = 0; i < t.size() - 1; i++) {
                assertEquals(1, t.get(i).length()); // single chars
            }
            assertEquals("a", t.get(0));
            assertEquals("e", t.get(4));
        }

        @Test
        public void testRepeatedTokens() {
            CustomArrayList<String> t = tok.tokenize("cat cat cat");
            assertEquals(3, t.size());
            for (int i = 0; i < 3; i++) assertEquals("cat", t.get(i));
        }
    }

    // =========================================================
    // Section 4 — InvertedIndex
    // =========================================================

    @Nested
    class InvertedIndexTest {

        private InvertedIndex idx;

        @BeforeEach
        public void setUp() { idx = new InvertedIndex(); }

        @Test
        public void testEmptyDocument() {
            idx.indexDocument(new Document(0, "T", ""));
            assertEquals(0, idx.vocabularySize());
        }

        @Test
        public void testSingleTerm() {
            idx.indexDocument(new Document(0, "T", "hello"));
            assertTrue(idx.containsTerm("hello"));
            assertEquals(1, idx.getPostings("hello").size());
            assertEquals(0, idx.getPostings("hello").get(0));
        }

        @Test
        public void testMissingTerm() {
            idx.indexDocument(new Document(0, "T", "hello world"));
            assertFalse(idx.containsTerm("java"));
            assertTrue(idx.getPostings("java").isEmpty());
        }

        @Test
        public void testPunctuationInQuery() {
            // Index normalizes content; query lookup should also normalize
            idx.indexDocument(new Document(0, "T", "hello, world!"));
            assertTrue(idx.containsTerm("hello"));
            assertTrue(idx.containsTerm("world"));
        }

        @Test
        public void testCaseInsensitiveLookup() {
            idx.indexDocument(new Document(0, "T", "Hello World"));
            // query in any case should match
            assertTrue(idx.containsTerm("HELLO"));
            assertTrue(idx.containsTerm("hello"));
            assertTrue(idx.containsTerm("Hello"));
        }

        @Test
        public void testRepeatedTermSameDocOnce() {
            // "cat cat cat" - doc 0 should appear only ONCE in posting list
            idx.indexDocument(new Document(0, "T", "cat cat cat"));
            CustomArrayList<Integer> postings = idx.getPostings("cat");
            assertEquals(1, postings.size());
            assertEquals(0, postings.get(0));
        }

        @Test
        public void testMultipleDocuments() {
            idx.indexDocument(new Document(0, "T", "the cat sat"));
            idx.indexDocument(new Document(1, "T", "the dog ran"));
            idx.indexDocument(new Document(2, "T", "a cat ran fast"));

            // "the" appears in docs 0 and 1
            CustomArrayList<Integer> thePostings = idx.getPostings("the");
            assertEquals(2, thePostings.size());
            assertTrue(contains(thePostings, 0));
            assertTrue(contains(thePostings, 1));

            // "cat" appears in docs 0 and 2
            CustomArrayList<Integer> catPostings = idx.getPostings("cat");
            assertEquals(2, catPostings.size());
            assertTrue(contains(catPostings, 0));
            assertTrue(contains(catPostings, 2));

            // "ran" appears in docs 1 and 2
            CustomArrayList<Integer> ranPostings = idx.getPostings("ran");
            assertEquals(2, ranPostings.size());
            assertTrue(contains(ranPostings, 1));
            assertTrue(contains(ranPostings, 2));

            // "sat" appears only in doc 0
            CustomArrayList<Integer> satPostings = idx.getPostings("sat");
            assertEquals(1, satPostings.size());
            assertEquals(0, satPostings.get(0));
        }

        @Test
        public void testVocabularySize() {
            idx.indexDocument(new Document(0, "T", "cat sat on the mat"));
            // distinct terms: cat, sat, on, the, mat = 5
            assertEquals(5, idx.vocabularySize());
        }

        @Test
        public void testWhitespaceOnlyDocument() {
            idx.indexDocument(new Document(0, "T", "   \t  \n  "));
            assertEquals(0, idx.vocabularySize());
        }

        @Test
        public void testNullDocumentThrows() {
            assertThrows(IllegalArgumentException.class, () -> idx.indexDocument(null));
        }
    }

    // =========================================================
    // Section 5 — PositionalInvertedIndex
    // =========================================================

    @Nested
    class PositionalIndexTest {

        private PositionalInvertedIndex pidx;

        @BeforeEach
        public void setUp() { pidx = new PositionalInvertedIndex(); }

        @Test
        public void testEmptyDocument() {
            pidx.indexDocument(new Document(0, "T", ""));
            assertEquals(0, pidx.vocabularySize());
        }

        @Test
        public void testSingleTermPosition() {
            pidx.indexDocument(new Document(0, "T", "hello"));
            CustomArrayList<Integer> pos = pidx.getPositions("hello", 0);
            assertEquals(1, pos.size());
            assertEquals(0, pos.get(0)); // position 0
        }

        @Test
        public void testMultipleTermPositions() {
            // "the quick brown fox" -> the:0, quick:1, brown:2, fox:3
            pidx.indexDocument(new Document(0, "T", "the quick brown fox"));
            assertEquals(0, pidx.getPositions("the",   0).get(0));
            assertEquals(1, pidx.getPositions("quick", 0).get(0));
            assertEquals(2, pidx.getPositions("brown", 0).get(0));
            assertEquals(3, pidx.getPositions("fox",   0).get(0));
        }

        @Test
        public void testRepeatedTermPositions() {
            // "cat sat on a cat mat cat" -> cat at positions 0, 4, 6
            pidx.indexDocument(new Document(0, "T", "cat sat on a cat mat cat"));
            CustomArrayList<Integer> pos = pidx.getPositions("cat", 0);
            assertEquals(3, pos.size());
            assertEquals(0, pos.get(0));
            assertEquals(4, pos.get(1));
            assertEquals(6, pos.get(2));
        }

        @Test
        public void testPositionsAcrossMultipleDocuments() {
            pidx.indexDocument(new Document(0, "T", "cat sat on mat"));
            pidx.indexDocument(new Document(1, "T", "the cat"));
            pidx.indexDocument(new Document(2, "T", "no match here"));

            // "cat" in doc 0 at position 0
            CustomArrayList<Integer> pos0 = pidx.getPositions("cat", 0);
            assertEquals(1, pos0.size());
            assertEquals(0, pos0.get(0));

            // "cat" in doc 1 at position 1
            CustomArrayList<Integer> pos1 = pidx.getPositions("cat", 1);
            assertEquals(1, pos1.size());
            assertEquals(1, pos1.get(0));

            // "cat" not in doc 2
            assertTrue(pidx.getPositions("cat", 2).isEmpty());
        }

        @Test
        public void testPostingsListFromPositionalIndex() {
            pidx.indexDocument(new Document(0, "T", "cat sat"));
            pidx.indexDocument(new Document(1, "T", "sat mat"));
            CustomArrayList<Integer> postings = pidx.getPostings("sat");
            assertEquals(2, postings.size());
            assertTrue(contains(postings, 0));
            assertTrue(contains(postings, 1));
        }

        @Test
        public void testMissingTermReturnsEmpty() {
            pidx.indexDocument(new Document(0, "T", "cat sat"));
            assertTrue(pidx.getPositions("dog", 0).isEmpty());
            assertTrue(pidx.getPositions("cat", 99).isEmpty());
        }

        @Test
        public void testPunctuationNormalized() {
            // Punctuation in content is stripped before indexing
            pidx.indexDocument(new Document(0, "T", "hello, world!"));
            assertFalse(pidx.getPositions("hello", 0).isEmpty());
            assertFalse(pidx.getPositions("world", 0).isEmpty());
        }

        @Test
        public void testCaseInsensitivePositions() {
            pidx.indexDocument(new Document(0, "T", "Java is GREAT"));
            // All should be normalized to lowercase
            assertFalse(pidx.getPositions("java",  0).isEmpty());
            assertFalse(pidx.getPositions("great", 0).isEmpty());
            assertEquals(0, pidx.getPositions("java",  0).get(0));
            assertEquals(2, pidx.getPositions("great", 0).get(0));
        }

        @Test
        public void testVocabularySize() {
            pidx.indexDocument(new Document(0, "T", "cat sat on the mat"));
            assertEquals(5, pidx.vocabularySize());
        }

        @Test
        public void testNullDocumentThrows() {
            assertThrows(IllegalArgumentException.class, () -> pidx.indexDocument(null));
        }

        @Test
        public void testNullTermReturnsEmpty() {
            pidx.indexDocument(new Document(0, "T", "hello"));
            assertTrue(pidx.getPositions(null, 0).isEmpty());
            assertTrue(pidx.getPostings(null).isEmpty());
        }
    }

    // =========================================================
    // Section 6 — Pipeline integration (normalize → tokenize → index)
    // =========================================================

    @Nested
    class PipelineIntegrationTest {

        @Test
        public void testFullPipelineNormalizesBeforeIndexing() {
            InvertedIndex idx = new InvertedIndex();
            // Raw content with uppercase, punctuation, extra whitespace
            idx.indexDocument(new Document(0, "T", "The   Quick, BROWN Fox!"));
            // All four words should be reachable in lowercase
            assertTrue(idx.containsTerm("the"));
            assertTrue(idx.containsTerm("quick"));
            assertTrue(idx.containsTerm("brown"));
            assertTrue(idx.containsTerm("fox"));
            assertEquals(4, idx.vocabularySize());
        }

        @Test
        public void testPositionalPipelinePositionsAfterNormalization() {
            PositionalInvertedIndex pidx = new PositionalInvertedIndex();
            pidx.indexDocument(new Document(0, "T", "Hello, World! Hello."));
            // After normalization: "hello world hello"
            // positions: hello -> [0, 2], world -> [1]
            CustomArrayList<Integer> hPos = pidx.getPositions("hello", 0);
            CustomArrayList<Integer> wPos = pidx.getPositions("world", 0);
            assertEquals(2, hPos.size());
            assertEquals(0, hPos.get(0));
            assertEquals(2, hPos.get(1));
            assertEquals(1, wPos.size());
            assertEquals(1, wPos.get(0));
        }

        @Test
        public void testIndexAndPositionalIndexAgreeOnPostings() {
            Document d0 = new Document(0, "D0", "the cat sat on the mat");
            Document d1 = new Document(1, "D1", "the dog lay on the rug");

            InvertedIndex      ii  = new InvertedIndex();
            PositionalInvertedIndex pii = new PositionalInvertedIndex();
            ii.indexDocument(d0);  ii.indexDocument(d1);
            pii.indexDocument(d0); pii.indexDocument(d1);

            // Both indexes must agree on which docs contain "the"
            CustomArrayList<Integer> iiPost  = ii.getPostings("the");
            CustomArrayList<Integer> piiPost = pii.getPostings("the");
            assertEquals(iiPost.size(), piiPost.size());
            for (int i = 0; i < iiPost.size(); i++) {
                assertTrue(contains(piiPost, iiPost.get(i)));
            }
        }
    }

    // =========================================================
    // Utility
    // =========================================================

    private boolean contains(CustomArrayList<Integer> list, int val) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == val) return true;
        }
        return false;
    }
}