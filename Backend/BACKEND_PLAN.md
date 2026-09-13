# BACKEND_PLAN.md — Mini Search Engine / Information-Retrieval System

> **Purpose of this file:** single source of truth for the backend plan. A new coding-agent
> session should read this file plus `BACKEND_TASKS.md` and be able to continue the project
> without re-deriving any context. Last updated: 2026-09-13.

---

## 1. Project Overview

College semester project (Data Structures & Algorithms focus): a real information-retrieval
system with a Java backend exposing a REST API, consumed by an already-existing React
frontend. The backend must implement **real, hand-written DSA** — no external search
libraries, no mocked results. Every search result must be computed by our own
indexing/search/ranking code and must be explainable at a project viva.

**Final goal:** a working Java backend that loads documents, builds an inverted index,
accepts REST search requests, executes the requested algorithm (exact/partial/fuzzy),
ranks results, paginates/sorts, returns snippets + metadata + real performance stats,
and is ready for direct connection to the existing React UI.

---

## 2. Environment (VERIFIED on this machine — do not re-verify unless something fails)

| Tool | Version |
|---|---|
| Java | OpenJDK **17.0.16** (Temurin, `C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot`) |
| Maven | **3.9.16** |
| OS | Windows 11 (bash via Git Bash — use POSIX syntax, quote paths with spaces) |
| Platform encoding | **Cp1252** → `pom.xml` MUST set `project.build.sourceEncoding=UTF-8` (corpus is UTF-8, later multilingual) |

---

## 3. Project Structure On Disk (actual names)

```
<workspace root>/
├── Mini Search Engine UI Design/     ← EXISTING React frontend (DO NOT MODIFY)
│   ├── package.json                  (react 19, vite 8, tailwind 4, pnpm)
│   ├── vite.config.ts                (dev server port 8443 — relevant for CORS)
│   └── src/
│       ├── App.tsx                   (screen state machine: home|results|document|technical)
│       ├── types.ts                  (SearchResult, DocumentData, Filters, AppState)  ← API contract reference
│       ├── data.ts                   (MOCK_RESULTS, MOCK_DOCUMENTS, CORPUS_STATS, ALGORITHMS, PIPELINE_STEPS)
│       ├── utils.tsx                 (filterAndSortResults, highlightTerms, getDocTypeCounts)
│       ├── components/               (SearchBar, Logo)
│       └── screens/                  (SearchHome, SearchResults, DocumentDetails, TechnicalPage)
└── Mini-Search-Engine Backend/       ← BACKEND (was empty; now contains this plan + tasks)
    ├── BACKEND_PLAN.md               ← this file
    ├── BACKEND_TASKS.md              ← ordered implementation checklist
    └── (all backend code goes here)
```

> Note: the user sometimes refers to these as `UI/` and `Backend/` — the real folder names
> are `Mini Search Engine UI Design/` and `Mini-Search-Engine Backend/`. Always quote paths
> containing spaces in shell commands.

**Hard rule: never modify anything inside `Mini Search Engine UI Design/`.**

---

## 4. Frontend API Contract (from UI inspection — completed)

The UI currently renders from mock data only (`src/data.ts`). There is **no fetch/axios
layer anywhere** in the frontend (verified). The backend must serve JSON matching these
shapes so a thin fetch layer can be added to the UI later.

### 4.1 `SearchResult` (UI `types.ts`) — one item in `/api/search` results
```ts
{ id: string; title: string; path: string; snippet: string;
  relevance: number;        // 0–100 integer (UI renders it as a % width bar)
  algorithm: string;        // display name, e.g. "KMP"
  matches: number;          // total match count in this document
  size: string;             // human-readable, e.g. "12.4 KB"
  docType: "Wikipedia" | "Article" | "Reference";
  date: string; }           // ISO yyyy-MM-dd (UI formats it)
```

### 4.2 `DocumentData` — document details screen fields
```ts
{ id, title, path, size, matches, relevance, algorithm,
  overview: string,
  sections: { heading: string; content: string }[],
  wordCount: number,
  searchTime: number,       // ms, per-search context
  index: string,            // e.g. "Positional Inverted Index"
  pattern: string,          // the query/pattern used
  date: string }
```
`searchTime`, `pattern`, `matches`, `relevance` are **per-search context** — they come from
`/api/search` results. `GET /api/documents/{id}` returns the static document fields
(overview, sections, wordCount, size, date, path, docType, category, content);

### 4.3 `Filters` (UI sidebar) — exact option values the backend must accept
- `docType`: `"all" | "Wikipedia" | "Article" | "Reference"` (UI counts per type)
- `matchType`: `"exact" | "partial" | "fuzzy"`
- `sortBy`: `"relevance" | "newest" | "alphabetical"`
- `algorithm`: `"auto" | "kmp" | "z-algo" | "aho-corasick" | "rabin-karp" | "edit-distance" | "inverted-index"`
  - ⚠️ **These exact ids come from the UI (`data.ts` ALGORITHMS). Backend algorithm ids must match them.**

### 4.4 UI behaviors the API must support
- `SearchHome` shows 4 stats (documents indexed, avg search time, algorithms count, index coverage) → `/api/statistics`
- `SearchResults` shows: result count, search time in ms, active algorithm name, index type; filter sidebar with per-docType counts; pagination (client-side in mock, must become server-side); client-side term highlighting via `highlightTerms(snippet, query)`
- `DocumentDetails` shows meta chips + overview + sections + "Search Analysis" panel (algorithm, pattern, matches, search time, index type, doc size, word count)
- `TechnicalPage` shows algorithm cards (name, fullName, purpose, complexity, spaceComplexity, description) + pipeline steps + system stats → `/api/algorithms` + `/api/statistics`
- Highlighting note: UI highlights are case-insensitive **literal** query tokens — backend snippets should keep original text casing so UI highlight regex matches.

---

## 5. Backend Architecture (layered, engine independent of Spring)

```
Controller (Spring) → DTO (JSON boundary)
        ↓
Service (Spring)  — orchestration, timing, pagination application
        ↓
SearchEngine (PURE JAVA — no Spring imports, no java.util.*)
        ↓
Index / Algorithms / Text pipeline (core.* — pure Java)
        ↓
DocumentRepository → corpus files (classpath, later disk)
```

**Dependency rules (enforced by review; core is pure enough to unit-test without Spring):**
- `core/**` — imports ONLY `java.lang.*` (+ primitives). **No `java.util.*`** (including
  `Arrays.sort`, `Collections`, `Optional`, `Objects`). `System.arraycopy` (java.lang) allowed.
  `String`/`StringBuilder`/`CharSequence` (java.lang) allowed.
- `engine/**` — imports only `core.*` + own package. Also **no `java.util.*`**, no Spring.
- `model/**` — plain data holders; follow the same no-`java.util` discipline (use `core.ds` types).
- `controller/service/config/dto/exception` — Spring Boot allowed, `java.util` allowed here.
- `dto/**` — the ONLY place JSON field names are defined. Engine/service never see JSON.

Rationale: this is a DSA college project — the grader must see our own structures doing the
work. The engine must compile/run in a plain JVM (its tests do not start Spring).

---

## 6. Package Structure (base package `com.minisearch`)

```
com.minisearch/
├── MiniSearchApplication.java        (@SpringBootApplication entry point)
├── config/
│   ├── WebConfig.java                (CORS for http://localhost:8443)
│   └── SearchEngineInitializer.java  (builds corpus+index once at startup, ApplicationRunner)
├── controller/
│   ├── SearchController.java         (POST /api/search)
│   ├── DocumentController.java       (GET /api/documents, GET /api/documents/{id})
│   └── MetadataController.java       (health, statistics, algorithms, suggestions, index rebuild)
├── dto/                              (records: SearchRequest, SearchResponse, ResultItem,
│   │                                  DocumentDetail, StatisticsResponse, AlgorithmInfo,
│   │                                  PerformanceEntry, HealthResponse, ErrorResponse, …)
├── service/
│   ├── SearchService.java            (calls engine, applies pagination/sorting contract)
│   ├── DocumentService.java
│   └── StatisticsService.java        (real measured numbers; performance aggregation)
├── engine/                           ← PURE JAVA, the heart of the project
│   ├── SearchEngine.java             (facade: index() once; search(SearchQuery) → EngineResult)
│   ├── query/QueryParser.java        (normalize → tokenize → classify → SearchQuery plan)
│   ├── match/SearchDispatcher.java   (mode+algorithm → concrete matcher; REAL dispatch)
│   ├── rank/Ranker.java              (TF-IDF + boosts, score breakdown)
│   └── snippet/SnippetExtractor.java (window around best match + match positions)
├── core/
│   ├── ds/       (CustomArrayList, CustomHashMap, CustomHashSet, CustomPriorityQueue,
│   │              CustomQueue, CustomSort, Comparator)
│   ├── string/   (KMP, ZAlgorithm, AhoCorasick, RabinKarp, LevenshteinDistance, SuffixArray)
│   ├── text/     (TextNormalizer, Tokenizer, StopWordFilter, SimpleStemmer)
│   └── index/    (InvertedIndex, PositionalInvertedIndex, Posting, PostingList, DocumentStore)
├── model/        (Document, Section, TermStats, MatchInfo, EngineResult, EngineStats, SearchQuery)
├── repository/   (CorpusLoader interface + ClasspathCorpusLoader + DocumentRepository)
└── exception/    (ApiException, NotFoundException, BadRequestException, GlobalExceptionHandler)
```

Do not add more layers "for appearance" — this set is sufficient.

---

## 7. Core Data Structures (ALL hand-written — do not substitute java.util)

| Class | Design | Used for |
|---|---|---|
| `CustomArrayList<T>` | growable `Object[]`, amortized O(1) `add`, `get/set/addAt/remove/size/isEmpty/contains`, unchecked unsafe get for hot loops | everywhere |
| `CustomHashMap<K,V>` | open addressing, power-of-2 capacity, load factor 0.75, hash mixing (`h ^= h >>> 16`), `put/get/containsKey/remove/size/keySet()/values()` returning custom collections | term→postings, token freq, doc store |
| `CustomHashSet<T>` | backed by `CustomHashMap<T,Boolean>`; `add/contains/size/iterator` | vocabulary, stop words, dedup |
| `CustomPriorityQueue<T>` | binary min-heap + `Comparator<T>`; `offer/poll/peek/size`; used as bounded top-K (drop when `size>K` and root worse) | top-K ranked results |
| `CustomQueue<T>` | ring-buffer array queue `offer/poll/peek` | BFS for Aho-Corasick failure links |
| `CustomSort` | `mergeSort` (stable, required for relevance ties) + `quickSort` (median-of-3) over `CustomArrayList`; both take a `Comparator` | all sorting |
| `Comparator<T>` (own, in `core.ds`) | single method `int compare(T a, T b)` — avoid java.util entirely | all sorts/heaps |
| `SuffixArray` | O(n log² n) doubling; `search(text, pattern)` → match range via 2 binary searches | suffix-based search mode |

Viva talking points to preserve in code comments: amortized analysis of ArrayList growth,
open addressing vs chaining, heap property + why min-heap for top-K, stability of merge sort.

---

## 8. Search Algorithms (ALL hand-written; each genuinely distinct)

| Algorithm | Complexity | Role in system |
|---|---|---|
| **KMP** | O(n+m) time, O(m) space | exact phrase matching (auto default for exact mode) |
| **Z Algorithm** | O(n+m) | alternative exact matcher |
| **Aho-Corasick** | O(n + m + z) | multi-pattern: ALL query terms in ONE pass per doc (partial mode default); gives per-term counts + positions |
| **Rabin-Karp** | O(n+m) avg | rolling-hash matcher with verification; multi-pattern via pattern hash set |
| **Levenshtein** | O(n·m) DP, early-abandon band | fuzzy mode: per query term scan vocabulary, threshold ≈ `max(1, len/3)` capped 2–3, expand to close terms then index lookup |
| **Suffix Array** | build O(n log² n), search O(m log n) | optional `algorithm=suffix` for exact mode |
| **InvertedIndex** | term → {docId, tf} | fast candidate generation (partial/fuzzy) |
| **PositionalInvertedIndex** | term → docId → sorted int[] positions | phrase/proximity, positional intersection |

**Algorithm selection must be REAL dispatch** — the chosen algorithm actually executes and
the response reports what ran. `auto` resolves per mode (exact→KMP, partial→Aho-Corasick,
fuzzy→Levenshtein) and reports the resolved id.

---

## 9. Document Model

```java
Document {
  String id;              // stable slug: "operating-system"
  String title;           // from front-matter or derived from filename
  String path;            // "corpus/computer-science/operating-system.txt"
  String docType;         // Wikipedia | Article | Reference  (front-matter `type:`)
  String category;        // folder name, e.g. "computer-science" (extensible)
  String content;         // full raw UTF-8 text (never destroyed by normalization)
  String overview;        // first paragraph
  CustomArrayList<Section> sections;  // parsed from markdown-ish "## Heading" blocks
  int wordCount;          // measured at load/index time
  long sizeBytes;         // measured at load time
  String indexedDate;     // front-matter `date:` (ISO yyyy-MM-dd)
}
```
UTF-8 end-to-end; field names are language-neutral so a future Indian-language Wikipedia
corpus needs **no model change** — only corpus files (+ optionally configurable tokenizer
rules). Suffix-array build cost on long docs: build lazily per doc on first use, or cap by
document size and document the cap.

---

## 10. Corpus Strategy

- Startup corpus: **~25–30 real, hand-written English articles** (200–500 words each) on CS
  topics, stored at `src/main/resources/corpus/{category}/{doc-slug}.txt`.
- Optional tiny front-matter header lines at top of each file:
  ```
  type: Wikipedia
  date: 2024-01-15
  overview: One-sentence overview used by the UI
  ```
  followed by `## Section` headings and body paragraphs.
- `CorpusLoader` is an **interface** (`CustomArrayList<Document> load()`) with
  `ClasspathCorpusLoader` now; a future disk/bulk loader (Indian-language Wikipedia dump)
  implements the same interface — zero changes to engine/index/model.
- **No hardcoded mock search results anywhere.** Stats, counts, scores, times are all measured.

---

## 11. Indexing Strategy (built ONCE at startup, never per request)

```
Document.content
  → TextNormalizer   (lowercase, accent-fold, punctuation→space, whitespace collapse; unicode-safe)
  → Tokenizer        (alphanumeric runs; positions recorded)
  → StopWordFilter   (~40 English stop words in a CustomHashSet; positions re-normalized)
  → SimpleStemmer    (light, transparent suffix stripping: plurals, -ing, -ed, -tion; no external lib)
  → InvertedIndex.add(docId, term)                    (coarse index)
  → PositionalInvertedIndex.add(docId, term, pos)     (positional index)
  → DocumentStore.put(doc)
```
- Runs in `SearchEngineInitializer` (ApplicationRunner) at startup; logs real index stats.
- `POST /api/index/rebuild` re-runs it (demo/viva tool).
- Raw text is always retained — pure pattern modes can scan documents directly even if a
  term was stop-worded/stemmed away.

---

## 12. Search Pipeline (per request)

```
Query string
  → QueryParser: normalize → tokenize (same pipeline as indexing)
  → Mode:
      EXACT   → phrase = raw normalized query; per candidate doc run chosen exact algorithm
                (kmp | z-algo | rabin-karp | suffix) over raw text; match positions returned
      PARTIAL → Aho-Corasick automaton over ALL query tokens (one pass per doc);
                per-term match counts + positions; candidate set from inverted index
      FUZZY   → per query term: Levenshtein ≤ threshold over vocabulary (custom heap keeps
                top-K close terms) → expanded terms → index lookup → per-term matches
  → Candidate docs (positional index postings, or all docs for pure pattern modes)
  → Ranker scoring (see §13)
  → docType filter → CustomSort (relevance | newest | alphabetical)
  → server-side pagination
  → SnippetExtractor per page item (window around best match, original casing, match offsets)
  → EngineResult (results, totals, real timings, algorithmUsed, documentsScanned, totalMatches)
```

---

## 13. Ranking Strategy (explainable at viva)

```
score = 0.45 × tfIdf       = Σ_t ( tf(t,d) / docLen(d) ) × log( N / df(t) )   [normalized]
      + 0.25 × titleBoost  = fraction of query terms present in the title
      + 0.15 × phraseBoost = 1.0 if full phrase occurs (exact / positional check)
      + 0.15 × proximity   = positional closeness of matched terms (positional index)
relevance (API) = round(score_normalized × 100)      // 0–100 integer for the UI bar
```
- Top-K via `CustomPriorityQueue` min-heap of size K; final ordering via `CustomSort.mergeSort`
  with a stable comparator (ties → title asc, then id).
- Every result carries `scoreBreakdown { tfidf, title, phrase, proximity }` — any rank can be
  explained on paper during the viva.

---

## 14. REST API Design

Base URL `http://localhost:8080`, JSON everywhere, CORS allow `http://localhost:8443`.

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/health` | liveness + indexReady + corpus document count |
| POST | `/api/search` | main search (filters/pagination in body) |
| GET | `/api/documents` | list documents (id, title, path, docType, category, size) |
| GET | `/api/documents/{id}` | full document detail |
| GET | `/api/suggestions?q=prefix` | prefix suggestions from vocabulary (CustomHashMap lookup) |
| GET | `/api/statistics` | real corpus/index numbers + measured avg search time |
| GET | `/api/algorithms` | algorithm catalog (ids match UI: kmp, z-algo, aho-corasick, rabin-karp, edit-distance, inverted-index) |
| GET | `/api/algorithms/performance` | measured per-algorithm avg/min/max ms + call counts (aggregated from real executions) |
| POST | `/api/index/rebuild` | rebuild index (viva demo tool) |

HTTP codes: 200 OK; 400 invalid body/params; 404 unknown document; 405 wrong method;
500 unexpected. Uniform error body (below). Do not fake any number — all timings/statistics
come from actual execution.

---

## 15. JSON Contracts

### POST /api/search — request
```json
{ "query": "operating system", "matchType": "exact", "algorithm": "auto",
  "docType": "all", "sort": "relevance", "page": 1, "pageSize": 10 }
```
Validation: query required non-blank (400); page ≥ 1; pageSize 1–50 (default 10);
matchType ∈ {exact, partial, fuzzy} (default partial); algorithm default "auto";
docType default "all"; sort default "relevance".

### POST /api/search — response
```json
{
  "query": "operating system",
  "totalResults": 42, "page": 1, "pageSize": 10, "totalPages": 5,
  "searchTimeMs": 12,
  "algorithmUsed": "kmp",
  "indexType": "positional-inverted",
  "documentsScanned": 30,
  "totalMatches": 613,
  "results": [
    { "id": "operating-system", "title": "Operating System",
      "path": "corpus/computer-science/operating-system.txt",
      "snippet": "An operating system is system software that manages…",
      "relevance": 94, "matches": 142, "algorithm": "KMP",
      "docType": "Wikipedia", "size": "12.4 KB", "date": "2024-01-15",
      "matchPositions": [4, 61, 109],
      "scoreBreakdown": { "tfidf": 0.41, "title": 0.25, "phrase": 0.15, "proximity": 0.13 } }
  ]
}
```
`algorithm` on each result = display name ("KMP"); `algorithmUsed` = id ("kmp").

### GET /api/documents/{id} — response (superset of UI DocumentData static fields)
```json
{ "id": "operating-system", "title": "Operating System",
  "path": "corpus/computer-science/operating-system.txt",
  "docType": "Wikipedia", "category": "computer-science",
  "size": "12.4 KB", "sizeBytes": 12698, "wordCount": 2847, "date": "2024-01-15",
  "overview": "…", "sections": [ { "heading": "…", "content": "…" } ] }
```
(Per-search fields matches/relevance/algorithm/pattern/searchTime are search-context and
come from /api/search results; the UI already falls back gracefully without them.)

### GET /api/statistics — response (all values real/measured)
```json
{ "totalDocuments": 30, "totalTokens": 128400, "uniqueTerms": 8471,
  "avgTokensPerDoc": 4280, "indexBuildTimeMs": 96, "indexCoveragePct": 100.0,
  "avgSearchTimeMs": 11, "totalSearches": 12,
  "docTypeCounts": { "Wikipedia": 10, "Article": 12, "Reference": 8 } }
```

### GET /api/algorithms — response
```json
{ "algorithms": [ { "id": "kmp", "name": "KMP", "fullName": "Knuth–Morris–Pratt",
    "purpose": "Linear-time exact pattern matching", "complexity": "O(n + m)",
    "spaceComplexity": "O(m)", "description": "…" } ] }
```

### GET /api/algorithms/performance — response
```json
{ "entries": [ { "algorithm": "kmp", "calls": 5, "avgMs": 8.2, "minMs": 3.0, "maxMs": 21.0 } ] }
```

### GET /api/health — `{ "status": "UP", "indexReady": true, "documents": 30 }`
### GET /api/suggestions?q=oper — `{ "suggestions": ["operating system", "operating systems", …] }`
### Error body (all errors)
```json
{ "status": 404, "error": "Not Found", "message": "No document with id 'xyz'",
  "path": "/api/documents/xyz", "timestamp": "2026-09-13T10:15:30Z" }
```

---

## 16. Testing Strategy (JUnit 5)

- **Pure unit tests** (no Spring): every `core.ds` structure (resize/collisions/empty/heap order);
  KMP & Z (classic cases, overlaps, no-match); Aho-Corasick (overlapping patterns, failure links);
  Rabin-Karp (collision verification path); Levenshtein (known distance tables, early-abandon);
  SuffixArray; TextNormalizer/Tokenizer/StopWordFilter/SimpleStemmer; InvertedIndex & positional
  (tf/df/phrase/proximity); Ranker (hand-computed score assertions); SnippetExtractor.
- **Engine integration:** exact vs partial vs fuzzy end-to-end on a tiny test corpus;
  pagination/sort invariants; determinism (same corpus+query → same output — golden invariant test).
- **Web layer (MockMvc):** every endpoint — status codes, JSON shape, validation errors, CORS header.
- **Maven:** Surefire runs everything; `mvn clean test` must be green; `mvn clean package` produces the jar.

---

## 17. Constraints & Important Decisions (DO NOT VIOLATE)

1. **No `java.util.*` in `core/**` and `engine/**`** — that's the point of the project.
   Spring layer (`controller/service/config/dto/exception`) may use java.util.
2. **No external search libraries** (Lucene/Elasticsearch/etc.). Only deps:
   `spring-boot-starter-web`, `spring-boot-starter-test` (test), `spring-boot-maven-plugin`.
3. **Never modify `Mini Search Engine UI Design/`.** Backend only.
4. **No fake data.** No random timings. Every number measured.
5. **Algorithm selection is real dispatch**, not a label.
6. **UTF-8 everywhere** (`pom` sourceEncoding; corpus is UTF-8; future multilingual).
7. **Backend port 8080** (UI dev server is 8443 — don't collide; CORS already planned).
8. Algorithm **ids must match the UI's ids**: `kmp, z-algo, aho-corasick, rabin-karp,
   edit-distance, inverted-index` + `auto` (see §4.3).
9. Engine stays framework-free (its tests don't boot Spring).
10. Build incrementally: compile + test after every stage (see BACKEND_TASKS.md).
11. Windows/Git-Bash environment: POSIX shell syntax; **quote paths with spaces**;
    `mvn -q` to reduce noise; Cp1252 platform encoding is why UTF-8 must be pinned in pom.

---

## 18. Current Implementation Status (as of this file's creation)

- ✅ UI fully inspected (all screens/components/data) — API contract extracted (§4)
- ✅ Backend folder verified empty; full plan produced and approved by user
- ✅ Toolchain verified: Java 17.0.16, Maven 3.9.16 (§2)
- ✅ This plan + BACKEND_TASKS.md written
- ⬜ No backend code written yet — implementation starts at Stage 1 in BACKEND_TASKS.md

Stage 1 (Maven skeleton + core.ds + tests) is **approved** and is the next action.
