type LogoProps = {
  onClick: () => void
  size?: "default" | "large"
}

export default function Logo({ onClick, size = "default" }: LogoProps) {
  const isLarge = size === "large"
  return (
    <button
      onClick={onClick}
      className="flex items-center gap-2.5 group flex-shrink-0"
    >
      <div className={`${isLarge ? "w-10 h-10" : "w-8 h-8"} bg-navy rounded-lg flex items-center justify-center shadow-sm group-hover:bg-navy-mid transition-colors`}>
        <svg
          viewBox="0 0 20 20"
          fill="none"
          className={`${isLarge ? "w-5 h-5" : "w-4 h-4"} text-white`}
        >
          <circle cx="8.5" cy="8.5" r="5.5" stroke="currentColor" strokeWidth="1.75" />
          <path d="M13 13L17 17" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" />
        </svg>
      </div>
      <div className="text-left">
        <span className={`font-semibold text-navy tracking-tight ${isLarge ? "text-base" : "text-sm"} block leading-tight`}>
          Mini Search Engine
        </span>
        {isLarge && (
          <span className="text-xs text-slate-500 font-mono tracking-wide">v1.0 · University Project</span>
        )}
      </div>
    </button>
  )
}
