import Logo from "../components/Logo"
import { MOCK_DOCUMENTS, MOCK_RESULTS } from "../data"
import { highlightTerms, formatDate } from "../utils"
import type { Screen } from "../types"
import { ArrowLeftIcon, ClockIcon, DocumentIcon } from "../icons"

type Props = {
  query: string
  resultId: string
  onBack: () => void
  onNavigate: (screen: Screen) => void
}

function MetaChip({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="text-xs text-slate-400 uppercase tracking-wider font-medium">{label}</span>
      <span className="text-sm font-mono text-slate-800 font-medium">{value}</span>
    </div>
  )
}

function AnalysisRow({ label, value, mono = false }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="flex items-center justify-between py-2.5 border-b border-slate-100 last:border-0">
      <span className="text-xs text-slate-500">{label}</span>
      <span className={`text-sm font-medium text-slate-900 ${mono ? "font-mono" : ""}`}>{value}</span>
    </div>
  )
}

export default function DocumentDetails({ query, resultId, onBack, onNavigate }: Props) {
  const doc = MOCK_DOCUMENTS[resultId]
  const result = MOCK_RESULTS.find((r) => r.id === resultId)

  if (!doc && !result) {
    return (
      <div className="min-h-screen bg-page-bg flex items-center justify-center">
        <div className="text-center">
          <p className="text-slate-500 mb-4">Document not found.</p>
          <button onClick={onBack} className="px-4 py-2 bg-navy text-white rounded-lg text-sm">
            Back to results
          </button>
        </div>
      </div>
    )
  }

  const title = doc?.title ?? result?.title ?? "Document"
  const path = doc?.path ?? result?.path ?? ""
  const size = doc?.size ?? result?.size ?? "—"
  const matches = doc?.matches ?? result?.matches ?? 0
  const relevance = doc?.relevance ?? result?.relevance ?? 0
  const algorithm = doc?.algorithm ?? result?.algorithm ?? "KMP"
  const searchTime = doc?.searchTime ?? 18
  const index = doc?.index ?? "Positional Inverted Index"
  const pattern = doc?.pattern ?? query
  const wordCount = doc?.wordCount ?? 0
  const date = doc?.date ?? result?.date ?? ""
  const overview = doc?.overview ?? result?.snippet ?? ""
  const sections = doc?.sections ?? []

  return (
    <div className="min-h-screen flex flex-col bg-page-bg">
      {/* Header */}
      <header className="sticky top-0 z-20 bg-white border-b border-slate-200 shadow-[0_1px_3px_rgba(0,0,0,0.04)]">
        <div className="max-w-screen-xl mx-auto px-6 h-16 flex items-center gap-4">
          <Logo onClick={() => onNavigate("home")} />
          <div className="h-5 w-px bg-slate-200" />
          <button
            onClick={onBack}
            className="flex items-center gap-2 text-sm text-slate-500 hover:text-navy transition-colors font-medium"
          >
            <ArrowLeftIcon className="w-4 h-4" />
            Back to results
          </button>
          <div className="ml-auto hidden sm:flex items-center gap-1">
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
          </div>
        </div>
      </header>

      <main className="flex-1 max-w-screen-xl mx-auto w-full px-4 sm:px-6 py-8">
        {/* Document title + meta */}
        <div className="mb-6">
          <div className="flex items-center gap-2 text-xs font-mono text-path-green mb-3">
            <DocumentIcon className="w-3.5 h-3.5" />
            {path}
          </div>
          <h1 className="text-3xl sm:text-4xl font-bold text-navy tracking-tight mb-4 leading-tight">
            {title}
          </h1>
          <div className="flex flex-wrap gap-5 bg-white border border-slate-200 rounded-xl px-6 py-4">
            <MetaChip label="Document ID" value={resultId} />
            <MetaChip label="Size" value={size} />
            <MetaChip label="Matches" value={`${matches}`} />
            <MetaChip label="Relevance" value={`${relevance}%`} />
            <MetaChip label="Algorithm" value={algorithm} />
            {wordCount > 0 && <MetaChip label="Words" value={wordCount.toLocaleString()} />}
            {date && <MetaChip label="Indexed" value={formatDate(date)} />}
          </div>
        </div>

        <div className="flex flex-col lg:flex-row gap-6">
          {/* Document content */}
          <div className="flex-1 min-w-0 space-y-5">
            {/* Overview */}
            <section className="bg-white border border-slate-200 rounded-xl p-6">
              <h2 className="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-3">Overview</h2>
              <p className="text-sm text-slate-700 leading-relaxed">
                {highlightTerms(overview, query)}
              </p>
            </section>

            {/* Matched content sections */}
            {sections.length > 0 ? (
              <section className="bg-white border border-slate-200 rounded-xl overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-100">
                  <h2 className="text-xs font-semibold uppercase tracking-widest text-slate-400">Matched Content</h2>
                </div>
                <div className="divide-y divide-slate-100">
                  {sections.map((section, idx) => (
                    <div key={idx} className="px-6 py-5">
                      <h3 className="font-semibold text-slate-900 text-sm mb-2">{section.heading}</h3>
                      <p className="text-sm text-slate-600 leading-relaxed">
                        {highlightTerms(section.content, query)}
                      </p>
                    </div>
                  ))}
                </div>
              </section>
            ) : (
              <section className="bg-white border border-slate-200 rounded-xl p-6">
                <h2 className="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-3">Matched Content</h2>
                <blockquote className="border-l-4 border-accent pl-4 py-1">
                  <p className="text-sm text-slate-700 leading-relaxed italic">
                    {highlightTerms(result?.snippet ?? overview, query)}
                  </p>
                </blockquote>
                <p className="text-xs text-slate-400 mt-4 font-mono">
                  Full document content available in corpus at <span className="text-slate-600">{path}</span>
                </p>
              </section>
            )}
          </div>

          {/* Technical analysis panel */}
          <aside className="w-full lg:w-72 flex-shrink-0">
            <div className="bg-white border border-slate-200 rounded-xl overflow-hidden sticky top-24">
              {/* Header */}
              <div className="bg-navy px-5 py-4">
                <h2 className="font-semibold text-white text-sm">Search Analysis</h2>
                <p className="text-xs text-blue-200 mt-0.5 font-mono">Technical metadata</p>
              </div>

              {/* Relevance score visual */}
              <div className="px-5 py-4 border-b border-slate-100">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs text-slate-500">Document Score</span>
                  <span className="text-sm font-mono font-bold text-accent">{(relevance / 100).toFixed(2)}</span>
                </div>
                <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-accent to-blue-400 rounded-full transition-all duration-500"
                    style={{ width: `${relevance}%` }}
                  />
                </div>
                <div className="flex justify-between mt-1">
                  <span className="text-xs text-slate-400 font-mono">0.00</span>
                  <span className="text-xs font-mono font-semibold text-accent">{relevance}% relevance</span>
                  <span className="text-xs text-slate-400 font-mono">1.00</span>
                </div>
              </div>

              {/* Stats */}
              <div className="px-5 py-2">
                <AnalysisRow label="Algorithm" value={algorithm} mono />
                <AnalysisRow label="Pattern" value={`"${pattern}"`} mono />
                <AnalysisRow label="Matches found" value={`${matches}`} mono />
                <AnalysisRow
                  label="Search time"
                  value={`${searchTime} ms`}
                  mono
                />
                <AnalysisRow label="Index type" value={index} />
                <AnalysisRow label="Document size" value={size} mono />
                {wordCount > 0 && <AnalysisRow label="Word count" value={wordCount.toLocaleString()} mono />}
              </div>

              {/* Performance indicator */}
              <div className="mx-5 mb-5 mt-2 bg-slate-50 rounded-lg px-4 py-3 border border-slate-100">
                <div className="flex items-center gap-2 mb-1">
                  <ClockIcon className="w-3.5 h-3.5 text-slate-400" />
                  <span className="text-xs text-slate-500 font-medium">Performance</span>
                </div>
                <p className="text-xs text-slate-500 leading-relaxed">
                  Pattern matched in{" "}
                  <span className="font-mono text-slate-700">{searchTime} ms</span> using{" "}
                  <span className="font-mono text-slate-700">{algorithm}</span> with{" "}
                  <span className="font-mono text-slate-700">O(n+m)</span> time complexity.
                </p>
              </div>
            </div>
          </aside>
        </div>
      </main>
    </div>
  )
}
