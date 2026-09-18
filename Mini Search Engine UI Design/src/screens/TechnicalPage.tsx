import { useState, useEffect } from "react"
import Logo from "../components/Logo"
import SearchBar from "../components/SearchBar"
import { ALGORITHMS, PIPELINE_STEPS } from "../data"
import type { Screen } from "../types"
import { fetchAlgorithms, fetchStatistics, type AlgorithmInfo } from "../api"

type Props = {
  onSearch: (query: string) => void
  onNavigate: (screen: Screen) => void
}

function AlgorithmCard({ algo }: { algo: AlgorithmInfo }) {
  return (
    <div className="bg-white border border-slate-200 rounded-xl p-5 hover:border-slate-300 hover:shadow-sm transition-all duration-150 group">
      <div className="flex items-start justify-between mb-3 gap-2">
        <div>
          <h3 className="font-bold text-navy text-base leading-tight">{algo.name}</h3>
          <p className="text-xs text-slate-500 mt-0.5">{algo.fullName}</p>
        </div>
        <span className="flex-shrink-0 px-2 py-0.5 bg-slate-100 text-slate-600 text-xs font-mono rounded border border-slate-200">
          {algo.complexity}
        </span>
      </div>
      <p className="text-xs font-semibold text-accent mb-2 uppercase tracking-wide">{algo.purpose}</p>
      <p className="text-xs text-slate-500 leading-relaxed mb-3">{algo.description}</p>
      <div className="flex items-center justify-between text-xs text-slate-400 pt-3 border-t border-slate-100">
        <span className="font-mono">Space: {algo.spaceComplexity}</span>
        <span className="text-accent font-medium">Implemented</span>
      </div>
    </div>
  )
}

function PipelineStep({ step }: { step: (typeof PIPELINE_STEPS)[0] }) {
  const isLast = step.step === PIPELINE_STEPS.length
  return (
    <div className="relative flex gap-5">
      {/* Connector line */}
      {!isLast && (
        <div className="absolute left-[19px] top-10 bottom-0 w-px bg-slate-200" />
      )}
      {/* Step number circle */}
      <div className="relative z-10 w-10 h-10 flex-shrink-0 bg-navy text-white rounded-full flex items-center justify-center text-sm font-mono font-semibold shadow-sm">
        {step.step}
      </div>
      {/* Content */}
      <div className="pb-7 pt-1.5 flex-1">
        <h3 className="font-semibold text-slate-900 text-sm mb-1">{step.name}</h3>
        <p className="text-xs text-slate-500 leading-relaxed">{step.description}</p>
      </div>
    </div>
  )
}

export default function TechnicalPage({ onSearch, onNavigate }: Props) {
  const [algorithmsList, setAlgorithmsList] = useState<AlgorithmInfo[]>(ALGORITHMS)
  const [statsData, setStatsData] = useState({
    totalDocs: "45",
    totalTokens: "128K",
    uniqueTerms: "8,471",
    avgTokens: "2,853",
  })

  useEffect(() => {
    fetchAlgorithms().then((algos) => {
      if (algos && algos.length > 0) {
        setAlgorithmsList(algos)
      }
    })

    fetchStatistics().then((s) => {
      setStatsData({
        totalDocs: s.totalDocuments.toLocaleString(),
        totalTokens: `${Math.round(s.totalTokens / 1000)}K`,
        uniqueTerms: s.uniqueTerms.toLocaleString(),
        avgTokens: s.avgTokensPerDoc.toLocaleString(),
      })
    })
  }, [])

  const systemStats = [
    { label: "Total Documents", value: statsData.totalDocs, sub: "corpus files" },
    { label: "Total Tokens", value: statsData.totalTokens, sub: "tokenized stream" },
    { label: "Unique Terms", value: statsData.uniqueTerms, sub: "vocabulary size" },
    { label: "Avg. Tokens/Doc", value: statsData.avgTokens, sub: "post-normalization" },
  ]

  return (
    <div className="min-h-screen flex flex-col bg-page-bg">
      {/* Header */}
      <header className="sticky top-0 z-20 bg-white border-b border-slate-200 shadow-[0_1px_3px_rgba(0,0,0,0.04)]">
        <div className="max-w-screen-xl mx-auto px-6 h-16 flex items-center gap-4 sm:gap-6">
          <Logo onClick={() => onNavigate("home")} />
          <div className="flex-1 max-w-xl">
            <SearchBar onSearch={onSearch} size="compact" placeholder="Search documents, topics, or keywords..." />
          </div>
          <nav className="hidden sm:flex items-center gap-1 ml-auto flex-shrink-0">
            <button
              onClick={() => onNavigate("home")}
              className="px-3 py-1.5 text-sm text-slate-600 hover:text-navy hover:bg-slate-50 rounded-lg transition-colors font-medium"
            >
              Search
            </button>
            <button className="px-3 py-1.5 text-sm text-navy bg-slate-100 rounded-lg font-medium">
              Technical
            </button>
          </nav>
        </div>
      </header>

      <main className="flex-1 max-w-screen-xl mx-auto w-full px-4 sm:px-6 py-10">
        {/* Page heading */}
        <div className="mb-10">
          <p className="text-xs font-mono text-accent uppercase tracking-widest mb-2">Architecture</p>
          <h1 className="text-3xl sm:text-4xl font-bold text-navy tracking-tight mb-3">
            How the Search Engine Works
          </h1>
          <p className="text-base text-slate-500 max-w-2xl leading-relaxed">
            A walkthrough of the information retrieval pipeline — from raw corpus ingestion through text
            normalization, indexing, pattern matching, and ranked result delivery.
          </p>
        </div>

        {/* System stats */}
        <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden mb-10">
          <div className="grid grid-cols-2 sm:grid-cols-4 divide-x divide-slate-100">
            {systemStats.map((stat, i) => (
              <div key={stat.label} className={`px-6 py-5 ${i >= 2 ? "border-t sm:border-t-0 border-slate-100" : ""}`}>
                <div className="text-2xl font-bold font-mono text-navy">{stat.value}</div>
                <div className="text-sm font-medium text-slate-700 mt-0.5">{stat.label}</div>
                <div className="text-xs text-slate-400 mt-0.5">{stat.sub}</div>
              </div>
            ))}
          </div>
        </div>

        <div className="flex flex-col lg:flex-row gap-10">
          {/* Pipeline */}
          <section className="lg:w-72 flex-shrink-0">
            <h2 className="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-6">Processing Pipeline</h2>
            <div className="bg-white border border-slate-200 rounded-xl p-6">
              {PIPELINE_STEPS.map((step) => (
                <PipelineStep key={step.step} step={step} />
              ))}
            </div>
          </section>

          {/* Algorithms */}
          <section className="flex-1 min-w-0">
            <h2 className="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-6">
              Implemented Algorithms
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
              {algorithmsList.map((algo) => (
                <AlgorithmCard key={algo.id} algo={algo} />
              ))}
            </div>

            {/* Index architecture note */}
            <div className="mt-8 bg-navy rounded-2xl p-6 text-white">
              <p className="text-xs font-mono text-blue-300 uppercase tracking-widest mb-2">Index Architecture</p>
              <h3 className="text-lg font-bold mb-2">Positional Inverted Index</h3>
              <p className="text-sm text-blue-100 leading-relaxed mb-4">
                The core data structure maps each unique stemmed term to a posting list containing document IDs
                and positional offsets. This enables both exact-phrase queries and proximity-based ranking.
              </p>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                {[
                  { label: "Lookup", value: "O(1)", desc: "Hash-based term lookup" },
                  { label: "Phrase Query", value: "O(p)", desc: "Position merging" },
                  { label: "Ranking", value: "O(k log k)", desc: "TF-IDF sort" },
                ].map((item) => (
                  <div key={item.label} className="bg-white/10 rounded-xl px-4 py-3">
                    <div className="text-xs text-blue-300 mb-0.5">{item.label}</div>
                    <div className="text-base font-mono font-bold text-white">{item.value}</div>
                    <div className="text-xs text-blue-200 mt-0.5">{item.desc}</div>
                  </div>
                ))}
              </div>
            </div>

            {/* Query flow */}
            <div className="mt-6 bg-white border border-slate-200 rounded-2xl p-6">
              <h3 className="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-4">Query → Result Flow</h3>
              <div className="flex flex-wrap items-center gap-2 text-sm">
                {["User Query", "Normalize", "Tokenize", "Index Lookup", "Pattern Match", "TF-IDF Score", "Rank", "Return"].map(
                  (step, i, arr) => (
                    <div key={step} className="flex items-center gap-2">
                      <div className="px-3 py-1.5 bg-slate-100 border border-slate-200 rounded-lg text-xs font-mono text-slate-700 whitespace-nowrap">
                        {step}
                      </div>
                      {i < arr.length - 1 && (
                        <svg viewBox="0 0 16 16" fill="none" className="w-4 h-4 text-slate-300 flex-shrink-0">
                          <path d="M3 8h10M9 4l4 4-4 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                        </svg>
                      )}
                    </div>
                  )
                )}
              </div>
            </div>
          </section>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-200 bg-white mt-10">
        <div className="max-w-screen-xl mx-auto px-6 py-4 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500">
          <span>Mini Search Engine · University Semester Project · 2024</span>
          <button
            onClick={() => onNavigate("home")}
            className="hover:text-slate-700 transition-colors"
          >
            ← Back to Home
          </button>
        </div>
      </footer>
    </div>
  )
}
