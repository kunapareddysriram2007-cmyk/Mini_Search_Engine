# BACKEND_TASKS.md — Ordered Implementation Checklist

> Companion to `BACKEND_PLAN.md` — read that first for architecture, JSON contracts, and
> hard constraints (especially §17). Work top-to-bottom. Compile + run tests after every
> stage; do not proceed while tests are red. Mark ✅ when done. Last updated: 2026-09-13.

---

## Stage 0 — Environment & Planning (DONE — do not redo)
- [x] Java 17.0.16 + Maven 3.9.16 verified
- [x] UI inspected; API contract extracted into plan §4
- [x] Backend folder confirmed empty; plan approved by user
- [x] `BACKEND_PLAN.md` + `BACKEND_TASKS.md` created

## Stage 1 — Maven Skeleton + core.ds (APPROVED, NEXT ACTION)
- [ ] `pom.xml`: Java 17, UTF-8 sourceEncoding, spring-boot-starter-web,
      spring-boot-starter-test (test), spring-boot-maven-plugin, Surefire
- [ ] `MiniSearchApplication.java` + `application.properties` (port 8080)
- [ ] `core/ds/Comparator.java` (own interface — no java.util)
- [ ] `core/ds/CustomArrayList.java` — growable array, add/get/set/addAt/remove/size/isEmpty/contains
- [ ] `core/ds/CustomHashMap.java` — open addressing, power-of-2 capacity, load 0.75,
      hash mixing; put/get/containsKey/remove/size/keySet/values
- [ ] `core/ds/CustomHashSet.java` — backed by CustomHashMap
- [ ] `core/ds/CustomQueue.java` — ring buffer
- [ ] `core/ds/CustomPriorityQueue.java` — binary min-heap + own Comparator
- [ ] `core/ds/CustomSort.java` — mergeSort (stable) + quickSort (median-of-3)
- [ ] Tests: every structure (resize, collisions, empty, heap order, stability, bounds)
- [ ] `mvn test` GREEN → commit/checkpoint

## Stage 2 — core.string (String Algorithms)
- [ ] `KMP.java` — failure table, find-all occurrences, O(n+m)
- [ ] `ZAlgorithm.java` — Z-array, find-all occurrences
- [ ] `AhoCorasick.java` — goto/fail/output automaton over multiple patterns, BFS failure
      links (uses CustomQueue), per-pattern counts + positions in ONE text pass
- [ ] `RabinKarp.java` — rolling hash + verification; multi-pattern via pattern hash set (CustomHashSet)
- [ ] `LevenshteinDistance.java` — DP table + early-abandon band; `distance(a,b)` and `withinThreshold(a,b,k)`
- [ ] `SuffixArray.java` — doubling build O(n log² n); search → occurrence range
- [ ] Tests: classic cases, overlaps, no-match, empty pattern, unicode chars, collision path
- [ ] `mvn test` GREEN

## Stage 3 — core.text + core.index + model + corpus
- [ ] `TextNormalizer` — lowercase, accent-fold, punctuation→space, whitespace collapse
- [ ] `Tokenizer` — alphanumeric runs; returns tokens + original offsets
- [ ] `StopWordFilter` — ~40 stop words in CustomHashSet
- [ ] `SimpleStemmer` — transparent suffix rules (s/es/ies, ed, ing, tion)
- [ ] `model/Document.java` + `model/Section.java` (per plan §9)
- [ ] `core/index/InvertedIndex.java` — term → {docId, tf}
- [ ] `core/index/PositionalInvertedIndex.java` — term → docId → sorted positions;
      phrase + proximity support
- [ ] `core/index/DocumentStore.java` (CustomHashMap-based)
- [ ] `repository/CorpusLoader` interface + `ClasspathCorpusLoader`
      (front-matter `type:`/`date:`/`overview:`, `## Heading` section parsing, UTF-8)
- [ ] `repository/DocumentRepository` — loads into DocumentStore
- [ ] Corpus: 25–30 real articles under `src/main/resources/corpus/{category}/*.txt`
      (200–500 words each; mix of Wikipedia/Article/Reference)
- [ ] Tests: normalizer/tokenizer/stemmer/stopwords; index tf/df/phrase/proximity; corpus parsing
- [ ] `mvn test` GREEN

## Stage 4 — engine (SearchEngine, pure Java)
- [ ] `model/SearchQuery` + `model/EngineResult` + `model/MatchInfo` + `model/EngineStats`
- [ ] `engine/query/QueryParser` — normalize→tokenize→mode/algorithm resolution (`auto` rules)
- [ ] `engine/match/SearchDispatcher` — REAL dispatch:
      exact→{kmp|z-algo|rabin-karp|suffix}, partial→aho-corasick(+positional),
      fuzzy→Levenshtein vocabulary expansion
- [ ] `engine/rank/Ranker` — 0.45 tfidf + 0.25 title + 0.15 phrase + 0.15 proximity,
      scoreBreakdown, top-K via CustomPriorityQueue, CustomSort for final order
- [ ] `engine/snippet/SnippetExtractor` — window around best match, original casing, offsets
- [ ] `engine/SearchEngine` — facade: `buildIndex()` once; `search(SearchQuery)` → EngineResult
      with real timings, documentsScanned, totalMatches
- [ ] Tests: exact/partial/fuzzy end-to-end on tiny test corpus; determinism golden test;
      ranker hand-computed assertions; pagination/sort invariants
- [ ] `mvn test` GREEN

## Stage 5 — Spring Layer (API)
- [ ] `dto/**` records matching plan §15 (SearchRequest, SearchResponse, ResultItem,
      DocumentDetail, StatisticsResponse, AlgorithmInfo, PerformanceEntry, HealthResponse, ErrorResponse)
- [ ] `service/SearchService` (engine call + pagination contract), `DocumentService`,
      `StatisticsService` (real measured aggregation)
- [ ] `controller/SearchController`, `DocumentController`, `MetadataController`
- [ ] `config/WebConfig` — CORS for http://localhost:8443
- [ ] `config/SearchEngineInitializer` (ApplicationRunner — build index at startup, log stats)
- [ ] `exception/GlobalExceptionHandler` — 400/404/405/500 + uniform error body
- [ ] MockMvc tests: every endpoint, validation errors, 404, CORS header, JSON shape
- [ ] `mvn test` GREEN

## Stage 6 — Statistics, Performance, Docs, Final Verification
- [ ] `/api/statistics` real numbers; `/api/algorithms`; `/api/algorithms/performance`
      (measured aggregation); `/api/suggestions`; `/api/index/rebuild`
- [ ] `README.md` — how to run, architecture diagram, viva explanations, endpoint examples
- [ ] Full `mvn clean test` → all green
- [ ] `mvn clean package` → runnable jar
- [ ] Start app, curl EVERY endpoint, paste real outputs into README
- [ ] Final check: no `java.util` imports in core/engine; no UI files touched

## Final Acceptance Criteria
1. `mvn clean package` succeeds; all JUnit tests green
2. Real search over real corpus via REST — exact, partial, fuzzy all work
3. Algorithm selection changes execution (verifiable by output/timings)
4. Pagination + sorting + docType filtering correct
5. All statistics/performance numbers measured, never mocked
6. `core/**` + `engine/**` free of `java.util.*` imports
7. Frontend could be wired with a thin fetch layer using plan §15 contracts as-is
