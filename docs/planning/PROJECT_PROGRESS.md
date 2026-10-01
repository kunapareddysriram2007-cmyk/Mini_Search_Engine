# PROJECT_PROGRESS.md — Mini Search Engine Backend

> Single source of truth for the backend's CURRENT state. Read together with
> `BACKEND_TASKS.md` and `BACKEND_PLAN.md`.
> Current status: ALL STAGES (0-6) FULLY IMPLEMENTED, TESTED, AND VERIFIED.
> Last updated: 2026-09-16.

---

## 1. Project Objective

Build a REAL information-retrieval engine for the Advanced DSA course (TextHack):
a Java 17 + Maven + Spring Boot backend exposing a REST API, implementing every
algorithm by hand (no search libraries, no mocked results), consumed by the
existing React/Vite UI in `UI/`.

---

## 2. Architecture & Modules

```
Mini_Search_Engine/
├── src/main/java/com/minisearchengine/     ← PURE JAVA ENGINE (reused via build-helper)
│   ├── core/ds/        CustomArrayList, CustomHashMap (separate chaining), CustomHashSet,
│   │                   CustomPriorityQueue (min-heap), CustomQueue,
│   │                   CustomSort (randomized quicksort), MergeSort (stable)
│   ├── core/string/    StringMatcher (if), NaiveStringMatcher, KMPStringMatcher,
│   │                   ZAlgorithmMatcher, RabinKarpMatcher, AhoCorasick,
│   │                   MultiPatternMatcher (if), SuffixStructure (if),
│   │                   SuffixArrayBuilder (prefix-doubling + Kasai LCP),
│   │                   LevenshteinDistance (two-row DP + early-abandon)
│   ├── engine/         Document, CorpusDocument, Section, TextNormalizer, Tokenizer,
│   │                   StopWordFilter (41 stop words), SimpleStemmer (suffix rules),
│   │                   TextPipeline (unified analyze), InvertedIndex, PositionalInvertedIndex,
│   │                   CorpusLoader (if), ClasspathCorpusLoader, QueryParser,
│   │                   SearchDispatcher, Ranker, SnippetExtractor, SearchEngine,
│   │                   SearchQuery, SearchEngineResult, SearchResultItem, ScoreBreakdown
│   └── Main.java       CLI demo
├── src/test/java/...                       ← 223 pure engine unit tests
└── Backend/                                ← SPRING BOOT REST LAYER
    ├── pom.xml                             Spring Boot 3.3.5, Java 17, build-helper, surefire
    ├── src/main/java/com/minisearchengine/rest/
    │   ├── MiniSearchApplication.java      @SpringBootApplication entry point
    │   ├── config/WebConfig.java           CORS configuration for UI origin :8443
    │   ├── config/SearchEngineInitializer.java  ApplicationRunner (indexes 45 docs on startup)
    │   ├── controller/SearchController.java     POST /api/search
    │   ├── controller/DocumentController.java   GET /api/documents, GET /api/documents/{id}
    │   ├── controller/MetadataController.java   GET /api/health, /api/statistics, /api/algorithms,
    │   │                                        /api/algorithms/performance, /api/suggestions,
    │   │                                        POST /api/index/rebuild
    │   ├── dto/                            SearchRequest, SearchResponseDto, ResultItemDto,
    │   │                                   DocumentDetailDto, StatisticsResponseDto, AlgorithmInfoDto,
    │   │                                   PerformanceEntryDto, HealthResponseDto, ErrorResponseDto,
    │   │                                   ScoreBreakdownDto, SectionDto
    │   ├── service/                        SearchService, DocumentService, StatisticsService
    │   └── exception/                      GlobalExceptionHandler (400/404/500 uniform error body)
    ├── src/main/resources/
    │   ├── application.properties          port 8080, UTF-8, CORS origins
    │   └── corpus/                         45 real articles across categories
    └── src/test/java/...                   Spring Boot context + MockMvc controller test suite
```

---

## 3. Implemented REST Endpoints

| Endpoint | Method | Purpose | Status |
| :--- | :--- | :--- | :--- |
| `/api/health` | GET | Health, index status, document count | **Active & Verified** |
| `/api/search` | POST | Multi-mode search (exact, partial, fuzzy), ranking, pagination | **Active & Verified** |
| `/api/documents` | GET | List all indexed documents | **Active & Verified** |
| `/api/documents/{id}`| GET | Get document detail by slug or int ID | **Active & Verified** |
| `/api/statistics` | GET | Real index metrics, docType counts, avg search time | **Active & Verified** |
| `/api/algorithms` | GET | Supported algorithm catalog matching UI IDs | **Active & Verified** |
| `/api/algorithms/performance` | GET | Real measured per-algorithm performance statistics | **Active & Verified** |
| `/api/suggestions` | GET | Dynamic prefix search over vocabulary & document titles | **Active & Verified** |
| `/api/index/rebuild` | POST | Re-indexes corpus and returns build metrics | **Active & Verified** |

---

## 4. Test & Build Status

```bash
cd Backend && mvn clean package
[INFO] Tests run: 235, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- **235 automated tests passing** across all data structures, string algorithms, indexing, ranking, search modes, and REST controllers.
- **Zero `java.util.*` imports** in `com.minisearchengine.core` and `com.minisearchengine.engine`.
- Executable JAR built at `Backend/target/mini-search-engine-backend-1.0-SNAPSHOT.jar`.
