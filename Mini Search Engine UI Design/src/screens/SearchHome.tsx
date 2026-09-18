import { useState, useEffect } from "react"
import Logo from "../components/Logo"
import SearchBar from "../components/SearchBar"
import AdvancedSearchModal from "../components/AdvancedSearchModal"
import type { Screen, Filters } from "../types"
import { GitHubIcon, ZapIcon, FilterIcon } from "../icons"
import { fetchStatistics, fetchSuggestions } from "../api"

type Props = {
  onSearch: (query: string, algorithm?: string) => void
  onAdvancedSearch?: (query: string, filters: Partial<Filters>) => void
  onNavigate: (screen: Screen) => void
}

interface SearchMode {
  id: string
  label: string
  shortBadge: string
  description: string
  complexity: string
}

const SEARCH_MODES: SearchMode[] = [
  {
    id: "smart",
    label: "Smart Search",
    shortBadge: "VSM / TF-IDF",
    description: "Inverted Index + TF-IDF Vector Space Model with Cosine Similarity relevance ranking.",
    complexity: "O(n+m)",
  },
  {
    id: "exact-pattern",
    label: "Exact Pattern",
    shortBadge: "KMP",
    description: "Knuth-Morris-Pratt deterministic pattern matching with LPS prefix-suffix failure table.",
    complexity: "O(n+m)",
  },
  {
    id: "multi-pattern",
    label: "Multi-Pattern",
    shortBadge: "Aho-Corasick",
    description: "Simultaneous multi-keyword dictionary matching using a finite-state trie automaton.",
    complexity: "O(n+m+z)",
  },
  {
    id: "fuzzy",
    label: "Fuzzy Search",
    shortBadge: "Levenshtein",
    description: "Dynamic programming edit-distance matching tolerant to typos and spelling errors.",
    complexity: "O(n×m)",
  },
]

const CURATED_EXAMPLES = [
  "binary search tree",
  "operating system",
  "machine learning",
  "virtual memory",
  "deadlock detection",
  "graph traversal",
  "cpu scheduling",
]

export default function SearchHome({ onSearch, onAdvancedSearch, onNavigate }: Props) {
  const [selectedMode, setSelectedMode] = useState<string>("smart")
  const [showAdvancedModal, setShowAdvancedModal] = useState<boolean>(false)
  const [statsData, setStatsData] = useState({
    totalDocuments: "69",
    avgSearchTime: "7 ms",
    algorithms: "7",
    indexCoverage: "100%",
  })
  const [examples, setExamples] = useState<string[]>(CURATED_EXAMPLES)

  useEffect(() => {
    fetchStatistics().then((stats) => {
      setStatsData({
        totalDocuments: stats.totalDocuments.toLocaleString(),
        avgSearchTime: `${stats.avgSearchTimeMs || 7} ms`,
        algorithms: "7",
        indexCoverage: `${stats.indexCoveragePct.toFixed(0)}%`,
      })
    })

    fetchSuggestions("").then((suggs) => {
      if (suggs && suggs.length > 0) {
        // Merge with curated examples without duplicates
        const combined = Array.from(new Set([...CURATED_EXAMPLES, ...suggs])).slice(0, 8)
        setExamples(combined)
      }
    })
  }, [])

  const currentModeInfo = SEARCH_MODES.find((m) => m.id === selectedMode) || SEARCH_MODES[0]

  const handleSearchSubmit = (query: string) => {
    onSearch(query, selectedMode)
  }

  const handleApplyAdvancedSearch = (q: string, f: Partial<Filters>) => {
    if (onAdvancedSearch) {
      onAdvancedSearch(q, f)
    } else {
      onSearch(q, f.algorithm || selectedMode)
    }
  }

  const stats = [
    { value: statsData.totalDocuments, label: "Documents Indexed" },
    { value: statsData.avgSearchTime, label: "Avg. Search Latency" },
    { value: statsData.algorithms, label: "Algorithms Built" },
    { value: statsData.indexCoverage, label: "Index Coverage" },
  ]


  return (
    <div className="min-h-screen flex flex-col bg-page-bg">
      {/* Top navigation */}
      <header className="flex-shrink-0">
        <div className="max-w-screen-xl mx-auto px-6 h-16 flex items-center justify-between">
          <Logo onClick={() => onNavigate("home")} />
          <nav className="flex items-center gap-1">
            <button
              onClick={() => onNavigate("home")}
              className="px-3 py-1.5 text-sm text-navy bg-white shadow-xs rounded-lg transition-colors font-medium"
            >
              Search
            </button>
            <button
              onClick={() => onNavigate("technical")}
              className="px-3 py-1.5 text-sm text-slate-600 hover:text-navy hover:bg-white rounded-lg transition-colors font-medium cursor-pointer"
            >
              Algorithms & Architecture
            </button>
          </nav>
        </div>
      </header>

      {/* Main hero area */}
      <main className="flex-1 flex flex-col items-center justify-center px-4 sm:px-6 py-8 sm:py-14">
        <div className="w-full max-w-3xl mx-auto">
          {/* Branded Badge */}
          <div className="flex justify-center mb-6">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 bg-blue-50/80 border border-blue-200/90 rounded-full shadow-2xs">
              <ZapIcon className="w-3.5 h-3.5 text-accent" />
              <span className="text-xs font-semibold text-accent tracking-tight">
                Information Retrieval & Pattern Matching Engine
              </span>
            </div>
          </div>

          {/* Title & Short Description */}
          <h1 className="text-4xl sm:text-5xl font-extrabold text-navy text-center leading-tight tracking-tight mb-3">
            Mini Search Engine
          </h1>
          <p className="text-base text-slate-600 text-center mb-8 max-w-xl mx-auto leading-relaxed">
            A high-performance algorithmic search engine combining a Positional Inverted Index with
            TF-IDF Cosine Similarity ranking and exact/fuzzy string matching algorithms.
          </p>

          {/* Search Mode Selector */}
          <div className="mb-4">
            <div className="flex items-center justify-between px-1 mb-2">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                Search Mode Selector
              </span>
              <span className="text-[11px] font-mono text-slate-400">
                Complexity: <span className="text-navy font-semibold">{currentModeInfo.complexity}</span>
              </span>
            </div>

            {/* Mode Pills */}
            <div className="flex flex-wrap items-center gap-1.5 bg-white/90 p-1.5 rounded-2xl border border-slate-200/90 shadow-2xs">
              {SEARCH_MODES.map((mode) => {
                const isActive = selectedMode === mode.id
                return (
                  <button
                    key={mode.id}
                    type="button"
                    onClick={() => setSelectedMode(mode.id)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-medium transition-all duration-150 cursor-pointer flex items-center gap-1.5 ${
                      isActive
                        ? "bg-navy text-white shadow-xs font-semibold"
                        : "text-slate-600 hover:text-navy hover:bg-slate-100"
                    }`}
                  >
                    <span>{mode.label}</span>
                    <span
                      className={`text-[10px] font-mono px-1.5 py-0.2 rounded-md ${
                        isActive ? "bg-white/20 text-white" : "bg-slate-100 text-slate-500"
                      }`}
                    >
                      {mode.shortBadge}
                    </span>
                  </button>
                )
              })}
            </div>

            {/* Mode Description Banner */}
            <div className="mt-2 px-3 py-1.5 bg-slate-100/70 border border-slate-200/60 rounded-xl flex items-center justify-between text-xs text-slate-600">
              <p className="leading-snug truncate">
                <span className="font-semibold text-slate-800">{currentModeInfo.label}:</span>{" "}
                {currentModeInfo.description}
              </p>
            </div>
          </div>

          {/* Large Search Bar */}
          <div className="relative">
            <SearchBar onSearch={handleSearchSubmit} autoFocus size="large" />
          </div>

          {/* Advanced Search Action Trigger */}
          <div className="mt-3 flex items-center justify-center">
            <button
              type="button"
              onClick={() => setShowAdvancedModal(true)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-slate-500 hover:text-accent hover:bg-blue-50 transition-colors cursor-pointer"
            >
              <FilterIcon className="w-3.5 h-3.5" />
              <span>Advanced Search (Phrase, Includes, Excludes & Language)</span>
            </button>
          </div>

          {/* Curated Example Searches */}
          <div className="mt-7">
            <p className="text-xs text-slate-400 text-center mb-3 font-medium uppercase tracking-wider">
              Example Searches
            </p>
            <div className="flex flex-wrap items-center justify-center gap-2">
              {examples.map((s) => (
                <button
                  key={s}
                  onClick={() => handleSearchSubmit(s)}
                  className="px-3.5 py-1.5 text-xs sm:text-sm bg-white border border-slate-200 hover:border-accent rounded-full text-slate-700 hover:text-accent hover:shadow-xs transition-all duration-150 cursor-pointer font-medium"
                >
                  "{s}"
                </button>
              ))}
            </div>
          </div>

          {/* Corpus Statistics Cards */}
          <div className="mt-12 bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-xs">
            <div className="grid grid-cols-2 sm:grid-cols-4">
              {stats.map((stat, i) => (
                <div
                  key={stat.label}
                  className={`px-5 py-4 text-center ${
                    i !== stats.length - 1 ? "border-r border-slate-100 last:border-r-0" : ""
                  } ${i >= 2 ? "border-t sm:border-t-0 border-slate-100" : ""}`}
                >
                  <div className="text-2xl font-bold font-mono text-navy">{stat.value}</div>
                  <div className="text-xs text-slate-500 mt-0.5">{stat.label}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </main>

      {/* Advanced Search Modal */}
      <AdvancedSearchModal
        isOpen={showAdvancedModal}
        onClose={() => setShowAdvancedModal(false)}
        currentQuery=""
        currentFilters={{
          docType: "all",
          category: "all",
          language: "all",
          matchType: "exact",
          sortBy: "relevance",
          algorithm: selectedMode,
        }}
        onApplyAdvancedSearch={handleApplyAdvancedSearch}
      />

      {/* Footer */}
      <footer className="flex-shrink-0 border-t border-slate-200 bg-white">
        <div className="max-w-screen-xl mx-auto px-6 py-4 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500">
          <span>Mini Search Engine · Data Structures & Algorithms Project</span>
          <div className="flex items-center gap-4">
            <button
              onClick={() => onNavigate("technical")}
              className="hover:text-navy font-medium transition-colors cursor-pointer"
            >
              Technical Specifications
            </button>
            <span className="flex items-center gap-1.5 text-slate-400">
              <GitHubIcon className="w-3.5 h-3.5" />
              Academic Implementation
            </span>
          </div>
        </div>
      </footer>
    </div>
  )

}
