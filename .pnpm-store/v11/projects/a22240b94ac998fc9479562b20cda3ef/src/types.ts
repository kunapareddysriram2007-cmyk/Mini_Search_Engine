export type Screen = "home" | "results" | "document" | "technical"

export interface ScoreBreakdown {
  queryTerms?: string[]
  matchedTerms?: string[]
  termFrequencies?: Record<string, number>
  tfidfContributions?: Record<string, number>
  cosineSimilarity?: number
  finalRelevance?: number
  termsMatched?: number
  totalQueryTerms?: number
  termFrequency?: number
  tfidf?: number
  title?: number
  phrase?: number
  proximity?: number
}

export interface SearchResult {
  id: string
  title: string
  path: string
  snippet: string
  relevance: number
  algorithm: string
  matches: number
  size: string
  docType: string
  category?: string
  language?: string
  date: string
  scoreBreakdown?: ScoreBreakdown
  matchPositions?: number[]
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
  docType?: string
  category?: string
  language?: string
  keywords?: string[]
  citedBy?: string[]
  references?: string[]
}

export interface Filters {
  docType: string
  category: string
  language: string
  matchType: string
  sortBy: string
  algorithm: string
  exactPhrase?: string
  includeTerms?: string
  excludeTerms?: string
  relevanceTier?: string
}

export interface AdvancedSearchState {
  exactPhrase: string
  includeTerms: string
  excludeTerms: string
  category: string
  language: string
  algorithm: string
}

export interface AlgorithmDetails {
  algorithmName: string
  algorithmFamily: string
  documentsIndexed: number
  candidateDocuments: number
  resultsReturned: number
  searchTimeMs: number
  pattern?: string
  totalOccurrences?: number
  comparisons?: number
  patterns?: string[]
  matchedPatterns?: string[]
  automatonStates?: number
  originalQuery?: string
  suggestedTerm?: string
  editDistance?: number
}

export interface AppState {
  screen: Screen
  query: string
  selectedResultId: string | null
  filters: Filters
  isLoading: boolean
  currentPage: number
}

