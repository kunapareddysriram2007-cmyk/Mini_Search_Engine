import { useState, useEffect } from "react"
import { XIcon, SearchIcon, FilterIcon } from "../icons"
import type { Filters, AdvancedSearchState } from "../types"
import { fetchFilterMetadata, type FilterMetadata } from "../api"

interface AdvancedSearchModalProps {
  isOpen: boolean
  onClose: () => void
  currentQuery: string
  currentFilters: Filters
  onApplyAdvancedSearch: (query: string, filters: Partial<Filters>) => void
}

const SEARCH_MODES = [
  { id: "smart", label: "Smart Search", desc: "Vector Space Model + TF-IDF Cosine Similarity ranking", badge: "VSM / TF-IDF" },
  { id: "exact-pattern", label: "Exact Pattern", desc: "Linear-time exact pattern matching with LPS failure function", badge: "KMP" },
  { id: "multi-pattern", label: "Multi-Pattern", desc: "Simultaneous dictionary matching via finite-state trie", badge: "Aho-Corasick" },
  { id: "fuzzy", label: "Fuzzy Search", desc: "Edit-distance matching tolerant to typos and spelling errors", badge: "Levenshtein" },
]

export default function AdvancedSearchModal({
  isOpen,
  onClose,
  currentQuery,
  currentFilters,
  onApplyAdvancedSearch,
}: AdvancedSearchModalProps) {
  const [query, setQuery] = useState(currentQuery)
  const [exactPhrase, setExactPhrase] = useState(currentFilters.exactPhrase || "")
  const [includeTerms, setIncludeTerms] = useState(currentFilters.includeTerms || "")
  const [excludeTerms, setExcludeTerms] = useState(currentFilters.excludeTerms || "")
  const [category, setCategory] = useState(currentFilters.category || "all")
  const [language, setLanguage] = useState(currentFilters.language || "all")
  const [algorithm, setAlgorithm] = useState(currentFilters.algorithm || "smart")
  const [metadata, setMetadata] = useState<FilterMetadata>({
    categories: ["computer-science", "data-structures", "algorithms", "abkhazian", "acehnese", "adyghe"],
    languages: ["English", "Abkhazian", "Acehnese", "Adyghe"],
    docTypes: ["Wikipedia", "Article", "Reference"],
  })

  useEffect(() => {
    if (isOpen) {
      setQuery(currentQuery)
      setExactPhrase(currentFilters.exactPhrase || "")
      setIncludeTerms(currentFilters.includeTerms || "")
      setExcludeTerms(currentFilters.excludeTerms || "")
      setCategory(currentFilters.category || "all")
      setLanguage(currentFilters.language || "all")
      setAlgorithm(currentFilters.algorithm || "smart")

      fetchFilterMetadata().then((data) => {
        if (data) setMetadata(data)
      })
    }
  }, [isOpen, currentQuery, currentFilters])

  if (!isOpen) return null

  const handleReset = () => {
    setQuery("")
    setExactPhrase("")
    setIncludeTerms("")
    setExcludeTerms("")
    setCategory("all")
    setLanguage("all")
    setAlgorithm("smart")
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    let effectiveQuery = query.trim()
    if (!effectiveQuery && exactPhrase.trim()) {
      effectiveQuery = `"${exactPhrase.trim()}"`
    } else if (!effectiveQuery && includeTerms.trim()) {
      effectiveQuery = includeTerms.trim()
    } else if (!effectiveQuery) {
      effectiveQuery = "algorithm"
    }

    let matchType = "exact"
    if (algorithm === "multi-pattern" || algorithm === "aho-corasick") matchType = "partial"
    else if (algorithm === "fuzzy" || algorithm === "edit-distance") matchType = "fuzzy"

    onApplyAdvancedSearch(effectiveQuery, {
      exactPhrase: exactPhrase.trim() || undefined,
      includeTerms: includeTerms.trim() || undefined,
      excludeTerms: excludeTerms.trim() || undefined,
      category,
      language,
      algorithm,
      matchType,
    })
    onClose()
  }

  const formatCategoryLabel = (cat: string) => {
    switch (cat.toLowerCase()) {
      case "computer-science": return "Computer Science"
      case "data-structures": return "Data Structures"
      case "algorithms": return "Algorithms"
      case "abkhazian": return "Abkhazian Corpus"
      case "acehnese": return "Acehnese Corpus"
      case "adyghe": return "Adyghe Corpus"
      default: return cat
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl max-w-2xl w-full max-h-[90vh] flex flex-col overflow-hidden animate-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 flex-shrink-0 bg-slate-50/50">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-blue-50 text-accent rounded-xl border border-blue-100">
              <FilterIcon className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-base font-bold text-navy">Advanced Search</h2>
              <p className="text-xs text-slate-500">Fine-tune queries with metadata filters and phrase operators</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-100 transition-colors cursor-pointer"
          >
            <XIcon className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Form Body */}
        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-6 space-y-5">
          {/* Main search term */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
              General Search Query
            </label>
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="e.g., operating system memory"
              className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:border-accent focus:ring-2 focus:ring-accent/20 outline-none text-slate-900 transition-all font-sans"
            />
          </div>

          {/* Exact phrase & Included terms */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                Exact Phrase
              </label>
              <input
                type="text"
                value={exactPhrase}
                onChange={(e) => setExactPhrase(e.target.value)}
                placeholder='e.g., virtual memory'
                className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:border-accent focus:ring-2 focus:ring-accent/20 outline-none text-slate-900 transition-all font-sans"
              />
              <span className="text-[11px] text-slate-400 mt-1 block">Matches exact contiguous string</span>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                Include Terms (+)
              </label>
              <input
                type="text"
                value={includeTerms}
                onChange={(e) => setIncludeTerms(e.target.value)}
                placeholder="e.g., kernel, scheduler"
                className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:border-accent focus:ring-2 focus:ring-accent/20 outline-none text-slate-900 transition-all font-sans"
              />
              <span className="text-[11px] text-slate-400 mt-1 block">Documents must contain all of these</span>
            </div>
          </div>

          {/* Excluded terms */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
              Exclude Terms (-)
            </label>
            <input
              type="text"
              value={excludeTerms}
              onChange={(e) => setExcludeTerms(e.target.value)}
              placeholder="e.g., windows, deadlock"
              className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:border-accent focus:ring-2 focus:ring-accent/20 outline-none text-slate-900 transition-all font-sans"
            />
            <span className="text-[11px] text-slate-400 mt-1 block">Documents containing any of these will be omitted</span>
          </div>

          {/* Category & Language Dropdowns */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                Topic Category
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:border-accent focus:ring-2 focus:ring-accent/20 outline-none text-slate-900 cursor-pointer"
              >
                <option value="all">All Categories</option>
                {metadata.categories.map((cat) => (
                  <option key={cat} value={cat}>
                    {formatCategoryLabel(cat)}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                Language
              </label>
              <select
                value={language}
                onChange={(e) => setLanguage(e.target.value)}
                className="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-300 rounded-xl focus:border-accent focus:ring-2 focus:ring-accent/20 outline-none text-slate-900 cursor-pointer"
              >
                <option value="all">All Languages</option>
                {metadata.languages.map((lang) => (
                  <option key={lang} value={lang}>
                    {lang}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Search Mode selection */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
              Search Mode Algorithm
            </label>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {SEARCH_MODES.map((mode) => (
                <button
                  key={mode.id}
                  type="button"
                  onClick={() => setAlgorithm(mode.id)}
                  className={`p-3 rounded-xl border text-left transition-all cursor-pointer flex flex-col justify-between ${
                    algorithm === mode.id
                      ? "border-accent bg-blue-50/70 shadow-xs ring-1 ring-accent"
                      : "border-slate-200 bg-white hover:bg-slate-50"
                  }`}
                >
                  <div className="flex items-center justify-between mb-1">
                    <span className={`text-xs font-bold ${algorithm === mode.id ? "text-accent" : "text-navy"}`}>
                      {mode.label}
                    </span>
                    <span className="text-[10px] font-mono px-1.5 py-0.5 bg-slate-100 text-slate-600 rounded">
                      {mode.badge}
                    </span>
                  </div>
                  <span className="text-[11px] text-slate-500 line-clamp-2 leading-tight">
                    {mode.desc}
                  </span>
                </button>
              ))}
            </div>
          </div>

          {/* Footer Actions */}
          <div className="pt-4 border-t border-slate-100 flex items-center justify-between">
            <button
              type="button"
              onClick={handleReset}
              className="px-4 py-2 text-xs font-medium text-slate-500 hover:text-slate-800 transition-colors cursor-pointer rounded-lg hover:bg-slate-100"
            >
              Reset Filters
            </button>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-xs font-medium text-slate-600 hover:text-slate-800 border border-slate-200 rounded-xl hover:bg-slate-50 transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-5 py-2 text-xs font-semibold bg-accent hover:bg-accent-dark text-white rounded-xl shadow-xs transition-colors cursor-pointer flex items-center gap-1.5"
              >
                <SearchIcon className="w-3.5 h-3.5" />
                <span>Apply & Search</span>
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}
