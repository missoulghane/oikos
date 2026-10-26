interface ProgressBarProps {
  /** 0 à 100. Absent quand la taille totale est inconnue : la barre défile sans prétendre à un pourcentage. */
  value?: number;
  /** Ce qui est en cours (« Envoi de budget.pdf »), lu à voix haute avec la progression. */
  label: string;
}

/**
 * Progression d'un transfert qui prend du temps (envoi ou téléchargement d'un
 * fichier) : là où un simple « chargement… » laisse se demander si quelque
 * chose se passe encore, la barre dit ce qui avance et de combien.
 */
export function ProgressBar({ value, label }: ProgressBarProps) {
  const percent = value === undefined ? undefined : Math.min(100, Math.max(0, Math.round(value)));

  return (
    <div className="flex flex-col gap-1">
      <div className="flex items-center justify-between gap-2 text-sm text-gray-600 dark:text-gray-400">
        <span className="truncate">{label}</span>
        {percent !== undefined && <span className="shrink-0 tabular-nums">{percent} %</span>}
      </div>
      <div
        role="progressbar"
        aria-label={label}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={percent}
        className="h-2 w-full overflow-hidden rounded-full bg-gray-100 dark:bg-white/[0.06]"
      >
        <div
          className={`h-full rounded-full bg-brand-500 transition-[width] duration-200 ${
            percent === undefined ? 'w-1/3 animate-pulse' : ''
          }`}
          style={percent === undefined ? undefined : { width: `${percent}%` }}
        />
      </div>
    </div>
  );
}
