package com.minisearchengine.rest.service;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.engine.CorpusDocument;
import com.minisearchengine.engine.SearchEngine;
import com.minisearchengine.engine.Section;
import com.minisearchengine.rest.dto.DocumentDetailDto;
import com.minisearchengine.rest.dto.SectionDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    private final SearchEngine searchEngine;

    public DocumentService(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    public List<DocumentDetailDto> getAllDocuments() {
        List<DocumentDetailDto> list = new ArrayList<>();
        CustomArrayList<CorpusDocument> docs = searchEngine.getCorpusDocuments();
        for (int i = 0; i < docs.size(); i++) {
            list.add(toDto(docs.get(i)));
        }
        return list;
    }

    public DocumentDetailDto getDocumentById(String id) {
        CorpusDocument doc = searchEngine.getDocumentBySlugOrId(id);
        if (doc == null) {
            return null;
        }
        return toDto(doc);
    }

    private DocumentDetailDto toDto(CorpusDocument doc) {
        List<SectionDto> sectionDtos = new ArrayList<>();
        CustomArrayList<Section> sections = doc.getSections();
        for (int i = 0; i < sections.size(); i++) {
            Section s = sections.get(i);
            sectionDtos.add(new SectionDto(s.getHeading(), s.getContent()));
        }

        return new DocumentDetailDto(
                doc.getSlugId(),
                doc.getTitle(),
                doc.getPath(),
                doc.getDocType(),
                doc.getCategory(),
                doc.getLanguage(),
                formatSizeBytes(doc.getSizeBytes()),
                doc.getSizeBytes(),
                doc.getWordCount(),
                doc.getDate(),
                doc.getOverview(),
                sectionDtos
        );
    }


    private String formatSizeBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        return String.format("%.1f KB", kb);
    }
}
