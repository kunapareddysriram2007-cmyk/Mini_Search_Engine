package com.minisearchengine.rest.config;

import com.minisearchengine.engine.ClasspathCorpusLoader;
import com.minisearchengine.engine.SearchEngine;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes and builds the Search Engine index once at application startup.
 */
@Configuration
public class SearchEngineInitializer implements ApplicationRunner {

    private final SearchEngine searchEngine = new SearchEngine();

    @Bean
    public SearchEngine searchEngine() {
        return searchEngine;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("[SearchEngineInitializer] Loading corpus and building index...");
        searchEngine.buildIndex(new ClasspathCorpusLoader());
        System.out.println("[SearchEngineInitializer] Index built successfully in "
                + searchEngine.getLastIndexBuildTimeMs() + " ms over "
                + searchEngine.getCorpusDocumentCount() + " documents.");
    }
}
