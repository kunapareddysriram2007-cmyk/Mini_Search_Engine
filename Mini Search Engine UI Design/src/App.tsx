import { useEffect, useState } from "react"
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

const HISTORY_STATE_KEY = "miniSearchState"

type StoredHistoryState = {
  [HISTORY_STATE_KEY]?: AppState
}

function savedState(): AppState | null {
  const candidate = window.history.state as StoredHistoryState | null
  const state = candidate?.[HISTORY_STATE_KEY]

  return state && typeof state === "object" ? state : null
}

function getStateFromLocation(): AppState {
  const { pathname, search } = window.location
  const params = new URLSearchParams(search)
  const query = params.get("q")?.trim() || ""
  const documentMatch = pathname.match(/^\/document\/([^/]+)$/)
  const previous = savedState()
  const filters = previous?.filters ?? { ...defaultFilters }

  if (documentMatch) {
    return {
      screen: "document",
      query,
      selectedResultId: decodeURIComponent(documentMatch[1]),
      filters,
      isLoading: false,
      currentPage: previous?.currentPage ?? 1,
    }
  }

  if (pathname === "/search" && query) {
    return {
      screen: "results",
      query,
      selectedResultId: null,
      filters,
      isLoading: false,
      currentPage: previous?.currentPage ?? 1,
    }
  }

  return {
    screen: pathname === "/technical" ? "technical" : "home",
    query: "",
    selectedResultId: null,
    filters: { ...defaultFilters },
    isLoading: false,
    currentPage: 1,
  }
}

function searchUrl(query: string) {
  return `/search?q=${encodeURIComponent(query)}`
}

function historyState(state: AppState): StoredHistoryState {
  return { [HISTORY_STATE_KEY]: state }
}

export default function App() {
  const [state, setState] = useState<AppState>(getStateFromLocation)

  useEffect(() => {
    // Preserve the initial entry too, so a later Back/Forward restores its
    // search context even though it was not created by this app session.
    window.history.replaceState(historyState(state), "")

    const onPopState = () => setState(getStateFromLocation())
    window.addEventListener("popstate", onPopState)
    return () => window.removeEventListener("popstate", onPopState)
  }, [])

  const update = (patch: Partial<AppState>) => {
    const nextState = { ...state, ...patch }
    // Filter and pagination changes intentionally replace the active entry.
    // They remain restorable without creating a Back-button entry per click.
    window.history.replaceState(historyState(nextState), "")
    setState(nextState)
  }

  const pushState = (url: string, patch: Partial<AppState>) => {
    const currentUrl = `${window.location.pathname}${window.location.search}`
    const nextState = { ...state, ...patch }
    if (currentUrl !== url) {
      window.history.pushState(historyState(nextState), "", url)
    } else {
      window.history.replaceState(historyState(nextState), "", url)
    }
    setState(nextState)
  }

  const handleSearch = (rawQuery: string, algorithm?: string) => {
    const query = rawQuery.trim()
    if (!query) return

    const selectedAlgorithm = algorithm || state.filters.algorithm || "smart"
    const matchType =
      selectedAlgorithm === "multi-pattern" ||
      selectedAlgorithm === "aho-corasick"
        ? "partial"
        : selectedAlgorithm === "fuzzy" || selectedAlgorithm === "edit-distance"
          ? "fuzzy"
          : "exact"
    const filters = {
      ...state.filters,
      algorithm: selectedAlgorithm,
      matchType,
    }

    pushState(searchUrl(query), {
      screen: "results",
      query,
      selectedResultId: null,
      filters,
      // SearchResults owns the request lifecycle. Leaving this true permanently
      // previously prevented the loaded results from being shown.
      isLoading: false,
      currentPage: 1,
    })
  }

  const handleAdvancedSearch = (
    rawQuery: string,
    newFilters: Partial<Filters>,
  ) => {
    const query = rawQuery.trim()
    if (!query) return
    pushState(searchUrl(query), {
      screen: "results",
      query,
      selectedResultId: null,
      filters: { ...state.filters, ...newFilters },
      isLoading: false,
      currentPage: 1,
    })
  }

  const handleOpenDocument = (id: string) => {
    const url = `/document/${encodeURIComponent(id)}?q=${encodeURIComponent(state.query)}`
    pushState(url, {
      screen: "document",
      selectedResultId: id,
      isLoading: false,
    })
  }

  const handleBack = () => window.history.back()

  const handleNavigate = (screen: Screen) => {
    if (screen === "home") {
      pushState("/", {
        screen: "home",
        query: "",
        selectedResultId: null,
        isLoading: false,
        currentPage: 1,
      })
      return
    }
    if (screen === "technical") {
      pushState("/technical", {
        screen: "technical",
        selectedResultId: null,
        isLoading: false,
      })
      return
    }
    if (screen === "results" && state.query) {
      pushState(searchUrl(state.query), { screen: "results", isLoading: false })
    }
  }

  const handleFilterChange = (key: keyof Filters, value: string) => {
    const matchType =
      key === "algorithm"
        ? value === "multi-pattern" || value === "aho-corasick"
          ? "partial"
          : value === "fuzzy" || value === "edit-distance"
            ? "fuzzy"
            : "exact"
        : state.filters.matchType
    // Filters update the current result state without adding browser-history entries.
    update({
      filters: { ...state.filters, [key]: value, matchType },
      currentPage: 1,
      isLoading: false,
    })
  }

  const handlePageChange = (page: number) => {
    update({ currentPage: page, isLoading: false })
    window.scrollTo({ top: 0, behavior: "smooth" })
  }

  switch (state.screen) {
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
          resultId={state.selectedResultId!}
          onBack={handleBack}
          onNavigate={handleNavigate}
        />
      )
    case "technical":
      return (
        <TechnicalPage onSearch={handleSearch} onNavigate={handleNavigate} />
      )
    case "home":
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
