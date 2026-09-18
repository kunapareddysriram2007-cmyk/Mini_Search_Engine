import { useState, useEffect, useCallback, useRef } from "react"
import SearchHome from "./screens/SearchHome"
import SearchResults from "./screens/SearchResults"
import DocumentDetails from "./screens/DocumentDetails"
import TechnicalPage from "./screens/TechnicalPage"
import type { AppState, Filters, Screen } from "./types"

const defaultFilters: Filters = {
  docType: "all",
  category: "all",
  language: "all",
  matchType: "exact",
  sortBy: "relevance",
  algorithm: "smart",
}

function parseStateFromUrl(): AppState {
  try {
    const params = new URLSearchParams(window.location.search)
    const doc = params.get("doc")
    const view = params.get("view")
    const q = params.get("q")
    const algo = params.get("algo") || params.get("mode") || "smart"
    const page = parseInt(params.get("page") || "1", 10)
    const cat = params.get("cat") || params.get("category") || "all"
    const lang = params.get("lang") || params.get("language") || "all"
    const type = params.get("type") || params.get("docType") || "all"
    const sort = params.get("sort") || params.get("sortBy") || "relevance"
    const exactPhrase = params.get("phrase") || undefined
    const includeTerms = params.get("include") || undefined
    const excludeTerms = params.get("exclude") || undefined

    let matchType = "exact"
    if (algo === "multi-pattern" || algo === "aho-corasick") matchType = "partial"
    else if (algo === "fuzzy" || algo === "edit-distance") matchType = "fuzzy"

    const filters: Filters = {
      docType: type,
      category: cat,
      language: lang,
      matchType,
      sortBy: sort,
      algorithm: algo,
      exactPhrase,
      includeTerms,
      excludeTerms,
    }

    if (doc) {
      return {
        screen: "document",
        query: q || "",
        selectedResultId: doc,
        filters,
        isLoading: false,
        currentPage: isNaN(page) ? 1 : Math.max(1, page),
      }
    }

    if (view === "technical") {
      return {
        screen: "technical",
        query: q || "",
        selectedResultId: null,
        filters,
        isLoading: false,
        currentPage: isNaN(page) ? 1 : Math.max(1, page),
      }
    }

    if (q !== null && q.trim().length > 0) {
      return {
        screen: "results",
        query: q,
        selectedResultId: null,
        filters,
        isLoading: false,
        currentPage: isNaN(page) ? 1 : Math.max(1, page),
      }
    }
  } catch {
    // Fallback on error
  }

  return {
    screen: "home",
    query: "",
    selectedResultId: null,
    filters: defaultFilters,
    isLoading: false,
    currentPage: 1,
  }
}

function buildUrlForState(state: AppState): string {
  const pathname = window.location.pathname || "/"
  const params = new URLSearchParams()

  if (state.screen === "document" && state.selectedResultId) {
    params.set("doc", state.selectedResultId)
    if (state.query) params.set("q", state.query)
    return `${pathname}?${params.toString()}`
  }

  if (state.screen === "technical") {
    params.set("view", "technical")
    if (state.query) params.set("q", state.query)
    return `${pathname}?${params.toString()}`
  }

  if (state.screen === "results" && state.query.trim()) {
    params.set("q", state.query.trim())
    if (state.filters.algorithm && state.filters.algorithm !== "smart") {
      params.set("algo", state.filters.algorithm)
    }
    if (state.currentPage > 1) {
      params.set("page", state.currentPage.toString())
    }
    if (state.filters.category && state.filters.category !== "all") {
      params.set("cat", state.filters.category)
    }
    if (state.filters.language && state.filters.language !== "all") {
      params.set("lang", state.filters.language)
    }
    if (state.filters.docType && state.filters.docType !== "all") {
      params.set("type", state.filters.docType)
    }
    if (state.filters.sortBy && state.filters.sortBy !== "relevance") {
      params.set("sort", state.filters.sortBy)
    }
    if (state.filters.exactPhrase) {
      params.set("phrase", state.filters.exactPhrase)
    }
    if (state.filters.includeTerms) {
      params.set("include", state.filters.includeTerms)
    }
    if (state.filters.excludeTerms) {
      params.set("exclude", state.filters.excludeTerms)
    }
    return `${pathname}?${params.toString()}`
  }

  return pathname
}

export default function App() {
  const [state, setState] = useState<AppState>(parseStateFromUrl)
  const isPopStateRef = useRef(false)
  const prevStateRef = useRef<AppState>(state)

  // Synchronize browser history URL whenever state changes
  useEffect(() => {
    if (isPopStateRef.current) {
      isPopStateRef.current = false
      prevStateRef.current = state
      return
    }
    const targetUrl = buildUrlForState(state)
    const currentUrl = `${window.location.pathname}${window.location.search}`
    if (targetUrl !== currentUrl) {
      const prev = prevStateRef.current
      // If primary navigation (screen, query, or selectedResultId changed), pushState.
      // If only secondary parameters (filters, pagination) changed on the same screen/query, replaceState.
      const isPrimaryNavigation =
        prev.screen !== state.screen ||
        prev.query !== state.query ||
        prev.selectedResultId !== state.selectedResultId

      if (isPrimaryNavigation) {
        window.history.pushState(state, "", targetUrl)
      } else {
        window.history.replaceState(state, "", targetUrl)
      }
    }
    prevStateRef.current = state
  }, [state])

  // Listen to browser Back and Forward button (popstate / undo events)
  useEffect(() => {
    const handlePopState = () => {
      isPopStateRef.current = true
      const parsed = parseStateFromUrl()
      setState(parsed)
    }

    window.addEventListener("popstate", handlePopState)
    return () => window.removeEventListener("popstate", handlePopState)
  }, [])

  const update = useCallback((patch: Partial<AppState>) => {
    setState((prev) => ({ ...prev, ...patch }))
  }, [])

  const handleSearch = useCallback((query: string, algorithm?: string) => {
    const trimmed = query.trim()
    if (!trimmed) {
      update({ screen: "home", query: "", currentPage: 1, isLoading: false })
      return
    }
    const algo = algorithm || state.filters.algorithm || "smart"
    let matchType = "exact"
    if (algo === "multi-pattern" || algo === "aho-corasick") matchType = "partial"
    else if (algo === "fuzzy" || algo === "edit-distance") matchType = "fuzzy"

    const updatedFilters: Filters = {
      ...state.filters,
      algorithm: algo,
      matchType,
    }
    update({ screen: "results", query: trimmed, filters: updatedFilters, isLoading: true, currentPage: 1 })
    window.scrollTo({ top: 0, behavior: "smooth" })
  }, [state.filters, update])

  const handleAdvancedSearch = useCallback((query: string, newFilters: Partial<Filters>) => {
    const mergedFilters: Filters = {
      ...state.filters,
      ...newFilters,
    }
    update({ screen: "results", query: query.trim(), filters: mergedFilters, isLoading: true, currentPage: 1 })
    window.scrollTo({ top: 0, behavior: "smooth" })
  }, [state.filters, update])

  const handleOpenDocument = useCallback((id: string) => {
    update({ screen: "document", selectedResultId: id })
    window.scrollTo({ top: 0, behavior: "smooth" })
  }, [update])

  const handleBack = useCallback(() => {
    // If there is browser history, navigate back; otherwise transition to results
    if (window.history.length > 1) {
      window.history.back()
    } else {
      update({ screen: state.query ? "results" : "home" })
    }
  }, [state.query, update])

  const handleNavigate = useCallback((screen: Screen) => {
    if (screen === "home") {
      update({ screen: "home", query: "", currentPage: 1 })
    } else {
      update({ screen })
    }
    window.scrollTo({ top: 0, behavior: "smooth" })
  }, [update])

  const handleFilterChange = useCallback((key: keyof Filters, value: string) => {
    if (key === "algorithm") {
      let matchType = "exact"
      if (value === "multi-pattern" || value === "aho-corasick") matchType = "partial"
      else if (value === "fuzzy" || value === "edit-distance") matchType = "fuzzy"
      update({
        filters: { ...state.filters, algorithm: value, matchType },
        currentPage: 1,
      })
      return
    }
    update({ filters: { ...state.filters, [key]: value }, currentPage: 1 })
  }, [state.filters, update])

  const handlePageChange = useCallback((page: number) => {
    update({ currentPage: page })
    window.scrollTo({ top: 0, behavior: "smooth" })
  }, [update])

  switch (state.screen) {
    case "home":
      return (
        <SearchHome
          onSearch={handleSearch}
          onAdvancedSearch={handleAdvancedSearch}
          onNavigate={handleNavigate}
        />
      )

    case "results":
      return (
        <SearchResults
          query={state.query}
          filters={state.filters}
          isLoading={state.isLoading}
          currentPage={state.currentPage}
          onSearch={handleSearch}
          onAdvancedSearch={handleAdvancedSearch}
          onOpenDocument={handleOpenDocument}
          onNavigate={handleNavigate}
          onFilterChange={handleFilterChange}
          onPageChange={handlePageChange}
        />
      )

    case "document":
      return (
        <DocumentDetails
          query={state.query}
          resultId={state.selectedResultId || "1"}
          onBack={handleBack}
          onNavigate={handleNavigate}
        />
      )

    case "technical":
      return <TechnicalPage onSearch={handleSearch} onNavigate={handleNavigate} />

    default:
      return (
        <SearchHome
          onSearch={handleSearch}
          onAdvancedSearch={handleAdvancedSearch}
          onNavigate={handleNavigate}
        />
      )
  }
}
