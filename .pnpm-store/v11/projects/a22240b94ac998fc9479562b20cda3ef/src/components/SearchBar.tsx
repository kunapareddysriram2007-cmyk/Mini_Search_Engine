import { useState, useEffect, useRef, type FormEvent, type KeyboardEvent } from "react"
import { SearchIcon, ClockIcon, XIcon } from "../icons"
import { fetchSuggestions } from "../api"

type SearchBarProps = {
  value?: string
  onSearch: (query: string) => void
  placeholder?: string
  size?: "large" | "compact"
  autoFocus?: boolean
}

export default function SearchBar({
  value = "",
  onSearch,
  placeholder = "Search documents, topics, or \"exact phrase\"...",
  size = "large",
  autoFocus = false,
}: SearchBarProps) {
  const [localValue, setLocalValue] = useState(value)
  const [suggestions, setSuggestions] = useState<string[]>([])
  const [history, setHistory] = useState<string[]>([])
  const [isFocused, setIsFocused] = useState(false)
  const [selectedIndex, setSelectedIndex] = useState<number>(-1)
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    setLocalValue(value)
  }, [value])

  // Load search history from localStorage
  useEffect(() => {
    try {
      const stored = localStorage.getItem("mini_search_history")
      if (stored) {
        setHistory(JSON.parse(stored))
      }
    } catch {
      // ignore
    }
  }, [])

  // Fetch autocomplete suggestions as user types
  useEffect(() => {
    if (!localValue.trim()) {
      setSuggestions([])
      return
    }

    let cancelled = false
    const timer = setTimeout(() => {
      fetchSuggestions(localValue).then((res) => {
        if (!cancelled) {
          setSuggestions(res)
        }
      })
    }, 150)

    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [localValue])

  // Close dropdown on outside click
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsFocused(false)
      }
    }
    document.addEventListener("mousedown", handleClickOutside)
    return () => document.removeEventListener("mousedown", handleClickOutside)
  }, [])

  const saveHistory = (query: string) => {
    const q = query.trim()
    if (!q) return
    try {
      const filtered = [q, ...history.filter((h) => h.toLowerCase() !== q.toLowerCase())].slice(0, 6)
      setHistory(filtered)
      localStorage.setItem("mini_search_history", JSON.stringify(filtered))
    } catch {
      // ignore
    }
  }

  const clearHistory = (e: React.MouseEvent) => {
    e.stopPropagation()
    setHistory([])
    try {
      localStorage.removeItem("mini_search_history")
    } catch {
      // ignore
    }
  }

  const removeHistoryItem = (e: React.MouseEvent, item: string) => {
    e.stopPropagation()
    const updated = history.filter((h) => h !== item)
    setHistory(updated)
    try {
      localStorage.setItem("mini_search_history", JSON.stringify(updated))
    } catch {
      // ignore
    }
  }

  const handleSelect = (query: string) => {
    setLocalValue(query)
    saveHistory(query)
    setIsFocused(false)
    onSearch(query)
  }

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (selectedIndex >= 0) {
      const list = localValue.trim() ? suggestions : history
      if (list[selectedIndex]) {
        handleSelect(list[selectedIndex])
        return
      }
    }
    if (localValue.trim()) {
      saveHistory(localValue)
      setIsFocused(false)
      onSearch(localValue.trim())
    }
  }

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    const list = localValue.trim() ? suggestions : history

    if (e.key === "ArrowDown") {
      e.preventDefault()
      setSelectedIndex((prev) => (prev < list.length - 1 ? prev + 1 : 0))
    } else if (e.key === "ArrowUp") {
      e.preventDefault()
      setSelectedIndex((prev) => (prev > 0 ? prev - 1 : list.length - 1))
    } else if (e.key === "Escape") {
      setIsFocused(false)
    }
  }

  const showHistory = isFocused && !localValue.trim() && history.length > 0
  const showSuggestions = isFocused && localValue.trim().length > 0 && suggestions.length > 0

  if (size === "compact") {
    return (
      <div ref={containerRef} className="relative w-full">
        <form onSubmit={handleSubmit} className="flex items-center gap-2 w-full">
          <div className="flex-1 flex items-center bg-white border border-slate-300 rounded-lg focus-within:border-accent focus-within:ring-2 focus-within:ring-accent/20 transition-all duration-150">
            <SearchIcon className="ml-3 w-4 h-4 text-slate-400 flex-shrink-0" />
            <input
              className="flex-1 px-3 py-2 text-sm bg-transparent outline-none text-slate-900 placeholder:text-slate-400 font-sans"
              value={localValue}
              onChange={(e) => {
                setLocalValue(e.target.value)
                setSelectedIndex(-1)
              }}
              onFocus={() => setIsFocused(true)}
              onKeyDown={handleKeyDown}
              placeholder={placeholder}
              autoFocus={autoFocus}
            />
            {localValue && (
              <button
                type="button"
                onClick={() => setLocalValue("")}
                className="mr-2 text-slate-400 hover:text-slate-600 transition-colors cursor-pointer"
              >
                <XIcon className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
          <button
            type="submit"
            className="px-4 py-2 bg-navy hover:bg-navy-light text-white rounded-lg text-sm font-medium transition-colors flex-shrink-0 cursor-pointer"
          >
            Search
          </button>
        </form>

        {/* Dropdown for compact search bar */}
        {(showHistory || showSuggestions) && (
          <div className="absolute left-0 right-0 top-full mt-1.5 bg-white border border-slate-200 rounded-xl shadow-lg z-50 overflow-hidden py-1.5 animate-in fade-in slide-in-from-top-1 duration-150">
            {showHistory && (
              <div>
                <div className="px-3.5 py-1.5 flex items-center justify-between text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                  <span>Recent Searches</span>
                  <button
                    type="button"
                    onClick={clearHistory}
                    className="text-slate-400 hover:text-red-500 transition-colors lowercase font-normal cursor-pointer"
                  >
                    clear all
                  </button>
                </div>
                {history.map((h, i) => (
                  <div
                    key={h}
                    onClick={() => handleSelect(h)}
                    className={`w-full text-left px-3.5 py-2 text-xs flex items-center justify-between gap-2.5 transition-colors cursor-pointer group ${
                      i === selectedIndex ? "bg-slate-100 text-navy font-medium" : "text-slate-700 hover:bg-slate-50"
                    }`}
                  >
                    <div className="flex items-center gap-2 min-w-0 flex-1">
                      <ClockIcon className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                      <span className="truncate">{h}</span>
                    </div>
                    <button
                      type="button"
                      onClick={(e) => removeHistoryItem(e, h)}
                      title="Remove from history"
                      className="opacity-0 group-hover:opacity-100 p-0.5 text-slate-400 hover:text-red-500 rounded transition-opacity cursor-pointer"
                    >
                      <XIcon className="w-3 h-3" />
                    </button>
                  </div>
                ))}
              </div>
            )}


            {showSuggestions && (
              <div>
                <div className="px-3.5 py-1.5 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                  Suggestions
                </div>
                {suggestions.map((s, i) => (
                  <button
                    key={s}
                    type="button"
                    onClick={() => handleSelect(s)}
                    className={`w-full text-left px-3.5 py-2 text-xs flex items-center gap-2.5 transition-colors ${
                      i === selectedIndex ? "bg-accent-pale text-accent font-medium" : "text-slate-700 hover:bg-slate-50"
                    }`}
                  >
                    <SearchIcon className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                    <span className="truncate">{s}</span>
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    )
  }

  return (
    <div ref={containerRef} className="relative w-full">
      <form onSubmit={handleSubmit} className="w-full">
        <div className="flex items-center bg-white border-2 border-slate-200 rounded-2xl shadow-sm focus-within:border-accent focus-within:shadow-[0_0_0_4px_rgba(37,99,235,0.08)] transition-all duration-200">
          <SearchIcon className="ml-5 w-5 h-5 text-slate-400 flex-shrink-0" />
          <input
            className="flex-1 px-4 py-4 text-base bg-transparent outline-none text-slate-900 placeholder:text-slate-400 font-sans"
            value={localValue}
            onChange={(e) => {
              setLocalValue(e.target.value)
              setSelectedIndex(-1)
            }}
            onFocus={() => setIsFocused(true)}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            autoFocus={autoFocus}
          />
          {localValue && (
            <button
              type="button"
              onClick={() => setLocalValue("")}
              className="text-slate-400 hover:text-slate-600 mr-2 transition-colors"
            >
              <XIcon className="w-4 h-4" />
            </button>
          )}
          <button
            type="submit"
            className="m-2 px-7 py-3 bg-accent hover:bg-accent-dark text-white rounded-xl font-medium text-sm transition-colors flex-shrink-0 shadow-sm"
          >
            Search
          </button>
        </div>
        <div className="flex items-center justify-center gap-3 text-xs text-slate-400 mt-3">
          <span>
            Press{" "}
            <kbd className="px-1.5 py-0.5 font-mono text-xs bg-slate-100 border border-slate-300 rounded-md">
              Enter
            </kbd>{" "}
            to search
          </span>
          <span>·</span>
          <span>
            Use <code className="text-slate-600 font-mono">"quotes"</code> for exact phrase
          </span>
        </div>
      </form>

      {/* Dropdown for large search bar */}
      {(showHistory || showSuggestions) && (
        <div className="absolute left-0 right-0 top-full mt-2 bg-white border border-slate-200 rounded-2xl shadow-xl z-50 overflow-hidden py-2 animate-in fade-in slide-in-from-top-2 duration-150 text-left">
          {showHistory && (
            <div>
              <div className="px-5 py-2 flex items-center justify-between text-xs font-semibold text-slate-400 uppercase tracking-wider">
                <span>Recent Searches</span>
                <button
                  type="button"
                  onClick={clearHistory}
                  className="text-xs text-slate-400 hover:text-red-500 transition-colors lowercase font-normal cursor-pointer"
                >
                  Clear all
                </button>
              </div>
              {history.map((h, i) => (
                <div
                  key={h}
                  onClick={() => handleSelect(h)}
                  className={`w-full text-left px-5 py-2.5 text-sm flex items-center justify-between gap-3 transition-colors cursor-pointer group ${
                    i === selectedIndex ? "bg-slate-100 text-navy font-medium" : "text-slate-700 hover:bg-slate-50"
                  }`}
                >
                  <div className="flex items-center gap-3 min-w-0 flex-1">
                    <ClockIcon className="w-4 h-4 text-slate-400 flex-shrink-0" />
                    <span className="truncate">{h}</span>
                  </div>
                  <button
                    type="button"
                    onClick={(e) => removeHistoryItem(e, h)}
                    title="Remove from history"
                    className="opacity-0 group-hover:opacity-100 p-1 text-slate-400 hover:text-red-500 rounded transition-opacity cursor-pointer"
                  >
                    <XIcon className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
          )}


          {showSuggestions && (
            <div>
              <div className="px-5 py-2 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Search Suggestions
              </div>
              {suggestions.map((s, i) => (
                <button
                  key={s}
                  type="button"
                  onClick={() => handleSelect(s)}
                  className={`w-full text-left px-5 py-2.5 text-sm flex items-center gap-3 transition-colors ${
                    i === selectedIndex ? "bg-accent-pale text-accent font-medium" : "text-slate-700 hover:bg-slate-50"
                  }`}
                >
                  <SearchIcon className="w-4 h-4 text-slate-400 flex-shrink-0" />
                  <span className="truncate">{s}</span>
                </button>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
