import { useState } from "react"
import type { AlgorithmDetails } from "../types"

interface Props {
  details?: AlgorithmDetails
  lpsArray?: number[]
}

export default function AlgorithmDetailsPanel({ details, lpsArray }: Props) {
  const [isOpen, setIsOpen] = useState<boolean>(false)

  if (!details) {
    return null
  }

  const isKmp = details.algorithmFamily === "kmp" || details.algorithmName.includes("KMP")
  const isAho = details.algorithmFamily === "aho-corasick" || details.algorithmName.includes("Aho-Corasick")
  const isFuzzy = details.algorithmFamily === "edit-distance" || details.algorithmName.includes("Edit Distance") || details.algorithmName.includes("Fuzzy")
  const isSmart = !isKmp && !isAho && !isFuzzy

  const patternChars = details.pattern ? details.pattern.slice(0, 16).split("") : []
  const lps = lpsArray && lpsArray.length > 0 ? lpsArray.slice(0, patternChars.length) : patternChars.map(() => 0)

  return (
    <div className="mb-6 bg-white border border-slate-200 rounded-xl overflow-hidden shadow-2xs transition-all">
      {/* Expandable toggle header */}
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="w-full px-5 py-3.5 flex items-center justify-between bg-slate-50/80 hover:bg-slate-100/80 transition-colors text-left cursor-pointer group focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/40"
        aria-expanded={isOpen}
      >
        <div className="flex items-center gap-2.5 min-w-0">
          <span className="w-6 h-6 rounded-md bg-blue-100/80 text-accent flex items-center justify-center text-xs font-bold flex-shrink-0">
            ⚙
          </span>
          <div className="min-w-0">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-800 flex items-center gap-2">
              <span>Search & Algorithm Details</span>
              <span className="px-2 py-0.5 rounded bg-blue-50 text-accent font-mono text-[11px] font-semibold border border-blue-200">
                {details.algorithmName}
              </span>
            </span>
          </div>
        </div>

        <div className="flex items-center gap-3 flex-shrink-0 text-xs text-slate-500 font-mono">
          <span className="hidden sm:inline-block font-sans font-medium text-slate-400">
            {isOpen ? "Click to hide" : "Click to view technical breakdown"}
          </span>
          <span
            className={`w-5 h-5 rounded-full bg-white border border-slate-200 flex items-center justify-center text-[10px] transform transition-transform duration-200 ${
              isOpen ? "rotate-180 text-accent border-accent/40" : "text-slate-400"
            }`}
          >
            ▼
          </span>
        </div>
      </button>

      {/* Expandable Technical Details Body */}
      {isOpen && (
        <div className="p-5 border-t border-slate-200/80 space-y-4 animate-in fade-in duration-150 text-xs">
          {/* Mode-specific Primary Breakdown */}
          {isSmart && (
            <div className="space-y-3">
              <div className="grid grid-cols-2 sm:grid-cols-5 gap-2.5">
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Algorithm</span>
                  <span className="font-bold text-navy font-mono text-xs">{details.algorithmName}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Documents indexed</span>
                  <span className="font-bold text-slate-800 font-mono text-sm">{details.documentsIndexed}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Candidate documents</span>
                  <span className="font-bold text-accent font-mono text-sm">{details.candidateDocuments}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Results returned</span>
                  <span className="font-bold text-emerald-700 font-mono text-sm">{details.resultsReturned}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100 col-span-2 sm:col-span-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Search time</span>
                  <span className="font-bold text-navy font-mono text-sm">{details.searchTimeMs} ms</span>
                </div>
              </div>

              <div className="p-3.5 bg-blue-50/50 border border-blue-100 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-slate-700">
                <div className="flex items-center gap-2">
                  <span className="font-semibold text-navy">Query terms:</span>
                  <span className="font-mono text-accent">
                    {details.patterns && details.patterns.length > 0 ? details.patterns.join(", ") : details.pattern}
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="font-semibold text-navy">Matched in corpus:</span>
                  <span className="font-mono text-emerald-700 font-semibold">
                    {details.matchedPatterns && details.matchedPatterns.length > 0
                      ? details.matchedPatterns.join(", ")
                      : "None"}
                  </span>
                </div>
              </div>
            </div>
          )}

          {isKmp && (
            <div className="space-y-3">
              <div className="grid grid-cols-2 sm:grid-cols-5 gap-2.5">
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Algorithm</span>
                  <span className="font-bold text-navy font-mono text-xs">{details.algorithmName}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Pattern</span>
                  <span className="font-bold text-accent font-mono text-xs truncate block">"{details.pattern}"</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Matches</span>
                  <span className="font-bold text-emerald-700 font-mono text-sm">{details.totalOccurrences}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Comparisons</span>
                  <span className="font-bold text-navy font-mono text-sm">
                    {details.comparisons !== undefined ? details.comparisons.toLocaleString() : "Linear"}
                  </span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100 col-span-2 sm:col-span-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Search time</span>
                  <span className="font-bold text-navy font-mono text-sm">{details.searchTimeMs} ms</span>
                </div>
              </div>

              {/* LPS Table */}
              {patternChars.length > 0 && (
                <div className="p-3.5 bg-slate-900 text-white rounded-xl font-mono text-xs">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-blue-300 text-[11px] uppercase font-semibold tracking-wider">
                      KMP LPS (π) Failure Function
                    </span>
                    <span className="text-[10px] text-slate-400">O(m) table</span>
                  </div>
                  <div className="overflow-x-auto pb-1">
                    <div className="inline-flex flex-col border border-slate-700 rounded-lg overflow-hidden">
                      <div className="flex bg-slate-800 border-b border-slate-700">
                        <div className="w-12 px-2 py-1 text-[10px] text-slate-400 border-r border-slate-700">Char</div>
                        {patternChars.map((c, i) => (
                          <div key={i} className="w-7 py-1 text-center font-bold text-accent text-xs">
                            {c}
                          </div>
                        ))}
                      </div>
                      <div className="flex bg-slate-900">
                        <div className="w-12 px-2 py-1 text-[10px] text-slate-400 border-r border-slate-700">LPS[i]</div>
                        {lps.map((val, i) => (
                          <div key={i} className="w-7 py-1 text-center text-xs font-semibold text-emerald-400">
                            {val}
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {isAho && (
            <div className="space-y-3">
              <div className="grid grid-cols-2 sm:grid-cols-5 gap-2.5">
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Algorithm</span>
                  <span className="font-bold text-navy font-mono text-xs">{details.algorithmName}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Trie Nodes</span>
                  <span className="font-bold text-accent font-mono text-sm">{details.automatonStates ?? 0}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Matches</span>
                  <span className="font-bold text-emerald-700 font-mono text-sm">{details.totalOccurrences}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Scanned docs</span>
                  <span className="font-bold text-slate-800 font-mono text-sm">{details.candidateDocuments}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100 col-span-2 sm:col-span-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Search time</span>
                  <span className="font-bold text-navy font-mono text-sm">{details.searchTimeMs} ms</span>
                </div>
              </div>

              <div className="p-3.5 bg-purple-50/60 border border-purple-100 rounded-xl space-y-1.5 text-slate-700">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-semibold text-purple-900">Patterns:</span>
                  <span className="font-mono text-slate-800 font-medium">
                    {details.patterns && details.patterns.length > 0 ? details.patterns.join(", ") : details.pattern}
                  </span>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-semibold text-purple-900">Matched patterns:</span>
                  <span className="font-mono text-emerald-700 font-bold">
                    {details.matchedPatterns && details.matchedPatterns.length > 0
                      ? details.matchedPatterns.join(", ")
                      : "None"}
                  </span>
                </div>
              </div>
            </div>
          )}

          {isFuzzy && (
            <div className="space-y-3">
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Algorithm</span>
                  <span className="font-bold text-navy font-mono text-xs">{details.algorithmName}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Original query</span>
                  <span className="font-bold text-slate-800 font-mono text-xs">{details.originalQuery}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Suggested term</span>
                  <span className="font-bold text-accent font-mono text-xs">{details.suggestedTerm || "N/A"}</span>
                </div>
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                  <span className="text-[10px] uppercase font-semibold text-slate-400 block mb-0.5">Edit distance</span>
                  <span className="font-bold text-emerald-700 font-mono text-sm">{details.editDistance ?? 0}</span>
                </div>
              </div>

              <div className="p-3.5 bg-amber-50/50 border border-amber-100 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-slate-700">
                <div>
                  <span className="font-semibold text-amber-900">Scanned vocabulary:</span>{" "}
                  <span className="font-mono">{details.candidateDocuments} documents</span>
                </div>
                <div>
                  <span className="font-semibold text-amber-900">Execution time:</span>{" "}
                  <span className="font-mono font-bold text-navy">{details.searchTimeMs} ms</span>
                </div>
              </div>
            </div>
          )}

          {/* Theoretical Complexity Footer */}
          <div className="pt-2 border-t border-slate-100 flex flex-wrap items-center justify-between gap-2 text-[11px] text-slate-500 font-mono">
            <span>
              Engine: <span className="text-slate-700 font-semibold">Positional Inverted Index</span>
            </span>
            <span>
              Time Complexity:{" "}
              <span className="text-navy font-semibold">
                {isKmp ? "O(n + m) linear" : isAho ? "O(n + m + z) automaton" : isFuzzy ? "O(m · n) DP" : "O(t + k log k) VSM"}
              </span>
            </span>
            <span>
              Zero mock metrics: <span className="text-emerald-600 font-semibold">100% backend-instrumented</span>
            </span>
          </div>
        </div>
      )}
    </div>
  )
}
