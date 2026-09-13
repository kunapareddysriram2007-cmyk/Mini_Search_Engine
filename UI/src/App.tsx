import { useState } from "react"
import SearchHome from "./screens/SearchHome"
import SearchResults from "./screens/SearchResults"
import DocumentDetails from "./screens/DocumentDetails"
import TechnicalPage from "./screens/TechnicalPage"
import type { AppState, Filters, Screen } from "./types"

const defaultFilters: Filters = {
  docType: "all",
  matchType: "exact",
  sortBy: "relevance",
  algorithm: "auto",
}

const initialState: AppState = {
  screen: "home",
  query: "",
  selectedResultId: null,
  filters: defaultFilters,
  isLoading: false,
  currentPage: 1,
}

export default function App() {
  const [state, setState] = useState<AppState>(initialState)

  const update = (patch: Partial<AppState>) =>
    setState((prev) => ({ ...prev, ...patch }))

  const handleSearch = (query: string) => {
    if (!query.trim()) return
    update({ screen: "results", query, isLoading: true, currentPage: 1 })
    setTimeout(() => update({ isLoading: false }), 700)
  }

  const handleOpenDocument = (id: string) => {
    update({ screen: "document", selectedResultId: id })
  }

  const handleBack = () => {
    update({ screen: "results" })
  }

  const handleNavigate = (screen: Screen) => {
    update({ screen })
  }

  const handleFilterChange = (key: keyof Filters, value: string) => {
    update({ filters: { ...state.filters, [key]: value }, currentPage: 1 })
  }

  const handlePageChange = (page: number) => {
    update({ currentPage: page })
    window.scrollTo({ top: 0, behavior: "smooth" })
  }

  switch (state.screen) {
    case "home":
      return <SearchHome onSearch={handleSearch} onNavigate={handleNavigate} />

    case "results":
      return (
        <SearchResults
          query={state.query}
          filters={state.filters}
          isLoading={state.isLoading}
          currentPage={state.currentPage}
          onSearch={handleSearch}
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
      return <TechnicalPage onSearch={handleSearch} onNavigate={handleNavigate} />

    default:
      return <SearchHome onSearch={handleSearch} onNavigate={handleNavigate} />
  }
}
