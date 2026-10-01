package com.minisearchengine.engine;

/**
 * Immutable representation of a document section parsed from heading blocks (e.g., "## Heading").
 */
public final class Section {

    private final String heading;
    private final String content;

    public Section(String heading, String content) {
        if (heading == null) throw new IllegalArgumentException("heading must not be null.");
        if (content == null) throw new IllegalArgumentException("content must not be null.");
        this.heading = heading;
        this.content = content;
    }

    public String getHeading() {
        return heading;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Section{heading=\"" + heading + "\"}";
    }
}
