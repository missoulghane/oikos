interface NotYetDueToggleProps {
  checked: boolean;
  /**
   * Rows currently hidden by this toggle. Omitted by the syndic list, where the
   * rule is applied server-side on a paginated query: counting the hidden rows
   * would cost a second round trip for a badge.
   */
  hiddenCount?: number;
  onChange: (checked: boolean) => void;
}

/**
 * Shows or hides the echeances not yet fallen due, which the lists leave out by
 * default. Sits in the filter panel as one more field, so it reads as part of
 * the same set of criteria.
 *
 * Two things earn their place because the control is folded away with the rest
 * of the panel: the count of what is currently hidden, and the "Masquées /
 * Affichées" wording. A neutral label would leave someone hunting for an
 * echeance dated next month with nothing telling them where it went.
 *
 * The warning palette rather than brand, on purpose: what this brings back is
 * precisely what the totals do not count.
 */
export function NotYetDueToggle({ checked, hiddenCount = 0, onChange }: NotYetDueToggleProps) {
  return (
    // Same label/control stack as Select, so the cell shares the row's baseline
    // instead of floating against taller neighbours.
    <div className="flex flex-col gap-1">
      <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Échéances à venir</span>
      <button
        type="button"
        aria-pressed={checked}
        aria-label="Échéances à venir"
        onClick={() => onChange(!checked)}
        title={
          checked
            ? 'Masquer les échéances dont la date n’est pas encore atteinte'
            : 'Afficher les échéances dont la date n’est pas encore atteinte'
        }
        className={`flex min-h-11 items-center gap-2 rounded-lg px-3 text-sm font-medium transition ${
          checked
            ? 'border border-warning-500 bg-warning-50 text-warning-600 dark:bg-warning-500/15 dark:text-warning-400'
            : 'border border-dashed border-gray-300 text-gray-500 hover:bg-gray-50 hover:text-gray-700 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-white/[0.03]'
        }`}
      >
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          className="shrink-0"
          aria-hidden="true"
        >
          <circle cx="12" cy="12" r="8.25" stroke="currentColor" strokeWidth="1.5" />
          <path d="M12 7.5V12l3 1.75" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        </svg>
        <span className="truncate">{checked ? 'Affichées' : 'Masquées'}</span>
        {/* Only meaningful while they are hidden; once shown they are on screen. */}
        {!checked && hiddenCount > 0 && (
          <span className="ml-auto flex h-5 min-w-5 items-center justify-center rounded-full bg-gray-100 px-1.5 text-xs font-semibold text-gray-600 dark:bg-white/[0.06] dark:text-gray-300">
            {hiddenCount}
          </span>
        )}
      </button>
    </div>
  );
}
