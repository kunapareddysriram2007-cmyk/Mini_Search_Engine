package com.minisearchengine.engine;

/**
 * Immutable value object representing a document in the corpus.
 *
 * Each document has:
 *   - id      : unique non-negative integer identifier assigned by the corpus manager.
 *   - title   : human-readable label (may be empty, never null).
 *   - content : raw text body (may be empty, never null).
 *
 * The raw content is stored as-is; normalization and tokenization are the
 * responsibility of {@link TextNormalizer} and {@link Tokenizer}.
 */
public final class Document {

    private final int    id;
    private final String title;
    private final String content;

    public Document(int id, String title, String content) {
        if (id < 0)       throw new IllegalArgumentException("Document id must be >= 0.");
        if (title   == null) throw new IllegalArgumentException("title must not be null.");
        if (content == null) throw new IllegalArgumentException("content must not be null.");
        this.id      = id;
        this.title   = title;
        this.content = content;
    }

    public int    getId()      { return id; }
    public String getTitle()   { return title; }
    public String getContent() { return content; }

    @Override
    public String toString() {
        return "Document{id=" + id + ", title=\"" + title + "\"}";
    }
}