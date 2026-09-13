import Logo from "../components/Logo"
import SearchBar from "../components/SearchBar"
import { CORPUS_STATS, SEARCH_SUGGESTIONS } from "../data"
import type { Screen } from "../types"
import { GitHubIcon, ZapIcon } from "../icons"

type Props = {
  onSearch: (query: string) => void
  onNavigate: (screen: Screen) => void
}

const stats = [
  { value: CORPUS_STATS.totalDocuments, label: "Documents Indexed" },
  { value: CORPUS_STATS.avgSearchTime, label: "Avg. Search Time" },
  { value: `${CORPUS_STATS.algorithms}`, label: "Search Algorithms" },
  { value: CORPUS_STATS.indexCoverage, label: "Index Coverage" },
]

export default function SearchHome({ onSearch, onNavigate }: Props) {
  return (
    <div className="min-h-screen flex flex-col bg-page-bg">
      {/* Top nav */}
      <header className="flex-shrink-0">
        <div className="max-w-screen-xl mx-auto px-6 h-16 flex items-center justify-between">
          <Logo onClick={() => onNavigate("home")} />
          <nav className="flex items-center gap-1">
            <button
              onClick={() => onNavigate("home")}
              className="px-3 py-1.5 text-sm text-slate-600 hover:text-navy hover:bg-white rounded-lg transition-colors font-medium"
            >
              Search
            </button>
            <button
              onClick={() => onNavigate("technical")}
              className="px-3 py-1.5 text-sm text-slate-600 hover:text-navy hover:bg-white rounded-lg transition-colors font-medium"
            >
              About
            </button>
          </nav>
        </div>
      </header>

      {/* Hero */}
      <main className="flex-1 flex flex-col items-center justify-center px-6 py-12">
        <div className="w-full max-w-2xl mx-auto">
          {/* Badge */}
          <div className="flex justify-center mb-8">
            <div className="inline-flex items-center gap-2 px-3 py-1.5 bg-accent-pale border border-blue-200 rounded-full">
              <ZapIcon className="w-3.5 h-3.5 text-accent" />
              <span className="text-xs font-medium text-accent">Information Retrieval System</span>
            </div>
          </div>

          {/* Heading */}
          <h1 className="text-4xl sm:text-5xl font-bold text-navy text-center leading-tight tracking-tight mb-4">
            Search the Knowledge Base
          </h1>
          <p className="text-base text-slate-500 text-center mb-10 leading-relaxed">
            Fast information retrieval powered by custom pattern matching algorithms and a positional inverted index.
          </p>

          {/* Search bar */}
          <SearchBar onSearch={onSearch} autoFocus size="large" />

          {/* Suggestions */}
          <div className="mt-8">
            <p className="text-xs text-slate-400 text-center mb-3 font-medium uppercase tracking-wider">
              Try searching for
            </p>
            <div className="flex flex-wrap items-center justify-center gap-2">
              {SEARCH_SUGGESTIONS.map((s) => (
                <button
                  key={s}
                  onClick={() => onSearch(s)}
                  className="px-3.5 py-1.5 text-sm border border-slate-300 rounded-full text-slate-600 hover:bg-white hover:border-slate-400 hover:text-navy hover:shadow-sm transition-all duration-150"
                >
                  {s}
                </button>
              ))}
            </div>
          </div>

          {/* Stats */}
          <div className="mt-14 bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
            <div className="grid grid-cols-2 sm:grid-cols-4">
              {stats.map((stat, i) => (
                <div
                  key={stat.label}
                  className={`px-6 py-5 text-center ${i !== stats.length - 1 ? "border-r border-slate-100 last:border-r-0" : ""} ${i >= 2 ? "border-t sm:border-t-0 border-slate-100" : ""}`}
                >
                  <div className="text-2xl font-bold font-mono text-navy">{stat.value}</div>
                  <div className="text-xs text-slate-500 mt-1">{stat.label}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="flex-shrink-0 border-t border-slate-200 bg-white">
        <div className="max-w-screen-xl mx-auto px-6 py-4 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500">
          <span>Mini Search Engine · University Semester Project · 2024</span>
          <div className="flex items-center gap-4">
            <button
              onClick={() => onNavigate("technical")}
              className="hover:text-slate-700 transition-colors"
            >
              Project details
            </button>
            <span className="flex items-center gap-1.5 text-slate-400" aria-label="University semester project">
              <GitHubIcon className="w-3.5 h-3.5" />
              Academic project
            </span>
          </div>
        </div>
      </footer>
    </div>
  )
}
