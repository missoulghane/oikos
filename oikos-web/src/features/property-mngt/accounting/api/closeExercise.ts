import { httpClient } from '@/shared/api/httpClient';
import type { ExerciseClosing } from '@/features/property-mngt/accounting/types/accounting.types';

/** Deux exercices pouvant être ouverts à la fois, celui qu'on clôture est désigné - l'API n'accepte que le plus ancien. */
export async function closeExercise(propertyId: string, exerciseId: string): Promise<ExerciseClosing> {
  const { data } = await httpClient.post<ExerciseClosing>(
    `/properties/${propertyId}/accounting/exercises/${exerciseId}/close`,
  );
  return data;
}
