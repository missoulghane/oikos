import { useId, useState, type ReactNode } from 'react';

interface FilterPanelSearch {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

interface FilterPanelProps {
  /** Number of fields differing from their defaults - see countActiveFilters. */
  activeCount: number;
  onClear: () => void;
  /** Omit on lists that have no meaningful free-text dimension. */
  search?: FilterPanelSearch;
  children: ReactNode;
}

/**
 * Toolbar above a list: a permanent search box, a "Filtres" toggle and a reset.
 *
 * The fields collapse behind the toggle at every width, not just on phones -
 * one consistent behaviour rather than a layout that rearranges itself at a
 * breakpoint, and the list gets the vertical space either way. What the toggle
 * must therefore carry on its own is *state*: it turns brand-coloured and shows
 * a count as soon as a filter is applied, so a filtered list can never look
 * like an unfiltered one just because the fields are folded away.
 */
export function FilterPanel({ activeCount, onClear, search, children }: FilterPanelProps) {
  const [isOpen, setIsOpen] = useState(false);
  const panelId = useId();
  const hasActiveFilters = activeCount > 0;

  return (
    <div className="flex flex-col gap-3">
      {/* One row at every width: the search box flexes (min-w-0 lets it shrink
          below its content) and the two buttons drop their labels on narrow
          screens, keeping their icon and their 44px target. */}
      <div className="flex items-center gap-2">
        {search && (
          <div className="relative min-w-0 flex-1">
            <span className="pointer-events-none absolute inset-y-0 left-3 flex items-center text-gray-400">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="1.5" />
                <path d="m20 20-3.5-3.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
              </svg>
            </span>
            <input
              type="search"
              value={search.value}
              onChange={(e) => search.onChange(e.target.value)}
              placeholder={search.placeholder ?? 'Rechercher…'}
              aria-label="Rechercher"
              className="h-11 w-full rounded-lg bg-transparent py-2 pl-10 pr-3 text-sm text-gray-800 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:outline-hidden focus:ring-brand-500 dark:text-white/90 dark:ring-gray-700"
            />
          </div>
        )}

        {/* aria-label rather than the visible text: that text is display:none
            below `sm`, where it would stop being announced at all. */}
        <button
          type="button"
          onClick={() => setIsOpen((value) => !value)}
          aria-expanded={isOpen}
          aria-controls={panelId}
          aria-label="Filtres"
          className={`flex h-11 min-w-11 shrink-0 items-center justify-center gap-2 rounded-lg px-3 text-sm font-medium ring-1 ring-inset ${
            hasActiveFilters
              ? 'bg-brand-50 text-brand-600 ring-brand-500 dark:bg-brand-500/[0.12] dark:text-brand-400'
              : 'text-gray-700 ring-gray-300 hover:bg-gray-50 dark:text-gray-300 dark:ring-gray-700 dark:hover:bg-white/[0.03]'
          }`}
        >
          <svg
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            className="shrink-0"
          >
            <path
              d="M3 5h18l-7 8v6l-4 2v-8L3 5Z"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
          <span className="hidden sm:inline">Filtres</span>
          {hasActiveFilters && (
            <span className="flex h-5 min-w-5 shrink-0 items-center justify-center rounded-full bg-brand-500 px-1.5 text-xs font-semibold text-white">
              {activeCount}
            </span>
          )}
        </button>

        {hasActiveFilters && (
          <button
            type="button"
            onClick={onClear}
            aria-label="Réinitialiser les filtres"
            title="Réinitialiser les filtres"
            className="flex h-11 min-w-11 shrink-0 items-center justify-center gap-1.5 rounded-lg px-3 text-sm font-medium text-gray-500 hover:bg-gray-50 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/[0.03] dark:hover:text-gray-300"
          >
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              className="shrink-0"
            >
              <path
                d="M6 6L18 18M18 6L6 18"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
            <span className="hidden sm:inline">Réinitialiser</span>
          </button>
        )}
      </div>

      {/* Kept mounted while folded so field values survive a toggle. */}
      <div id={panelId} className={isOpen ? 'block' : 'hidden'}>
        {children}
      </div>
    </div>
  );
}
