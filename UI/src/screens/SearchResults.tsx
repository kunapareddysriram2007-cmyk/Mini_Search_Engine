import { useState, useEffect } from "react"
import Logo from "../components/Logo"
import SearchBar from "../components/SearchBar"
import AdvancedSearchModal from "../components/AdvancedSearchModal"
import AlgorithmDetailsPanel from "../components/AlgorithmDetailsPanel"
import { highlightTerms, formatDate } from "../utils"
import type { Filters, Screen, SearchResult } from "../types"
import { FilterIcon, XIcon, ChevronRightIcon, SearchIcon } from "../icons"
import { executeSearch, fetchFilterMetadata, type SearchApiResponse, type FilterMetadata } from "../api"

const RESULTS_PER_PAGE = 5

type Props = {
  query: string
  filters: Filters
  isLoading: boolean
  currentPage: number
  onSearch: (query: string) => void
  onAdvancedSearch?: (query: string, filters: Partial<Filters>) => void
  onOpenDocument: (id: string) => void
  onNavigate: (screen: Screen) => void
  onFilterChange: (key: keyof Filters, value: string) => void
  onPageChange: (page: number) => void
}


function RelevanceBar({ value }: { value: number }) {
  const color = value >= 80 ? "bg-accent" : value >= 50 ? "bg-blue-500" : "bg-slate-400"
  return (
    <div className="flex items-center gap-2">
      <div className="w-16 h-1.5 bg-slate-100 rounded-full overflow-hidden flex-shrink-0">
        <div className={`h-full ${color} rounded-full transition-all`} style={{ width: `${value}%` }} />
      </div>
      <span className={`text-xs font-mono font-semibold ${value >= 80 ? "text-accent" : value >= 50 ? "text-blue-600" : "text-slate-500"}`}>
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

function getMatchedTerms(query: string, text: string): string[] {
  const clean = query.replace(/["']/g, "").toLowerCase().trim()
  const terms = clean.split(/\s+/).filter((t) => t.length > 0)
  const lowerText = text.toLowerCase()
  const matched: string[] = []
  for (const term of terms) {
    const escaped = term.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")
    const regex = new RegExp(`\\b${escaped}\\b`, "i")
    if (regex.test(lowerText) && !matched.includes(term)) {
      matched.push(term)
    }
  }
  return matched
}

function ResultItem({
  result,
  query,
  onOpen,
  isBreakdownOpen,
  onToggleBreakdown,
}: {
  result: SearchResult
  query: string
  onOpen: (id: string) => void
  isBreakdownOpen: boolean
  onToggleBreakdown: () => void
}) {
  const sb = result.scoreBreakdown
  const isExactPhraseQuery = query.trim().startsWith('"') && query.trim().endsWith('"')
  const matchedTerms = (sb?.matchedTerms && sb.matchedTerms.length > 0)
    ? sb.matchedTerms
    : getMatchedTerms(query, result.title + " " + result.snippet)

  return (
    <article className="group py-6 border-b border-slate-100 last:border-0">
      <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 sm:gap-6">
        <div className="flex-1 min-w-0">
          {/* Document Title */}
          <button
            onClick={() => onOpen(result.id)}
            className="text-left cursor-pointer group/title"
          >
            <h3 className="text-xl font-bold text-navy group-hover/title:text-accent transition-colors leading-snug">
              {result.title}
            </h3>
          </button>

          {/* Relevance Percentage + Document Path */}
          <div className="flex flex-wrap items-center gap-2.5 my-1.5">
            <span
              className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-mono font-bold ${
                result.relevance >= 75
                  ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                  : result.relevance >= 40
                  ? "bg-blue-50 text-accent border border-blue-200"
                  : "bg-slate-100 text-slate-600 border border-slate-200"
              }`}
            >
              {result.relevance}% relevance
            </span>
            <span className="text-xs font-mono text-path-green truncate">{result.path}</span>
          </div>

          {/* Relevant text snippet with highlighted matching terms */}
          <p className="text-sm text-slate-700 leading-relaxed mb-3">
            "{highlightTerms(result.snippet, query)}"
          </p>

          {/* Metadata badges & highlighted matched terms */}
          <div className="flex flex-wrap items-center gap-x-2.5 gap-y-1.5 mb-3 text-xs text-slate-500">
            <AlgoBadge algorithm={result.algorithm} />
            <span>·</span>
            <span>
              <span className="font-mono text-slate-700 font-semibold">{result.matches}</span> occurrences
            </span>
            <span>·</span>
            <span className="font-mono text-slate-500">{result.size}</span>
            <span>·</span>
            <span className="bg-slate-100 text-slate-600 px-2 py-0.5 rounded font-medium">
              {result.docType}
            </span>
            {result.category && result.category !== "general" && (
              <span className="bg-slate-100 text-slate-700 px-2 py-0.5 rounded font-medium">
                {result.category}
              </span>
            )}
            {result.language && result.language !== "English" && (
              <span className="bg-purple-50 text-purple-700 border border-purple-200 px-2 py-0.5 rounded font-medium">
                {result.language}
              </span>
            )}
            {matchedTerms.length > 0 && (
              <span className="bg-blue-50 text-accent border border-blue-200 px-2 py-0.5 rounded font-mono">
                Matched: {matchedTerms.join(", ")}
              </span>
            )}
            {isExactPhraseQuery && (
              <span className="text-xs text-blue-700 bg-blue-50 border border-blue-200 px-2 py-0.5 rounded font-medium inline-flex items-center gap-1">
                <span>🎯</span> Exact phrase match
              </span>
            )}
            {result.date && <span className="text-slate-400">{formatDate(result.date)}</span>}
          </div>


          {/* Action buttons: [Why this result?] [Open document] */}
          <div className="flex flex-wrap items-center gap-2 pt-1">
            <button
              type="button"
              onClick={onToggleBreakdown}
              className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors cursor-pointer ${
                isBreakdownOpen
                  ? "bg-blue-50 border-blue-300 text-accent font-semibold"
                  : "bg-white border-slate-200 text-slate-700 hover:bg-slate-50 hover:border-slate-300"
              }`}
            >
              <span>Why this result?</span>
              <span
                className={`text-[9px] transform transition-transform duration-150 ${
                  isBreakdownOpen ? "rotate-180 text-accent" : "text-slate-400"
                }`}
              >
                ▼
              </span>
            </button>

            <button
              type="button"
              onClick={() => onOpen(result.id)}
              className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg text-xs font-medium bg-navy text-white hover:bg-navy-mid transition-colors cursor-pointer shadow-2xs"
            >
              <span>Open document</span>
              <ChevronRightIcon className="w-3.5 h-3.5 text-blue-200" />
            </button>
          </div>

          {/* Why this result? — Transparent explainability */}
          {isBreakdownOpen && (
            <div className="mt-3.5 p-4 bg-slate-50 border border-slate-200 rounded-xl text-xs space-y-3.5 animate-in fade-in duration-150">
              <div className="flex items-center justify-between pb-2.5 border-b border-slate-200">
                <span className="font-bold text-slate-900 uppercase tracking-wider text-[11px]">
                  Why this result?
                </span>
                <span className="font-mono font-bold text-accent text-sm">
                  {result.relevance}% Final Relevance
                </span>
              </div>

              {result.relevance === 0 ? (
                <div className="space-y-1.5 text-slate-600 font-mono text-xs">
                  <div>
                    <span className="font-semibold text-slate-800">Matched terms:</span> None
                  </div>
                  <div>
                    <span className="font-semibold text-slate-800">Cosine similarity:</span> 0
                  </div>
                  <div>
                    <span className="font-semibold text-slate-800">Relevance:</span> 0%
                  </div>
                </div>
              ) : (
                <div className="space-y-3 text-slate-700">
                  {/* Query terms & Matched terms */}
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                    <div className="bg-white p-3 rounded-lg border border-slate-200/80">
                      <span className="font-semibold text-slate-900 block mb-1.5 text-[11px] uppercase tracking-wide">
                        Query terms:
                      </span>
                      <ul className="space-y-1">
                        {(sb?.queryTerms && sb.queryTerms.length > 0
                          ? sb.queryTerms
                          : query
                              .replace(/["']/g, "")
                              .trim()
                              .toLowerCase()
                              .split(/\s+/)
                              .filter((t) => t.length > 0)
                        ).map((t) => (
                          <li key={t} className="font-mono text-slate-700 flex items-center gap-1.5">
                            <span className="text-slate-400">•</span>
                            <span>{t}</span>
                          </li>
                        ))}
                      </ul>
                    </div>

                    <div className="bg-white p-3 rounded-lg border border-slate-200/80">
                      <span className="font-semibold text-slate-900 block mb-1.5 text-[11px] uppercase tracking-wide">
                        Matched terms:
                      </span>
                      <ul className="space-y-1">
                        {(sb?.queryTerms && sb.queryTerms.length > 0
                          ? sb.queryTerms
                          : query
                              .replace(/["']/g, "")
                              .trim()
                              .toLowerCase()
                              .split(/\s+/)
                              .filter((t) => t.length > 0)
                        ).map((t) => {
                          const isMatched =
                            sb?.matchedTerms && sb.matchedTerms.length > 0
                              ? sb.matchedTerms.includes(t)
                              : matchedTerms.includes(t)
                          return (
                            <li key={t} className="font-mono flex items-center justify-between text-xs">
                              <span className={isMatched ? "text-slate-800 font-semibold" : "text-slate-400"}>
                                • {t}
                              </span>
                              {isMatched ? (
                                <span className="text-emerald-600 font-bold text-xs">✓ matched</span>
                              ) : (
                                <span className="text-slate-300 text-xs">✗ not in doc</span>
                              )}
                            </li>
                          )
                        })}
                      </ul>
                    </div>
                  </div>

                  {/* Term frequency & TF-IDF contribution */}
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                    <div className="bg-white p-3 rounded-lg border border-slate-200/80">
                      <span className="font-semibold text-slate-900 block mb-1.5 text-[11px] uppercase tracking-wide">
                        Term frequency:
                      </span>
                      {sb?.termFrequencies && Object.keys(sb.termFrequencies).length > 0 ? (
                        <div className="space-y-1 font-mono text-xs text-slate-700">
                          {Object.entries(sb.termFrequencies).map(([term, count]) => (
                            <div key={term} className="flex justify-between">
                              <span className="text-slate-600">{term}:</span>
                              <span className="font-semibold text-slate-900">{count}</span>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <span className="text-slate-500 font-mono text-xs">
                          {result.matches} occurrences in text
                        </span>
                      )}
                    </div>

                    <div className="bg-white p-3 rounded-lg border border-slate-200/80">
                      <span className="font-semibold text-slate-900 block mb-1.5 text-[11px] uppercase tracking-wide">
                        TF-IDF contribution:
                      </span>
                      {sb?.tfidfContributions && Object.keys(sb.tfidfContributions).length > 0 ? (
                        <div className="space-y-1 font-mono text-xs text-slate-700">
                          {Object.entries(sb.tfidfContributions).map(([term, weight]) => (
                            <div key={term} className="flex justify-between">
                              <span className="text-slate-600">{term}:</span>
                              <span className="font-semibold text-accent">{weight.toFixed(3)}</span>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <span className="text-slate-500 font-mono text-xs">
                          {sb?.tfidf !== undefined ? sb.tfidf.toFixed(4) : "Calculated"}
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Cosine similarity and Final relevance summary */}
                  <div className="bg-white p-3 rounded-lg border border-slate-200/80 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2">
                    <div>
                      <span className="text-slate-500 text-xs font-medium">Cosine similarity:</span>{" "}
                      <span className="font-mono font-bold text-navy text-sm">
                        {sb?.cosineSimilarity !== undefined
                          ? sb.cosineSimilarity.toFixed(4)
                          : sb?.tfidf !== undefined
                          ? sb.tfidf.toFixed(4)
                          : (result.relevance / 100).toFixed(2)}
                      </span>
                    </div>
                    <div>
                      <span className="text-slate-500 text-xs font-medium">Final relevance:</span>{" "}
                      <span className="font-mono font-bold text-accent text-sm">
                        {result.relevance}%
                      </span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
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
        <li className="flex items-center gap-2"><span className="text-slate-300">·</span> Check your spelling or look for "Did you mean"</li>
        <li className="flex items-center gap-2"><span className="text-slate-300">·</span> Try fewer keywords</li>
        <li className="flex items-center gap-2"><span className="text-slate-300">·</span> Switch Search Mode to Fuzzy or Multi-Keyword</li>
        <li className="flex items-center gap-2"><span className="text-slate-300">·</span> Clear Topic Category filters</li>
      </ul>
      <button
        onClick={onClear}
        className="px-5 py-2.5 bg-accent text-white rounded-lg text-sm font-medium hover:bg-accent-dark transition-colors cursor-pointer"
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

const SEARCH_MODES = [
  { id: "smart", name: "Smart Search", badge: "VSM / TF-IDF" },
  { id: "exact-pattern", name: "Exact Pattern", badge: "KMP" },
  { id: "multi-pattern", name: "Multi-Pattern", badge: "Aho-Corasick" },
  { id: "fuzzy", name: "Fuzzy Search", badge: "Levenshtein" },
]

function SearchModePicker({
  value,
  onChange,
}: {
  value: string
  onChange: (v: string) => void
}) {
  return (
    <div className="mb-7">
      <h3 className="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-3">Search Mode</h3>
      <div className="space-y-1.5">
        {SEARCH_MODES.map((mode) => (
          <button
            key={mode.id}
            type="button"
            onClick={() => onChange(mode.id)}
            className={`w-full text-left px-3 py-2 text-xs rounded-lg flex items-center justify-between transition-colors cursor-pointer ${
              value === mode.id
                ? "bg-accent-pale border border-blue-200 text-accent font-semibold"
                : "text-slate-600 hover:bg-slate-50 border border-transparent"
            }`}
          >
            <span className="font-medium truncate">{mode.name}</span>
            <span
              className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
                value === mode.id ? "bg-accent text-white" : "bg-slate-100 text-slate-500"
              }`}
            >
              {mode.badge}
            </span>
          </button>
        ))}
      </div>
    </div>
  )
}

function AlgorithmDetailsModal({
  isOpen,
  onClose,
  algorithm,
  query,
  lpsArray,
  searchTimeMs,
  documentsScanned,
}: {
  isOpen: boolean
  onClose: () => void
  algorithm: string
  query: string
  lpsArray?: number[]
  searchTimeMs: number
  documentsScanned: number
}) {
  if (!isOpen) return null

  const cleanPattern = query.replace(/^"|"$/g, "").trim()
  const chars = cleanPattern.slice(0, 16).split("")
  const lps = lpsArray && lpsArray.length > 0 ? lpsArray.slice(0, chars.length) : chars.map(() => 0)

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl max-w-lg w-full p-6 animate-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between pb-3 border-b border-slate-100 mb-4">
          <div className="flex items-center gap-2">
            <span className="p-1.5 bg-blue-100 text-accent rounded-lg text-sm">⚙</span>
            <h3 className="font-bold text-slate-900 text-base">Algorithm Inspection</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100 transition-colors cursor-pointer"
          >
            <XIcon className="w-5 h-5" />
          </button>
        </div>

        {/* Algorithm Badge & Metrics */}
        <div className="space-y-4 text-xs text-slate-600">
          <div className="flex items-center justify-between p-3 bg-slate-50 rounded-xl border border-slate-100">
            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">Active Algorithm</span>
              <span className="text-sm font-bold text-navy font-mono">{algorithm}</span>
            </div>
            <div className="text-right">
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">Search Latency</span>
              <span className="text-sm font-bold text-accent font-mono">{searchTimeMs} ms</span>
            </div>
            <div className="text-right">
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">Scanned</span>
              <span className="text-sm font-bold text-slate-800 font-mono">{documentsScanned} docs</span>
            </div>
          </div>

          {/* LPS Array Table for KMP */}
          <div className="p-4 bg-slate-900 text-white rounded-xl font-mono">
            <div className="flex items-center justify-between mb-2">
              <span className="text-blue-300 text-[11px] uppercase font-semibold tracking-wider">
                KMP LPS (π) Failure Table
              </span>
              <span className="text-[10px] text-slate-400">O(m) preprocessing</span>
            </div>
            <p className="text-[11px] text-slate-400 mb-3 font-sans">
              Stores length of the longest proper prefix that is also a suffix. When mismatches occur, KMP shifts the pattern pointer without re-reading text.
            </p>

            <div className="overflow-x-auto pb-1">
              <div className="inline-flex flex-col border border-slate-700 rounded-lg overflow-hidden">
                <div className="flex bg-slate-800 border-b border-slate-700">
                  <div className="w-12 px-2 py-1 text-[10px] text-slate-400 border-r border-slate-700">Char</div>
                  {chars.map((c, i) => (
                    <div key={i} className="w-8 py-1 text-center font-bold text-accent text-xs">
                      {c}
                    </div>
                  ))}
                </div>
                <div className="flex bg-slate-900">
                  <div className="w-12 px-2 py-1 text-[10px] text-slate-400 border-r border-slate-700">LPS[i]</div>
                  {lps.map((val, i) => (
                    <div key={i} className="w-8 py-1 text-center text-xs font-semibold text-emerald-400">
                      {val}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Complexity analysis card */}
          <div className="p-3.5 bg-blue-50/50 border border-blue-100 rounded-xl space-y-1.5">
            <div className="flex justify-between text-slate-700 font-medium">
              <span>Time Complexity:</span>
              <span className="font-mono text-navy font-bold">O(n + m) linear</span>
            </div>
            <div className="flex justify-between text-slate-700 font-medium">
              <span>Auxiliary Space:</span>
              <span className="font-mono text-navy font-bold">O(m) LPS array</span>
            </div>
            <div className="flex justify-between text-slate-700 font-medium">
              <span>Index Engine:</span>
              <span className="font-mono text-navy font-bold">Positional Inverted Index</span>
            </div>
          </div>
        </div>

        <div className="mt-5 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-navy hover:bg-navy-light text-white rounded-xl text-xs font-medium transition-colors cursor-pointer"
          >
            Close Details
          </button>
        </div>
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
        className="px-3 py-2 text-sm text-slate-600 hover:bg-white hover:text-navy rounded-lg disabled:opacity-40 disabled:cursor-not-allowed transition-colors border border-transparent hover:border-slate-200 cursor-pointer"
      >
        Prev
      </button>
      {pages.map((p) => (
        <button
          key={p}
          onClick={() => onChange(p)}
          className={`w-9 h-9 text-sm rounded-lg transition-colors font-medium cursor-pointer ${
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
            className="w-9 h-9 text-sm rounded-lg text-slate-600 hover:bg-white hover:text-navy hover:border-slate-200 border border-transparent transition-colors font-medium cursor-pointer"
          >
            {total}
          </button>
        </>
      )}
      <button
        onClick={() => onChange(Math.min(total, current + 1))}
        disabled={current === total}
        className="px-3 py-2 text-sm text-slate-600 hover:bg-white hover:text-navy rounded-lg disabled:opacity-40 disabled:cursor-not-allowed transition-colors border border-transparent hover:border-slate-200 cursor-pointer"
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
  onAdvancedSearch,
  onOpenDocument,
  onNavigate,
  onFilterChange,
  onPageChange,
}: Props) {
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false)
  const [searchResponse, setSearchResponse] = useState<SearchApiResponse | null>(null)
  const [fetching, setFetching] = useState<boolean>(true)
  const [expandedBreakdown, setExpandedBreakdown] = useState<string | null>(null)
  const [showAlgoModal, setShowAlgoModal] = useState<boolean>(false)
  const [showAdvancedModal, setShowAdvancedModal] = useState<boolean>(false)
  const [metadata, setMetadata] = useState<FilterMetadata>({
    categories: ["computer-science", "data-structures", "algorithms"],
    languages: ["English", "Abkhazian", "Acehnese", "Adyghe"],
    docTypes: ["article", "documentation", "guide"],
  })

  useEffect(() => {
    fetchFilterMetadata().then((data) => {
      if (data && (data.categories.length > 0 || data.languages.length > 0)) {
        setMetadata(data)
      }
    })
  }, [])

  useEffect(() => {
    let cancelled = false
    setFetching(true)
    executeSearch(query, filters, currentPage, RESULTS_PER_PAGE).then((data) => {
      if (!cancelled) {
        setSearchResponse(data)
        setFetching(false)
      }
    })
    return () => {
      cancelled = true
    }
  }, [query, filters, currentPage])

  const results = searchResponse?.results || []
  const totalResults = searchResponse?.totalResults ?? 0
  const searchTimeMs = searchResponse?.searchTimeMs ?? 18
  const activeAlgoName = searchResponse?.algorithmUsed ?? (filters.algorithm === "auto" ? "Auto" : filters.algorithm)
  const indexType = searchResponse?.indexType ?? "Positional Inverted Index"
  const totalPages = searchResponse?.totalPages ?? Math.max(1, Math.ceil(totalResults / RESULTS_PER_PAGE))
  const didYouMean = searchResponse?.didYouMean

  // Filter out 0% relevance results for lexical search
  const displayedResults = results.filter((r) => {
    if (r.relevance <= 0) return false
    if (!filters.relevanceTier || filters.relevanceTier === "all") return true
    if (filters.relevanceTier === "high") return r.relevance >= 75
    if (filters.relevanceTier === "medium") return r.relevance >= 40 && r.relevance < 75
    if (filters.relevanceTier === "low") return r.relevance < 40
    return true
  })

  const formatLabel = (s: string) =>
    s
      .split("-")
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
      .join(" ")

  const categoryOptions = [
    { label: "All Categories", value: "all" },
    ...metadata.categories.map((cat) => ({
      label: formatLabel(cat),
      value: cat,
    })),
  ]

  const languageOptions = [
    { label: "All Languages", value: "all" },
    ...metadata.languages.map((lang) => ({
      label: lang,
      value: lang,
    })),
  ]

  const docTypeOptions = [
    { label: "All Document Types", value: "all" },
    ...metadata.docTypes.map((dt) => ({
      label: formatLabel(dt),
      value: dt,
    })),
  ]

  const relevanceTierOptions = [
    { label: "All Scores", value: "all" },
    { label: "High Match (≥ 75%)", value: "high" },
    { label: "Medium Match (40% - 74%)", value: "medium" },
    { label: "Low Match (< 40%)", value: "low" },
  ]

  const sortOptions = [
    { label: "Best Match (Relevance)", value: "relevance" },
    { label: "Alphabetical (A–Z)", value: "alphabetical" },
  ]

  const hasActiveFilters =
    (filters.category && filters.category !== "all") ||
    (filters.language && filters.language !== "all") ||
    (filters.docType && filters.docType !== "all") ||
    (filters.relevanceTier && filters.relevanceTier !== "all") ||
    Boolean(filters.exactPhrase) ||
    Boolean(filters.includeTerms) ||
    Boolean(filters.excludeTerms)

  const clearAllFilters = () => {
    onFilterChange("category", "all")
    onFilterChange("language", "all")
    onFilterChange("docType", "all")
    onFilterChange("relevanceTier", "all")
    onFilterChange("exactPhrase", "")
    onFilterChange("includeTerms", "")
    onFilterChange("excludeTerms", "")
  }

  const FilterContent = () => (
    <>
      <SearchModePicker value={filters.algorithm} onChange={(v) => onFilterChange("algorithm", v)} />
      <FilterSection title="Category" options={categoryOptions} value={filters.category || "all"} onChange={(v) => onFilterChange("category", v)} />
      <FilterSection title="Language" options={languageOptions} value={filters.language || "all"} onChange={(v) => onFilterChange("language", v)} />
      {metadata.docTypes.length > 1 && (
        <FilterSection title="Document Type" options={docTypeOptions} value={filters.docType || "all"} onChange={(v) => onFilterChange("docType", v)} />
      )}
      <FilterSection title="Relevance Tier" options={relevanceTierOptions} value={filters.relevanceTier || "all"} onChange={(v) => onFilterChange("relevanceTier", v)} />
      <FilterSection title="Sort Order" options={sortOptions} value={filters.sortBy} onChange={(v) => onFilterChange("sortBy", v)} />

      <div className="pt-2 border-t border-slate-100">
        <button
          type="button"
          onClick={() => setShowAdvancedModal(true)}
          className="w-full py-2 px-3 text-xs font-medium text-accent bg-blue-50/70 hover:bg-blue-100/70 border border-blue-200 rounded-lg transition-colors flex items-center justify-center gap-1.5 cursor-pointer"
        >
          <span>⚙</span>
          <span>Advanced Search</span>
        </button>
      </div>
    </>
  )

  const showLoading = fetching

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
              onClick={() => setShowAdvancedModal(true)}
              className="px-3 py-1.5 text-xs text-accent hover:bg-blue-50 border border-blue-200 rounded-lg transition-colors font-medium cursor-pointer flex items-center gap-1.5"
            >
              <span>⚙</span>
              <span>Advanced</span>
            </button>
            <button
              onClick={() => onNavigate("home")}
              className="px-3 py-1.5 text-sm text-slate-600 hover:text-navy hover:bg-slate-50 rounded-lg transition-colors font-medium cursor-pointer"
            >
              Search
            </button>
            <button
              onClick={() => onNavigate("technical")}
              className="px-3 py-1.5 text-sm text-slate-600 hover:text-navy hover:bg-slate-50 rounded-lg transition-colors font-medium cursor-pointer"
            >
              Technical
            </button>
          </nav>
        </div>
      </header>

      <main className="flex-1 max-w-screen-xl mx-auto w-full px-4 sm:px-6 py-6">
        {/* Backend error banner if present */}
        {searchResponse?.error && (
          <div className="mb-5 px-4 py-3 bg-red-50 border border-red-200 rounded-xl text-xs text-red-700 flex items-center gap-2">
            <span className="font-bold text-sm">⚠</span>
            <span>{searchResponse.error}</span>
          </div>
        )}

        {/* Search query, count, and execution time header */}
        <div className="mb-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
          <div>
            {showLoading ? (
              <div className="flex items-center gap-2 text-sm text-slate-500">
                <div className="w-3.5 h-3.5 border-2 border-accent border-t-transparent rounded-full animate-spin" />
                Searching the knowledge base&hellip;
              </div>
            ) : (
              <>
                <h1 className="text-xl font-bold text-navy mb-0.5">
                  Results for <span className="text-accent font-mono">"{query}"</span>
                </h1>
                <div className="flex flex-wrap items-center gap-2 text-xs text-slate-500">
                  <span className="font-semibold text-slate-800">{totalResults.toLocaleString()} results</span>
                  <span>found in</span>
                  <span className="font-mono font-semibold text-slate-800">{searchTimeMs} ms</span>
                  <span className="text-slate-300">|</span>
                  <span className="font-mono">
                    Algorithm: <span className="text-slate-700 font-semibold">{activeAlgoName}</span>
                    {" · "}
                    Index: <span className="text-slate-700 font-semibold">{indexType}</span>
                  </span>
                  <button
                    type="button"
                    onClick={() => setShowAlgoModal(true)}
                    className="text-accent hover:underline cursor-pointer font-sans font-medium flex items-center gap-1"
                  >
                    <span>⚙ View Algorithm Details & LPS</span>
                  </button>
                </div>
              </>
            )}
          </div>
          <div className="flex items-center gap-2">
            <button
              className="flex md:hidden items-center gap-2 px-3.5 py-2 border border-slate-300 bg-white rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors self-start cursor-pointer"
              onClick={() => setMobileFiltersOpen(true)}
            >
              <FilterIcon className="w-4 h-4" />
              Filters
            </button>
            <button
              className="flex sm:hidden items-center gap-1.5 px-3 py-2 border border-blue-200 bg-blue-50 text-accent rounded-lg text-xs font-medium cursor-pointer"
              onClick={() => setShowAdvancedModal(true)}
            >
              <span>⚙</span>
              <span>Advanced</span>
            </button>
          </div>
        </div>

        {/* Active Filter Chips */}
        {hasActiveFilters && (
          <div className="mb-4 flex flex-wrap items-center gap-2 pt-1 pb-2 border-b border-slate-200/80">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Active Filters:</span>
            {filters.category && filters.category !== "all" && (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-blue-50 text-accent border border-blue-200">
                <span>Category: {formatLabel(filters.category)}</span>
                <button
                  type="button"
                  onClick={() => onFilterChange("category", "all")}
                  className="hover:text-red-600 font-bold ml-0.5 cursor-pointer"
                >
                  ×
                </button>
              </span>
            )}
            {filters.language && filters.language !== "all" && (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-purple-50 text-purple-700 border border-purple-200">
                <span>Language: {filters.language}</span>
                <button
                  type="button"
                  onClick={() => onFilterChange("language", "all")}
                  className="hover:text-red-600 font-bold ml-0.5 cursor-pointer"
                >
                  ×
                </button>
              </span>
            )}
            {filters.docType && filters.docType !== "all" && (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-slate-100 text-slate-700 border border-slate-200">
                <span>Type: {formatLabel(filters.docType)}</span>
                <button
                  type="button"
                  onClick={() => onFilterChange("docType", "all")}
                  className="hover:text-red-600 font-bold ml-0.5 cursor-pointer"
                >
                  ×
                </button>
              </span>
            )}
            {filters.exactPhrase && (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-amber-50 text-amber-800 border border-amber-200">
                <span>Exact: "{filters.exactPhrase}"</span>
                <button
                  type="button"
                  onClick={() => onFilterChange("exactPhrase", "")}
                  className="hover:text-red-600 font-bold ml-0.5 cursor-pointer"
                >
                  ×
                </button>
              </span>
            )}
            {filters.includeTerms && (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-50 text-emerald-800 border border-emerald-200">
                <span>Must include: +{filters.includeTerms}</span>
                <button
                  type="button"
                  onClick={() => onFilterChange("includeTerms", "")}
                  className="hover:text-red-600 font-bold ml-0.5 cursor-pointer"
                >
                  ×
                </button>
              </span>
            )}
            {filters.excludeTerms && (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-rose-50 text-rose-800 border border-rose-200">
                <span>Exclude: -{filters.excludeTerms}</span>
                <button
                  type="button"
                  onClick={() => onFilterChange("excludeTerms", "")}
                  className="hover:text-red-600 font-bold ml-0.5 cursor-pointer"
                >
                  ×
                </button>
              </span>
            )}
            {filters.relevanceTier && filters.relevanceTier !== "all" && (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-slate-100 text-slate-700 border border-slate-200">
                <span>Tier: {filters.relevanceTier}</span>
                <button
                  type="button"
                  onClick={() => onFilterChange("relevanceTier", "all")}
                  className="hover:text-red-600 font-bold ml-0.5 cursor-pointer"
                >
                  ×
                </button>
              </span>
            )}
            <button
              type="button"
              onClick={clearAllFilters}
              className="text-xs text-slate-500 hover:text-navy underline ml-1 cursor-pointer font-medium"
            >
              Clear all filters
            </button>
          </div>
        )}

        {/* Did you mean banner */}
        {!showLoading && didYouMean && didYouMean.toLowerCase() !== query.toLowerCase().replace(/"/g, "") && (
          <div className="mb-5 px-4 py-3 bg-blue-50/80 border border-blue-200/80 rounded-xl text-sm flex items-center justify-between gap-3 animate-in fade-in duration-150">
            <div className="flex items-center gap-2">
              <span className="text-slate-600">Did you mean:</span>
              <button
                type="button"
                onClick={() => onSearch(didYouMean)}
                className="font-semibold text-accent hover:underline hover:text-accent-dark cursor-pointer text-left"
              >
                {didYouMean}
              </button>
            </div>
            <span className="text-xs text-blue-600 bg-blue-100/60 px-2 py-0.5 rounded font-mono hidden sm:inline-block">
              Levenshtein Spellcheck
            </span>
          </div>
        )}

        <div className="flex gap-8">
          {/* Desktop sidebar */}
          <aside className="hidden md:block w-56 flex-shrink-0">
            <div className="bg-white border border-slate-200 rounded-xl p-5 sticky top-24 max-h-[calc(100vh-7rem)] overflow-y-auto">
              <div className="flex items-center justify-between mb-5">
                <h2 className="font-semibold text-sm text-slate-900">Filters</h2>
                <FilterIcon className="w-4 h-4 text-slate-400" />
              </div>
              <FilterContent />
            </div>
          </aside>

          {/* Main results */}
          <div className="flex-1 min-w-0">
            {/* Expandable Technical Search & Algorithm Details Panel */}
            {!showLoading && searchResponse?.algorithmDetails && (
              <AlgorithmDetailsPanel
                details={searchResponse.algorithmDetails}
                lpsArray={searchResponse.lpsArray}
              />
            )}

            {showLoading ? (
              <div className="bg-white border border-slate-200 rounded-xl px-6">
                {Array.from({ length: 5 }).map((_, i) => (
                  <ResultSkeleton key={i} />
                ))}
              </div>
            ) : displayedResults.length === 0 ? (
              <div className="bg-white border border-slate-200 rounded-xl">
                <EmptyState query={query} onClear={() => onSearch("")} />
              </div>
            ) : (
              <>
                <div className="bg-white border border-slate-200 rounded-xl px-6">
                  {displayedResults.map((result) => (
                    <ResultItem
                      key={result.id}
                      result={result}
                      query={query}
                      onOpen={onOpenDocument}
                      isBreakdownOpen={expandedBreakdown === result.id}
                      onToggleBreakdown={() =>
                        setExpandedBreakdown(expandedBreakdown === result.id ? null : result.id)
                      }
                    />
                  ))}
                </div>
                <div className="bg-white border border-slate-200 rounded-xl mt-2">
                  <Pagination
                    current={currentPage}
                    total={totalPages}
                    onChange={onPageChange}
                  />
                </div>
              </>
            )}
          </div>
        </div>
      </main>

      {/* Advanced Search Modal */}
      <AdvancedSearchModal
        isOpen={showAdvancedModal}
        onClose={() => setShowAdvancedModal(false)}
        currentQuery={query}
        currentFilters={filters}
        onApplyAdvancedSearch={(advQuery, advFilters) => {
          if (onAdvancedSearch) {
            onAdvancedSearch(advQuery, advFilters)
          } else {
            onSearch(advQuery)
          }
        }}
      />

      {/* Algorithm inspection modal */}
      <AlgorithmDetailsModal
        isOpen={showAlgoModal}
        onClose={() => setShowAlgoModal(false)}
        algorithm={activeAlgoName}
        query={query}
        lpsArray={searchResponse?.lpsArray}
        searchTimeMs={searchTimeMs}
        documentsScanned={searchResponse?.documentsScanned ?? 69}
      />

      {/* Mobile filters sheet */}
      {mobileFiltersOpen && (
        <div className="fixed inset-0 z-50 md:hidden">
          <div
            className="absolute inset-0 bg-black/40 backdrop-blur-xs"
            onClick={() => setMobileFiltersOpen(false)}
          />
          <div className="absolute bottom-0 left-0 right-0 bg-white rounded-t-2xl p-6 max-h-[85vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-5">
              <h2 className="font-semibold text-slate-900">Filters</h2>
              <button
                onClick={() => setMobileFiltersOpen(false)}
                className="p-1.5 text-slate-500 hover:text-slate-700 rounded-lg hover:bg-slate-100 transition-colors cursor-pointer"
              >
                <XIcon className="w-5 h-5" />
              </button>
            </div>
            <FilterContent />
            <button
              onClick={() => setMobileFiltersOpen(false)}
              className="w-full mt-2 py-3 bg-navy text-white rounded-xl font-medium text-sm cursor-pointer"
            >
              Apply Filters
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
