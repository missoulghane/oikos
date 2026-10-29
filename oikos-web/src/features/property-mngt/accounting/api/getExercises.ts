import { httpClient } from '@/shared/api/httpClient';
import type { AccountingExercise } from '@/features/property-mngt/accounting/types/accounting.types';

/** Tous les exercices de la copropriété, du plus ancien au plus récent - une liste vide si aucun n'a été ouvert. */
export async function getExercises(propertyId: string): Promise<AccountingExercise[]> {
  const { data } = await httpClient.get<AccountingExercise[]>(`/properties/${propertyId}/accounting/exercises`);
  return data;
}
