package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import java.io.IOException;

/**
 * Strategy interface for loading corpus documents.
 * Allows decoupling the storage format / source (classpath, disk, database)
 * from the search engine indexer.
 */
public interface CorpusLoader {

    /**
     * Loads and returns all corpus documents.
     * @return CustomArrayList of parsed CorpusDocument instances.
     * @throws IOException if reading the corpus fails.
     */
    CustomArrayList<CorpusDocument> loadCorpus() throws IOException;
}
