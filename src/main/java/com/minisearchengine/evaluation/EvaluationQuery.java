package com.minisearchengine.evaluation;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Represents a single evaluation query with metadata and ground-truth relevant documents (qrels).
 */
public class EvaluationQuery {

    private final String id;
    private final String queryText;
    private final String type;
    private final String description;
    private final CustomArrayList<String> expectedDocPaths;

    public EvaluationQuery(String id, String queryText, String type, String description, CustomArrayList<String> expectedDocPaths) {
        this.id = id;
        this.queryText = queryText;
        this.type = (type == null) ? "smart" : type;
        this.description = (description == null) ? "" : description;
        this.expectedDocPaths = (expectedDocPaths == null) ? new CustomArrayList<>() : expectedDocPaths;
    }

    public String getId() { return id; }
    public String getQueryText() { return queryText; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public CustomArrayList<String> getExpectedDocPaths() { return expectedDocPaths; }
}
