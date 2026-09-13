import { useState, useEffect, type FormEvent, type KeyboardEvent } from "react"
import { SearchIcon } from "../icons"

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
  placeholder = "Search documents, topics, or keywords...",
  size = "large",
  autoFocus = false,
}: SearchBarProps) {
  const [localValue, setLocalValue] = useState(value)

  useEffect(() => {
    setLocalValue(value)
  }, [value])

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (localValue.trim()) onSearch(localValue.trim())
  }

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      e.preventDefault()
      if (localValue.trim()) onSearch(localValue.trim())
    }
  }

  if (size === "compact") {
    return (
      <form onSubmit={handleSubmit} className="flex items-center gap-2 w-full">
        <div className="flex-1 flex items-center bg-white border border-slate-300 rounded-lg focus-within:border-accent focus-within:ring-2 focus-within:ring-accent/20 transition-all duration-150">
          <SearchIcon className="ml-3 w-4 h-4 text-slate-400 flex-shrink-0" />
          <input
            className="flex-1 px-3 py-2 text-sm bg-transparent outline-none text-slate-900 placeholder:text-slate-400 font-sans"
            value={localValue}
            onChange={(e) => setLocalValue(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            autoFocus={autoFocus}
          />
          {localValue && (
            <button
              type="button"
              onClick={() => setLocalValue("")}
              className="mr-2 text-slate-400 hover:text-slate-600 transition-colors"
            >
              <svg viewBox="0 0 16 16" fill="none" className="w-3.5 h-3.5">
                <path d="M12 4L4 12M4 4l8 8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
              </svg>
            </button>
          )}
        </div>
        <button
          type="submit"
          className="px-4 py-2 bg-accent hover:bg-accent-dark text-white rounded-lg text-sm font-medium transition-colors flex-shrink-0"
        >
          Search
        </button>
      </form>
    )
  }

  return (
    <form onSubmit={handleSubmit} className="w-full">
      <div className="flex items-center bg-white border-2 border-slate-200 rounded-2xl shadow-sm focus-within:border-accent focus-within:shadow-[0_0_0_4px_rgba(37,99,235,0.08)] transition-all duration-200">
        <SearchIcon className="ml-5 w-5 h-5 text-slate-400 flex-shrink-0" />
        <input
          className="flex-1 px-4 py-4 text-base bg-transparent outline-none text-slate-900 placeholder:text-slate-400 font-sans"
          value={localValue}
          onChange={(e) => setLocalValue(e.target.value)}
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
            <svg viewBox="0 0 16 16" fill="none" className="w-4 h-4">
              <path d="M12 4L4 12M4 4l8 8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
            </svg>
          </button>
        )}
        <button
          type="submit"
          className="m-2 px-7 py-3 bg-accent hover:bg-accent-dark text-white rounded-xl font-medium text-sm transition-colors flex-shrink-0"
        >
          Search
        </button>
      </div>
      <p className="text-center text-xs text-slate-400 mt-3">
        Press{" "}
        <kbd className="px-1.5 py-0.5 font-mono text-xs bg-slate-100 border border-slate-300 rounded-md">
          Enter
        </kbd>{" "}
        to search
      </p>
    </form>
  )
}
