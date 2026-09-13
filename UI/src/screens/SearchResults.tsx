import { useState } from "react"
import Logo from "../components/Logo"
import SearchBar from "../components/SearchBar"
import { MOCK_RESULTS, ALGORITHMS } from "../data"
import { filterAndSortResults, highlightTerms, formatDate, getDocTypeCounts } from "../utils"
import type { Filters, Screen, SearchResult } from "../types"
import { FilterIcon, XIcon, ChevronRightIcon, ExternalLinkIcon, SearchIcon, AlertCircleIcon } from "../icons"

const RESULTS_PER_PAGE = 5
const TOTAL_PAGES = 8

type Props = {
  query: string
  filters: Filters
  isLoading: boolean
  currentPage: number
  onSearch: (query: string) => void
  onOpenDocument: (id: string) => void
  onNavigate: (screen: Screen) => void
  onFilterChange: (key: keyof Filters, value: string) => void
  onPageChange: (page: number) => void
}

function RelevanceBar({ value }: { value: number }) {
  const color = value >= 80 ? "bg-accent" : value >= 60 ? "bg-blue-400" : "bg-slate-400"
  return (
    <div className="flex items-center gap-2">
      <div className="w-16 h-1.5 bg-slate-100 rounded-full overflow-hidden flex-shrink-0">
        <div className={`h-full ${color} rounded-full transition-all`} style={{ width: `${value}%` }} />
      </div>
      <span className={`text-xs font-mono font-semibold ${value >= 80 ? "text-accent" : value >= 60 ? "text-blue-500" : "text-slate-500"}`}>
        {value}%
      </span>
    </div>
  )
}

function AlgoBadge({ algorithm }: { algorithm: string }) {
  return (
    <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-mono font-medium bg-slate-100 text-slate-600 border border-slate-200">
      {algorithm}
    </span>
  )
}

function ResultItem({ result, query, onOpen }: { result: SearchResult; query: string; onOpen: (id: string) => void }) {
  return (
    <article className="group py-6 border-b border-slate-100 last:border-0">
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 sm:gap-6">
        <div className="flex-1 min-w-0">
          <button
            onClick={() => onOpen(result.id)}
            className="text-left w-full"
          >
            <h3 className="text-lg font-semibold text-accent hover:text-accent-dark group-hover:underline underline-offset-2 mb-0.5 leading-snug transition-colors">
              {result.title}
            </h3>
          </button>
          <p className="text-xs font-mono text-path-green mb-2 truncate">{result.path}</p>
          <p className="text-sm text-slate-600 leading-relaxed mb-3 line-clamp-2">
            {highlightTerms(result.snippet, query)}
          </p>
          {/* Metadata row */}
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5">
            <RelevanceBar value={result.relevance} />
            <AlgoBadge algorithm={result.algorithm} />
            <span className="text-xs text-slate-400">
              <span className="font-mono text-slate-600">{result.matches}</span> matches
            </span>
            <span className="text-xs text-slate-400">
              <span className="font-mono text-slate-600">{result.size}</span>
            </span>
            <span className="text-xs text-slate-400">{result.docType}</span>
            <span className="text-xs text-slate-400">{formatDate(result.date)}</span>
          </div>
        </div>
        {/* Action buttons */}
        <div className="flex flex-row sm:flex-col gap-1.5 flex-shrink-0 pt-0.5">
          <button
            onClick={() => onOpen(result.id)}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-navy border border-slate-200 rounded-lg hover:bg-slate-50 hover:border-slate-300 transition-colors"
          >
            <ExternalLinkIcon className="w-3.5 h-3.5" />
            Open
          </button>
          <button
            onClick={() => onOpen(result.id)}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-600 border border-slate-200 rounded-lg hover:bg-slate-50 hover:border-slate-300 transition-colors"
          >
            <ChevronRightIcon className="w-3.5 h-3.5" />
            Details
          </button>
        </div>
      </div>
    </article>
  )
}

function ResultSkeleton() {
  return (
    <div className="py-6 border-b border-slate-100 animate-pulse">
      <div className="h-5 bg-slate-200 rounded-md w-48 mb-2" />
      <div className="h-3 bg-slate-100 rounded w-52 mb-3" />
      <div className="space-y-1.5 mb-3">
        <div className="h-3.5 bg-slate-100 rounded" />
        <div className="h-3.5 bg-slate-100 rounded w-11/12" />
        <div className="h-3.5 bg-slate-100 rounded w-4/5" />
      </div>
      <div className="flex items-center gap-4">
        <div className="h-3 bg-slate-100 rounded w-20" />
        <div className="h-4 bg-slate-100 rounded w-10" />
        <div className="h-3 bg-slate-100 rounded w-16" />
        <div className="h-3 bg-slate-100 rounded w-12" />
      </div>
    </div>
  )
}

function EmptyState({ query, onClear }: { query: string; onClear: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="w-16 h-16 bg-slate-100 rounded-2xl flex items-center justify-center mb-5">
        <SearchIcon className="w-7 h-7 text-slate-400" />
      </div>
      <h3 className="text-xl font-semibold text-slate-900 mb-2">No results found</h3>
      <p className="text-slate-500 mb-6 text-sm">
        No documents matched{" "}
        <span className="font-mono text-slate-700">"{query}"</span> with the current filters.
      </p>
      <ul className="text-sm text-slate-500 space-y-1 mb-8 text-left">
        <li className="flex items-center gap-2"><span className="text-slate-300">·</span> Check your spelling</li>
        <li className="flex items-center gap-2"><span className="text-slate-300">·</span> Try fewer keywords</li>
        <li className="flex items-center gap-2"><span className="text-slate-300">·</span> Try a broader topic</li>
        <li className="flex items-center gap-2"><span className="text-slate-300">·</span> Change the Document Type filter</li>
      </ul>
      <button
        onClick={onClear}
        className="px-5 py-2.5 bg-accent text-white rounded-lg text-sm font-medium hover:bg-accent-dark transition-colors"
      >
        Clear Search
      </button>
    </div>
  )
}

type FilterSectionProps = {
  title: string
  options: { label: string; value: string; count?: number }[]
  value: string
  onChange: (value: string) => void
}

function FilterSection({ title, options, value, onChange }: FilterSectionProps) {
  return (
    <div className="mb-7">
      <h3 className="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-3">{title}</h3>
      <div className="space-y-1">
        {options.map((opt) => (
          <button
            key={opt.value}
            type="button"
            aria-pressed={value === opt.value}
            onClick={() => onChange(opt.value)}
            className="w-full flex items-center gap-2.5 cursor-pointer group py-1 text-left rounded-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/40"
          >
            <div
              className={`w-4 h-4 rounded-full border-2 flex items-center justify-center flex-shrink-0 transition-colors ${
                value === opt.value
                  ? "border-accent bg-accent"
                  : "border-slate-300 group-hover:border-slate-400"
              }`}
            >
              {value === opt.value && <div className="w-1.5 h-1.5 bg-white rounded-full" />}
            </div>
            <span
              className={`text-sm transition-colors flex-1 ${value === opt.value ? "text-slate-900 font-medium" : "text-slate-600 group-hover:text-slate-800"}`}
            >
              {opt.label}
            </span>
            {opt.count !== undefined && (
              <span className="text-xs text-slate-400 font-mono">{opt.count}</span>
            )}
          </button>
        ))}
      </div>
    </div>
  )
}

function AlgorithmPicker({
  value,
  onChange,
}: {
  value: string
  onChange: (v: string) => void
}) {
  const algoOptions = [{ id: "auto", name: "Auto" }, ...ALGORITHMS.map((a) => ({ id: a.id, name: a.name }))]
  return (
    <div className="mb-7">
      <h3 className="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-3">Search Algorithm</h3>
      <div className="space-y-1">
        {algoOptions.map((algo) => (
          <button
            key={algo.id}
            onClick={() => onChange(algo.id)}
            className={`w-full text-left px-3 py-2 text-sm rounded-lg flex items-center justify-between transition-colors ${
              value === algo.id
                ? "bg-accent-pale border border-blue-200 text-accent font-medium"
                : "text-slate-600 hover:bg-slate-50 border border-transparent"
            }`}
          >
            <span className="font-mono">{algo.name}</span>
            {value === algo.id && (
              <span className="text-xs text-accent/70">active</span>
            )}
          </button>
        ))}
      </div>
    </div>
  )
}

function Pagination({ current, total, onChange }: { current: number; total: number; onChange: (p: number) => void }) {
  const pages = Array.from({ length: Math.min(5, total) }, (_, i) => i + 1)
  return (
    <div className="flex items-center justify-center gap-1 pt-6 pb-2">
      <button
        onClick={() => onChange(Math.max(1, current - 1))}
        disabled={current === 1}
        className="px-3 py-2 text-sm text-slate-600 hover:bg-white hover:text-navy rounded-lg disabled:opacity-40 disabled:cursor-not-allowed transition-colors border border-transparent hover:border-slate-200"
      >
        Prev
      </button>
      {pages.map((p) => (
        <button
          key={p}
          onClick={() => onChange(p)}
          className={`w-9 h-9 text-sm rounded-lg transition-colors font-medium ${
            p === current
              ? "bg-navy text-white"
              : "text-slate-600 hover:bg-white hover:text-navy hover:border-slate-200 border border-transparent"
          }`}
        >
          {p}
        </button>
      ))}
      {total > 5 && (
        <>
          <span className="px-2 text-slate-400 text-sm">...</span>
          <button
            onClick={() => onChange(total)}
            className="w-9 h-9 text-sm rounded-lg text-slate-600 hover:bg-white hover:text-navy hover:border-slate-200 border border-transparent transition-colors font-medium"
          >
            {total}
          </button>
        </>
      )}
      <button
        onClick={() => onChange(Math.min(total, current + 1))}
        disabled={current === total}
        className="px-3 py-2 text-sm text-slate-600 hover:bg-white hover:text-navy rounded-lg disabled:opacity-40 disabled:cursor-not-allowed transition-colors border border-transparent hover:border-slate-200"
      >
        Next
      </button>
    </div>
  )
}

export default function SearchResults({
  query,
  filters,
  isLoading,
  currentPage,
  onSearch,
  onOpenDocument,
  onNavigate,
  onFilterChange,
  onPageChange,
}: Props) {
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false)

  const allResults = filterAndSortResults(MOCK_RESULTS, filters)
  const paginatedResults = allResults.slice((currentPage - 1) * RESULTS_PER_PAGE, currentPage * RESULTS_PER_PAGE)
  const counts = getDocTypeCounts(MOCK_RESULTS)

  const activeAlgoName = filters.algorithm === "auto"
    ? "Auto (KMP)"
    : ALGORITHMS.find((a) => a.id === filters.algorithm)?.name ?? filters.algorithm

  const docTypeOptions = [
    { label: "All", value: "all", count: counts.all },
    { label: "Wikipedia", value: "Wikipedia", count: counts["Wikipedia"] },
    { label: "Article", value: "Article", count: counts["Article"] },
    { label: "Reference", value: "Reference", count: counts["Reference"] },
  ]

  const matchTypeOptions = [
    { label: "Exact Match", value: "exact" },
    { label: "Partial Match", value: "partial" },
    { label: "Fuzzy Match", value: "fuzzy" },
  ]

  const sortOptions = [
    { label: "Relevance", value: "relevance" },
    { label: "Newest", value: "newest" },
    { label: "Alphabetical", value: "alphabetical" },
  ]

  const FilterContent = () => (
    <>
      <FilterSection title="Document Type" options={docTypeOptions} value={filters.docType} onChange={(v) => onFilterChange("docType", v)} />
      <FilterSection title="Match Type" options={matchTypeOptions} value={filters.matchType} onChange={(v) => onFilterChange("matchType", v)} />
      <FilterSection title="Sort By" options={sortOptions} value={filters.sortBy} onChange={(v) => onFilterChange("sortBy", v)} />
      <AlgorithmPicker value={filters.algorithm} onChange={(v) => onFilterChange("algorithm", v)} />
    </>
  )

  return (
    <div className="min-h-screen flex flex-col bg-page-bg">
      {/* Compact sticky header */}
      <header className="sticky top-0 z-20 bg-white border-b border-slate-200 shadow-[0_1px_3px_rgba(0,0,0,0.04)]">
        <div className="max-w-screen-xl mx-auto px-4 sm:px-6 h-16 flex items-center gap-3 sm:gap-6">
          <div className="hidden xs:block">
            <Logo onClick={() => onNavigate("home")} />
          </div>
          <div className="flex-1 max-w-xl min-w-0">
            <SearchBar value={query} onSearch={onSearch} size="compact" />
          </div>
          <nav className="hidden sm:flex items-center gap-1 ml-auto flex-shrink-0">
            <button
              onClick={() => onNavigate("home")}
              className="px-3 py-1.5 text-sm text-slate-600 hover:text-navy hover:bg-slate-50 rounded-lg transition-colors font-medium"
            >
              Search
            </button>
            <button
              onClick={() => onNavigate("technical")}
              className="px-3 py-1.5 text-sm text-slate-600 hover:text-navy hover:bg-slate-50 rounded-lg transition-colors font-medium"
            >
              Technical
            </button>
          </nav>
        </div>
      </header>

      <main className="flex-1 max-w-screen-xl mx-auto w-full px-4 sm:px-6 py-6">
        {/* Search info bar */}
        <div className="mb-5 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
          <div>
            {isLoading ? (
              <div className="flex items-center gap-2 text-sm text-slate-500">
                <div className="w-3.5 h-3.5 border-2 border-accent border-t-transparent rounded-full animate-spin" />
                Searching the knowledge base&hellip;
              </div>
            ) : (
              <>
                <p className="text-sm text-slate-700">
                  <span className="font-semibold text-slate-900">{allResults.length.toLocaleString()} results</span> found for{" "}
                  <span className="font-mono text-navy">"{query}"</span>{" "}
                  <span className="text-slate-400">in</span>{" "}
                  <span className="font-mono text-slate-700">24 ms</span>
                </p>
                <p className="text-xs text-slate-400 mt-0.5 font-mono">
                  Algorithm: <span className="text-slate-600">{activeAlgoName}</span>
                  {" "}·{" "}
                  Index: <span className="text-slate-600">Positional Inverted Index</span>
                </p>
              </>
            )}
          </div>
          <button
            className="flex md:hidden items-center gap-2 px-4 py-2 border border-slate-300 bg-white rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors self-start"
            onClick={() => setMobileFiltersOpen(true)}
          >
            <FilterIcon className="w-4 h-4" />
            Filters
          </button>
        </div>

        <div className="flex gap-8">
          {/* Desktop sidebar */}
          <aside className="hidden md:block w-52 flex-shrink-0">
            <div className="bg-white border border-slate-200 rounded-xl p-5 sticky top-24">
              <div className="flex items-center justify-between mb-5">
                <h2 className="font-semibold text-sm text-slate-900">Filters</h2>
                <FilterIcon className="w-4 h-4 text-slate-400" />
              </div>
              <FilterContent />
            </div>
          </aside>

          {/* Main results */}
          <div className="flex-1 min-w-0">
            {isLoading ? (
              <div className="bg-white border border-slate-200 rounded-xl px-6">
                {Array.from({ length: 5 }).map((_, i) => (
                  <ResultSkeleton key={i} />
                ))}
              </div>
            ) : paginatedResults.length === 0 ? (
              <div className="bg-white border border-slate-200 rounded-xl">
                <EmptyState query={query} onClear={() => onSearch("")} />
              </div>
            ) : (
              <>
                <div className="bg-white border border-slate-200 rounded-xl px-6">
                  {paginatedResults.map((result) => (
                    <ResultItem
                      key={result.id}
                      result={result}
                      query={query}
                      onOpen={onOpenDocument}
                    />
                  ))}
                </div>
                <div className="bg-white border border-slate-200 rounded-xl mt-2">
                  <Pagination
                    current={currentPage}
                    total={TOTAL_PAGES}
                    onChange={onPageChange}
                  />
                </div>
              </>
            )}
          </div>
        </div>
      </main>

      {/* Mobile filters sheet */}
      {mobileFiltersOpen && (
        <div className="fixed inset-0 z-50 md:hidden">
          <div
            className="absolute inset-0 bg-black/40 backdrop-blur-sm"
            onClick={() => setMobileFiltersOpen(false)}
          />
          <div className="absolute bottom-0 left-0 right-0 bg-white rounded-t-2xl p-6 max-h-[85vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-5">
              <h2 className="font-semibold text-slate-900">Filters</h2>
              <button
                onClick={() => setMobileFiltersOpen(false)}
                className="p-1.5 text-slate-500 hover:text-slate-700 rounded-lg hover:bg-slate-100 transition-colors"
              >
                <XIcon className="w-5 h-5" />
              </button>
            </div>
            <FilterContent />
            <button
              onClick={() => setMobileFiltersOpen(false)}
              className="w-full mt-2 py-3 bg-navy text-white rounded-xl font-medium text-sm"
            >
              Apply Filters
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
