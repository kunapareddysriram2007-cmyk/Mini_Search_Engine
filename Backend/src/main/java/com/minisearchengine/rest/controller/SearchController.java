package com.minisearchengine.rest.controller;

import com.minisearchengine.rest.dto.SearchRequest;
import com.minisearchengine.rest.dto.SearchResponseDto;
import com.minisearchengine.rest.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/search")
    public ResponseEntity<SearchResponseDto> search(@RequestBody SearchRequest request) {
        SearchResponseDto response = searchService.executeSearch(request);
        return ResponseEntity.ok(response);
    }
}
