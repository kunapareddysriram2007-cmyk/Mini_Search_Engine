package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ClasspathCorpusLoaderTest {

    @Test
    public void testLoadCorpusSuccess() throws IOException {
        ClasspathCorpusLoader loader = new ClasspathCorpusLoader();
        CustomArrayList<CorpusDocument> docs = loader.loadCorpus();

        assertNotNull(docs, "Loaded documents must not be null");
        assertTrue(docs.size() >= 45, "Should load at least 45 corpus documents from Wikipedia datasets");

        // Verify first document metadata
        CorpusDocument doc0 = docs.get(0);
        assertEquals(0, doc0.getId());
        assertNotNull(doc0.getSlugId());
        assertNotNull(doc0.getTitle());
        assertEquals("Wikipedia", doc0.getDocType());
        assertNotNull(doc0.getCategory());
        assertTrue(doc0.getWordCount() > 0, "Word count should be > 0");
        assertTrue(doc0.getSizeBytes() > 0, "Size in bytes should be > 0");
        assertTrue(doc0.getSections().size() >= 1, "Doc should have at least 1 section");
    }

    @Test
    public void testMissingManifestThrowsException() {
        ClasspathCorpusLoader loader = new ClasspathCorpusLoader("/corpus/non_existent_manifest.txt");
        assertThrows(IOException.class, () -> loader.loadCorpus());
    }
}
