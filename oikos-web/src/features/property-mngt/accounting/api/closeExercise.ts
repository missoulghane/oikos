import { httpClient } from '@/shared/api/httpClient';
import type { ExerciseClosing } from '@/features/property-mngt/accounting/types/accounting.types';

/** Porte sur l'exercice ouvert de la copropriété - il n'y en a qu'un, d'où l'absence d'identifiant. */
export async function closeExercise(propertyId: string): Promise<ExerciseClosing> {
  const { data } = await httpClient.post<ExerciseClosing>(`/properties/${propertyId}/accounting/exercises/close`);
  return data;
}
