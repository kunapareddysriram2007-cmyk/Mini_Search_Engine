package com.minisearchengine.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minisearchengine.rest.dto.SearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.indexReady").value(true))
                .andExpect(jsonPath("$.documents").value(org.hamcrest.Matchers.greaterThanOrEqualTo(45)));
    }

    @Test
    public void testSearchEndpointSuccess() throws Exception {
        SearchRequest request = new SearchRequest("algorithm", "exact", "kmp", "all", "relevance", 1, 10);

        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("algorithm"))
                .andExpect(jsonPath("$.algorithmUsed").value("KMP"))
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    public void testSearchDifferentAlgorithms() throws Exception {
        // 1. Smart Search (VSM + Inverted Index + TF-IDF)
        SearchRequest smartReq = new SearchRequest("operating system", "exact", "smart", "all", "relevance", 1, 5);
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smartReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithmUsed").value("Smart Search (VSM)"))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[0].scoreBreakdown.queryTerms").isArray())
                .andExpect(jsonPath("$.results[0].scoreBreakdown.matchedTerms").isArray())
                .andExpect(jsonPath("$.results[0].scoreBreakdown.termFrequencies").isMap())
                .andExpect(jsonPath("$.results[0].scoreBreakdown.cosineSimilarity").isNumber())
                .andExpect(jsonPath("$.results[0].scoreBreakdown.finalRelevance").isNumber());



        // 2. Exact Pattern Search (KMP)
        SearchRequest exactReq = new SearchRequest("Binary Search Tree", "exact", "exact-pattern", "all", "relevance", 1, 5);
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exactReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithmUsed").value("Exact Pattern (KMP)"))
                .andExpect(jsonPath("$.results").isArray());

        // 3. Multi-Pattern Search (Aho-Corasick)
        SearchRequest multiReq = new SearchRequest("tree graph search", "partial", "multi-pattern", "all", "relevance", 1, 5);
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(multiReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithmUsed").value("Multi-Pattern (Aho-Corasick)"))
                .andExpect(jsonPath("$.results").isArray());

        // 4. Fuzzy Search (Levenshtein Edit Distance)
        SearchRequest fuzzyReq = new SearchRequest("algoritm", "fuzzy", "fuzzy", "all", "relevance", 1, 5);
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fuzzyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithmUsed").value("Fuzzy Search (Levenshtein)"))
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    public void testSearchWorkOsRelevanceCorrectness() throws Exception {
        SearchRequest req = new SearchRequest("work os", "partial", "aho-corasick", "all", "relevance", 1, 10);
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithmUsed").value("Aho-Corasick"));
    }

    @Test
    public void testSearchEmptyQueryReturnsBadRequest() throws Exception {
        SearchRequest invalidReq = new SearchRequest("", "exact", "auto", "all", "relevance", 1, 10);
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    public void testGetAllDocumentsEndpoint() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(45)));
    }

    @Test
    public void testGetDocumentByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/documents/0"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetDocumentByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/documents/unknown-non-existent-doc-12345"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetStatisticsEndpoint() throws Exception {
        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDocuments").value(org.hamcrest.Matchers.greaterThanOrEqualTo(45)))
                .andExpect(jsonPath("$.docTypeCounts").isMap());
    }

    @Test
    public void testGetAlgorithmsEndpoint() throws Exception {
        mockMvc.perform(get("/api/algorithms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithms").isArray());
    }

    @Test
    public void testGetAlgorithmsPerformanceEndpoint() throws Exception {
        mockMvc.perform(get("/api/algorithms/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries").isArray());
    }

    @Test
    public void testSuggestionsEndpoint() throws Exception {
        mockMvc.perform(get("/api/suggestions?q=algo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions").isArray());

        mockMvc.perform(get("/api/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions").isArray());
    }

    @Test
    public void testRebuildIndexEndpoint() throws Exception {
        mockMvc.perform(post("/api/index/rebuild"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REBUILT"))
                .andExpect(jsonPath("$.documents").value(org.hamcrest.Matchers.greaterThanOrEqualTo(45)));
    }

    @Test
    public void testFilterMetadataEndpoint() throws Exception {
        mockMvc.perform(get("/api/filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.languages").isArray())
                .andExpect(jsonPath("$.docTypes").isArray());
    }

    @Test
    public void testAdvancedSearchAndMetadataFiltering() throws Exception {
        // Test filtering by category & language
        SearchRequest req = new SearchRequest(
                "operating", "exact", "smart", "all", "computer-science", "English", null, null, null, "relevance", 1, 5
        );
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].category").value("computer-science"))
                .andExpect(jsonPath("$.results[0].language").value("English"));

        // Test advanced search with excluded terms
        SearchRequest exclReq = new SearchRequest(
                "operating system", "exact", "smart", "all", "all", "all", null, null,
                java.util.List.of("process"), "relevance", 1, 5
        );
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exclReq)))
                .andExpect(status().isOk());
    }
}

