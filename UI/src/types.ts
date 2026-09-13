export type Screen = "home" | "results" | "document" | "technical"

export interface SearchResult {
  id: string
  title: string
  path: string
  snippet: string
  relevance: number
  algorithm: string
  matches: number
  size: string
  docType: "Wikipedia" | "Article" | "Reference"
  date: string
}

export interface DocumentSection {
  heading: string
  content: string
}

export interface DocumentData {
  id: string
  title: string
  path: string
  size: string
  matches: number
  relevance: number
  algorithm: string
  overview: string
  sections: DocumentSection[]
  wordCount: number
  searchTime: number
  index: string
  pattern: string
  date: string
}

export interface Filters {
  docType: string
  matchType: string
  sortBy: string
  algorithm: string
}

export interface AppState {
  screen: Screen
  query: string
  selectedResultId: string | null
  filters: Filters
  isLoading: boolean
  currentPage: number
}
