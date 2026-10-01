package com.minisearchengine.engine;

import com.minisearchengine.core.ds.CustomArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Classpath implementation of {@link CorpusLoader}.
 * Reads document files listed in a corpus manifest (default: {@code /corpus/index.txt})
 * from the application classpath.
 */
public class ClasspathCorpusLoader implements CorpusLoader {

    private final String manifestPath;

    public ClasspathCorpusLoader() {
        this("/corpus/index.txt");
    }

    public ClasspathCorpusLoader(String manifestPath) {
        if (manifestPath == null) throw new IllegalArgumentException("manifestPath must not be null");
        this.manifestPath = manifestPath;
    }

    @Override
    public CustomArrayList<CorpusDocument> loadCorpus() throws IOException {
        InputStream manifestStream = getClass().getResourceAsStream(manifestPath);
        if (manifestStream == null) {
            throw new IOException("Corpus manifest file not found on classpath: " + manifestPath);
        }

        CustomArrayList<String> filePaths = new CustomArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(manifestStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    filePaths.add(line);
                }
            }
        }

        String baseDir = manifestPath.lastIndexOf('/') >= 0 ? manifestPath.substring(0, manifestPath.lastIndexOf('/') + 1) : "/corpus/";
        CustomArrayList<CorpusDocument> documents = new CustomArrayList<>();
        int assignId = 0;
        for (int i = 0; i < filePaths.size(); i++) {
            String relPath = filePaths.get(i);
            String resourcePath = baseDir + relPath;
            InputStream docStream = getClass().getResourceAsStream(resourcePath);
            if (docStream == null) {
                throw new IOException("Corpus document resource not found on classpath: " + resourcePath);
            }

            CorpusDocument doc = parseDocumentResource(assignId++, relPath, docStream);
            documents.add(doc);
        }

        return documents;
    }

    private CorpusDocument parseDocumentResource(int id, String relPath, InputStream stream) throws IOException {
        String docType = "Article";
        String date = "2024-01-01";
        String customTitle = null;
        String frontMatterOverview = null;
        String customLanguage = null;

        CustomArrayList<Section> sections = new CustomArrayList<>();
        StringBuilder fullContentBuilder = new StringBuilder();
        StringBuilder currentSectionContent = new StringBuilder();
        String currentHeading = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            boolean inFrontMatter = true;

            while ((line = reader.readLine()) != null) {
                if (inFrontMatter) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("type:")) {
                        docType = trimmed.substring("type:".length()).trim();
                        continue;
                    } else if (trimmed.startsWith("date:")) {
                        date = trimmed.substring("date:".length()).trim();
                        continue;
                    } else if (trimmed.startsWith("language:")) {
                        customLanguage = trimmed.substring("language:".length()).trim();
                        continue;
                    } else if (trimmed.startsWith("title:")) {
                        customTitle = trimmed.substring("title:".length()).trim();
                        continue;
                    } else if (trimmed.startsWith("overview:")) {
                        frontMatterOverview = trimmed.substring("overview:".length()).trim();
                        if (fullContentBuilder.length() > 0) {
                            fullContentBuilder.append("\n");
                        }
                        fullContentBuilder.append(frontMatterOverview);
                        continue;
                    } else if (trimmed.isEmpty()) {
                        continue;
                    } else {
                        inFrontMatter = false;
                    }
                }

                if (line.startsWith("## ")) {
                    if (currentHeading != null) {
                        sections.add(new Section(currentHeading, currentSectionContent.toString().trim()));
                        currentSectionContent.setLength(0);
                    }
                    currentHeading = line.substring(3).trim();
                } else {
                    if (currentHeading != null) {
                        if (currentSectionContent.length() > 0) {
                            currentSectionContent.append("\n");
                        }
                        currentSectionContent.append(line);
                    }
                }

                if (fullContentBuilder.length() > 0) {
                    fullContentBuilder.append("\n");
                }
                fullContentBuilder.append(line);
            }

            if (currentHeading != null) {
                sections.add(new Section(currentHeading, currentSectionContent.toString().trim()));
            }
        }

        String fullContent = fullContentBuilder.toString().trim();
        byte[] bytes = fullContent.getBytes(StandardCharsets.UTF_8);
        long sizeBytes = bytes.length;

        int wordCount = countWords(fullContent);

        // Derive slugId and category from relPath (e.g., "computer-science/operating-system.txt")
        String category = "general";
        String filename = relPath;
        int lastSlash = relPath.lastIndexOf('/');
        if (lastSlash >= 0) {
            category = relPath.substring(0, lastSlash);
            filename = relPath.substring(lastSlash + 1);
        }
        String slugId = filename.endsWith(".txt") ? filename.substring(0, filename.length() - 4) : filename;

        String language = "English";
        if (customLanguage != null && !customLanguage.isEmpty()) {
            language = customLanguage;
        } else if ("abkhazian".equalsIgnoreCase(category)) {
            language = "Abkhazian";
        } else if ("acehnese".equalsIgnoreCase(category)) {
            language = "Acehnese";
        } else if ("adyghe".equalsIgnoreCase(category)) {
            language = "Adyghe";
        } else {
            language = "English";
        }

        String title = (customTitle != null && !customTitle.isEmpty())
                ? customTitle
                : deriveTitleFromSlug(slugId);

        String overview = (frontMatterOverview != null && !frontMatterOverview.isEmpty())
                ? frontMatterOverview
                : (sections.size() > 0 ? sections.get(0).getContent() : fullContent);

        String fullPath = "corpus/" + relPath;

        return new CorpusDocument(
                id,
                slugId,
                title,
                fullPath,
                docType,
                category,
                language,
                fullContent,
                overview,
                sections,
                wordCount,
                sizeBytes,
                date
        );

    }

    private static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        int words = 0;
        boolean inWord = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                if (!inWord) {
                    words++;
                    inWord = true;
                }
            } else {
                inWord = false;
            }
        }
        return words;
    }

    private static String deriveTitleFromSlug(String slug) {
        String[] parts = slug.split("-");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) {
                sb.append(parts[i].substring(1));
            }
        }
        return sb.toString();
    }
}
