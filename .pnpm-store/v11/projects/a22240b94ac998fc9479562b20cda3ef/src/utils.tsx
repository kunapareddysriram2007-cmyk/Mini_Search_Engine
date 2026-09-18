import type { SearchResult, Filters } from "./types"

export function highlightTerms(text: string, query: string): React.ReactNode {
  if (!text || !query.trim()) return text
  const clean = query.replace(/["']/g, "").trim()
  const terms = clean
    .split(/\s+/)
    .filter((t) => t.length > 0)
    .map((t) => t.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"))
  if (terms.length === 0) return text
  const pattern = new RegExp(`(\\b(?:${terms.join("|")})\\b)`, "gi")
  const parts = text.split(pattern)
  return parts.map((part, i) => {
    const isMatch = terms.some((t) => new RegExp(`^${t}$`, "i").test(part))
    return isMatch ? <mark key={i} className="bg-amber-100 text-amber-950 font-semibold px-0.5 rounded">{part}</mark> : <span key={i}>{part}</span>
  })
}

export function filterAndSortResults(results: SearchResult[], filters: Filters): SearchResult[] {
  let filtered = [...results]

  if (filters.docType && filters.docType !== "all") {
    filtered = filtered.filter(
      (r) =>
        r.docType.toLowerCase() === filters.docType.toLowerCase() ||
        r.docType.toLowerCase().replace(/\s+/g, "-") === filters.docType.toLowerCase() ||
        r.path.toLowerCase().includes(filters.docType.toLowerCase())
    )
  }

  if (filters.sortBy === "alphabetical") {
    filtered.sort((a, b) => a.title.localeCompare(b.title))
  } else {
    filtered.sort((a, b) => b.relevance - a.relevance)
  }

  return filtered
}

export function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString("en-US", { year: "numeric", month: "short", day: "numeric" })
}

export function getDocTypeCounts(results: SearchResult[]) {
  const counts: Record<string, number> = { all: results.length }
  results.forEach((r) => {
    counts[r.docType] = (counts[r.docType] || 0) + 1
  })
  return counts
}
