import type { AccountingExercise } from '@/features/property-mngt/accounting/types/accounting.types';
import type { OpenExerciseFormValues } from '@/features/property-mngt/accounting/schemas/openExerciseSchema';

/** Au plus deux exercices ouverts à la fois - même plafond que l'API (OpenAccountingExerciseService). */
export const MAX_OPEN_EXERCISES = 2;

function toIsoDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}

/**
 * Ce que l'API attend du prochain exercice : il commence le lendemain de la fin
 * du dernier et dure aussi un an. Les dates sont manipulées en UTC, sans heure
 * locale : une date ISO lue comme minuit local glisse d'un jour selon le fuseau.
 */
export function nextExerciseDefaults(last: AccountingExercise): OpenExerciseFormValues {
  const start = new Date(`${last.endDate}T00:00:00Z`);
  start.setUTCDate(start.getUTCDate() + 1);
  const end = new Date(start);
  end.setUTCFullYear(end.getUTCFullYear() + 1);
  end.setUTCDate(end.getUTCDate() - 1);

  const startYear = start.getUTCFullYear();
  const endYear = end.getUTCFullYear();
  return {
    label: startYear === endYear ? `Exercice ${startYear}` : `Exercice ${startYear}-${endYear}`,
    startDate: toIsoDate(start),
    endDate: toIsoDate(end),
    comment: '',
  };
}

/** Le plus ancien des exercices ouverts : le seul que l'API accepte de clôturer. */
export function oldestOpenExercise(exercises: AccountingExercise[]): AccountingExercise | undefined {
  return exercises.find((exercise) => exercise.status === 'OPEN');
}
