interface StepperProps {
  label: string;
  value: number;
  onChange: (value: number) => void;
  min?: number;
  max?: number;
  /** Rend le libellé accessible sans l'afficher (listes où l'intitulé est déjà à côté). */
  hideLabel?: boolean;
}

/**
 * Compteur − / + avec saisie clavier possible. Les deux boutons sont désactivés
 * aux bornes plutôt que de laisser passer une valeur qu'on corrigerait ensuite
 * en silence : l'utilisateur voit pourquoi il ne peut pas descendre plus bas.
 */
export function Stepper({ label, value, onChange, min = 0, max = 999, hideLabel = false }: StepperProps) {
  function clamp(next: number): number {
    return Math.min(max, Math.max(min, next));
  }

  return (
    <div className="flex items-center justify-between gap-3">
      <span className={hideLabel ? 'sr-only' : 'text-sm text-gray-700 dark:text-gray-300'}>{label}</span>
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={() => onChange(clamp(value - 1))}
          disabled={value <= min}
          aria-label={`Diminuer : ${label}`}
          className="flex size-11 items-center justify-center rounded-lg border border-gray-300 text-lg font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]"
        >
          −
        </button>
        <input
          type="number"
          inputMode="numeric"
          aria-label={label}
          value={value}
          min={min}
          max={max}
          onChange={(event) => {
            const parsed = Number.parseInt(event.target.value, 10);
            onChange(Number.isNaN(parsed) ? min : clamp(parsed));
          }}
          className="min-h-11 w-16 rounded-lg border border-gray-300 bg-transparent px-2 text-center text-base text-gray-800 focus:outline-none focus:ring-3 focus:ring-brand-500/20 dark:border-gray-700 dark:text-white/90"
        />
        <button
          type="button"
          onClick={() => onChange(clamp(value + 1))}
          disabled={value >= max}
          aria-label={`Augmenter : ${label}`}
          className="flex size-11 items-center justify-center rounded-lg border border-gray-300 text-lg font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]"
        >
          +
        </button>
      </div>
    </div>
  );
}
