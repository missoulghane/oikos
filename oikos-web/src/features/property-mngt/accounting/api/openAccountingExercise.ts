import { httpClient } from '@/shared/api/httpClient';
import type { OpenExercisePayload } from '@/features/property-mngt/accounting/types/accounting.types';

export async function openAccountingExercise(propertyId: string, payload: OpenExercisePayload): Promise<void> {
  await httpClient.post(`/properties/${propertyId}/accounting/exercises`, payload);
}
