# BACKEND_TASKS.md — Ordered Implementation Checklist

> Companion to `PROJECT_PROGRESS.md` (current state) and `BACKEND_PLAN.md`
> (architecture and design specification).
> Work status: ALL STAGES (0 through 6) COMPLETED and VERIFIED.
> Last updated: 2026-09-16.

---

## Stage 0 — Environment & Planning (DONE)
- [x] Java 17.0.16 + Maven 3.9.16 verified
- [x] UI inspected; API contract extracted (filter/algorithm ids listed in PROJECT_PROGRESS.md §14)
- [x] Architecture decision APPROVED: Backend reuses `../src/main/java` + `../src/test/java`
      via build-helper-maven-plugin (no copies, no engine duplication)

## Stage 1 — Backend Maven Skeleton + core.ds (DONE)
- [x] `Backend/pom.xml`: Java 17, UTF-8, Spring Boot 3.3.5 parent, starter-web,
      starter-test, build-helper (add-source + add-test-source → `../src/...`),
      surefire, spring-boot-maven-plugin
- [x] `MiniSearchApplication.java` + `application.properties` (port 8080, CORS origins)
- [x] `core/ds/Comparator` equivalent: `CustomPriorityQueue.CustomComparator<T>` (pre-existing)
- [x] `CustomArrayList`, `CustomHashMap` (separate chaining), `CustomHashSet`,
      `CustomQueue`, `CustomPriorityQueue` (min-heap), `CustomSort` (randomized quicksort)
- [x] `core/ds/MergeSort.java` — stable merge sort
- [x] Tests: every structure incl. merge-sort stability — 183 legacy + 6 new GREEN
- [x] `mvn test` GREEN (via Backend build) ✔

## Stage 2 — core.string (DONE)
- [x] `NaiveStringMatcher`, `KMPStringMatcher` (LPS), `ZAlgorithmMatcher`
- [x] `AhoCorasick` (goto/fail/output, BFS failure links via CustomQueue)
- [x] `RabinKarpMatcher` (rolling hash + explicit verification)
- [x] `SuffixArrayBuilder` (prefix-doubling + Kasai LCP + binary-search search())
- [x] `LevenshteinDistance`: two-row DP, `distance` + `withinThreshold` with early-abandon (length rule + row-min rule)
- [x] Tests: classic cases, overlaps, no-match, empty, unicode, early-abandon consistency — GREEN

## Stage 2.5 — Text pipeline (DONE)
- [x] `TextNormalizer` (lowercase, punctuation→space, whitespace collapse)
- [x] `Tokenizer` (space-delimited, single pass)
- [x] `StopWordFilter`: 41-word English stop list on CustomHashSet
- [x] `SimpleStemmer`: ordered longest-match suffix rules, idempotent, operate/operating aligned to "operat"
- [x] `TextPipeline`: unified analyze() for docs+queries, emits Token{term, original, position}; positions compacted after stop-word removal
- [x] Tests: 16 unit tests (TextPipelineTest) — GREEN

## Stage 3 — Corpus & document model (DONE)
- [x] `engine/CorpusDocument.java` — slug id, title, path, docType, category, overview, sections, wordCount, sizeBytes, date; wraps/extends `Document`
- [x] `engine/CorpusLoader.java` (interface) + `engine/ClasspathCorpusLoader.java` — reads `classpath:corpus/{category}/*.txt`, UTF-8, front-matter (`type:` / `date:` / `overview:`), `## Heading` section parsing, deterministic id assignment
- [x] 45 real articles under `Backend/src/main/resources/corpus/` (computer-science, data-structures, algorithms, etc.; mix of Wikipedia/Article/Reference docTypes; no lorem-ipsum placeholders)
- [x] TF-aware positional indexing (`PositionalInvertedIndex`, `InvertedIndex`)
- [x] Tests: corpus parsing (front-matter, sections, UTF-8 chars), loader determinism, wordCount/sizeBytes correctness — GREEN

## Stage 4 — Engine facade (DONE)
- [x] `engine/QueryParser` — normalize→tokenize (via TextPipeline)→ mode/algorithm resolution; `auto` rules: exact→kmp, partial→aho-corasick, fuzzy→edit-distance
- [x] `engine/SearchDispatcher` — REAL dispatch (the chosen algorithm actually runs):
      exact→{kmp | z-algo | rabin-karp | suffix} over raw doc text;
      partial→Aho-Corasick (all query terms, one pass per doc);
      fuzzy→Levenshtein vocabulary expansion (threshold ≈ max(1, len/3), capped 2–3) → index lookup
- [x] `engine/Ranker` — 0.45·tfidf + 0.25·title + 0.15·phrase + 0.15·proximity; scoreBreakdown per result; top-K via CustomPriorityQueue; final order via MergeSort (stable; ties → title asc, then id)
- [x] `engine/SnippetExtractor` — window around best match, ORIGINAL casing preserved (UI highlights case-insensitive literal tokens), match offsets
- [x] `engine/SearchEngine` — facade: buildIndex() once; search(SearchQuery) → EngineResult with MEASURED searchTimeMs, documentsScanned, totalMatches, algorithmUsed (resolved id), indexType
- [x] docType filtering + server-side pagination (page ≥ 1, pageSize 1–50)
- [x] Tests: exact/partial/fuzzy end-to-end on test corpus; determinism golden test; hand-computed ranker scores; pagination/sort invariants; algorithm-dispatch assertions (each id runs its own algorithm) — GREEN

## Stage 5 — Spring REST layer (DONE)
- [x] `rest/dto/**` — SearchRequest, SearchResponseDto, ResultItemDto, DocumentDetailDto, StatisticsResponseDto, AlgorithmInfoDto, PerformanceEntryDto, HealthResponseDto, ErrorResponseDto, ScoreBreakdownDto, SectionDto (field names EXACTLY per UI types.ts / BACKEND_PLAN.md §15)
- [x] `rest/service/SearchService` (engine call, sort contract, pagination, per-algorithm duration tracking), `DocumentService`, `StatisticsService` (real measured aggregation & performance metrics)
- [x] `rest/controller/SearchController` — POST /api/search
- [x] `rest/controller/DocumentController` — GET /api/documents, GET /api/documents/{id} (404 on unknown)
- [x] `rest/controller/MetadataController` — GET /api/health, /api/statistics, /api/algorithms, /api/algorithms/performance, /api/suggestions?q=, POST /api/index/rebuild
- [x] `rest/config/SearchEngineInitializer` — build corpus+index once at startup, log real index stats
- [x] `rest/exception/GlobalExceptionHandler` — 400/404/500 uniform error body
- [x] Tests: MockMvc — every endpoint, JSON shape, validation errors, 404, CORS header — GREEN

## Stage 6 — Final verification & docs (DONE)
- [x] Full `mvn clean test` green (235/235 tests passing)
- [x] `mvn clean package` produces runnable fat JAR
- [x] Endpoint validation: /api/health, /api/search, /api/documents, /api/documents/{id}, /api/statistics, /api/algorithms, /api/algorithms/performance, /api/suggestions, /api/index/rebuild
- [x] Final audit: no `java.util` in engine; UI untouched; all stats/timings measured

---

## Final Acceptance Criteria
1. `mvn clean package` succeeds; all JUnit tests green (235/235 passed)
2. Real search over real corpus via REST — exact, partial, fuzzy all work
3. Algorithm selection changes execution (verifiable by output/timings)
4. Pagination + sorting + docType filtering correct
5. All statistics/performance numbers measured, never mocked
6. `core/**` + `engine/**` free of `java.util.*` imports
7. UI can be wired with a thin fetch layer using the documented contracts as-is
