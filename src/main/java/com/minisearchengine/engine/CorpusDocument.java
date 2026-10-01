package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Rich document object representing an ingested document from the corpus.
 * Contains metadata required for both search indexing and detailed UI rendering.
 */
public final class CorpusDocument {

    private final int id;
    private final String slugId;
    private final String title;
    private final String path;
    private final String docType;
    private final String category;
    private final String language;
    private final String content;
    private final String overview;
    private final CustomArrayList<Section> sections;
    private final int wordCount;
    private final long sizeBytes;
    private final String date;

    public CorpusDocument(
            int id,
            String slugId,
            String title,
            String path,
            String docType,
            String category,
            String content,
            String overview,
            CustomArrayList<Section> sections,
            int wordCount,
            long sizeBytes,
            String date) {
        this(id, slugId, title, path, docType, category, "English", content, overview, sections, wordCount, sizeBytes, date);
    }

    public CorpusDocument(
            int id,
            String slugId,
            String title,
            String path,
            String docType,
            String category,
            String language,
            String content,
            String overview,
            CustomArrayList<Section> sections,
            int wordCount,
            long sizeBytes,
            String date) {
        if (id < 0) throw new IllegalArgumentException("id must be >= 0");
        if (slugId == null) throw new IllegalArgumentException("slugId must not be null");
        if (title == null) throw new IllegalArgumentException("title must not be null");
        if (path == null) throw new IllegalArgumentException("path must not be null");
        if (docType == null) throw new IllegalArgumentException("docType must not be null");
        if (category == null) throw new IllegalArgumentException("category must not be null");
        if (language == null) throw new IllegalArgumentException("language must not be null");
        if (content == null) throw new IllegalArgumentException("content must not be null");
        if (overview == null) throw new IllegalArgumentException("overview must not be null");
        if (sections == null) throw new IllegalArgumentException("sections must not be null");
        if (date == null) throw new IllegalArgumentException("date must not be null");

        this.id = id;
        this.slugId = slugId;
        this.title = title;
        this.path = path;
        this.docType = docType;
        this.category = category;
        this.language = language;
        this.content = content;
        this.overview = overview;
        this.sections = sections;
        this.wordCount = wordCount;
        this.sizeBytes = sizeBytes;
        this.date = date;
    }


    public int getId() {
        return id;
    }

    public String getSlugId() {
        return slugId;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public String getDocType() {
        return docType;
    }

    public String getCategory() {
        return category;
    }

    public String getLanguage() {
        return language;
    }


    public String getContent() {
        return content;
    }

    public String getOverview() {
        return overview;
    }

    public CustomArrayList<Section> getSections() {
        return sections;
    }

    public int getWordCount() {
        return wordCount;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getDate() {
        return date;
    }

    /**
     * Converts to basic engine Document for backward compatibility.
     */
    public Document toDocument() {
        return new Document(id, title, content);
    }

    @Override
    public String toString() {
        return "CorpusDocument{id=" + id + ", slugId=\"" + slugId + "\", title=\"" + title + "\"}";
    }
}
