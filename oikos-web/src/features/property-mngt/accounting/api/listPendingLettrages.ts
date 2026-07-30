import { httpClient } from '@/shared/api/httpClient';
import type { PendingLettrage } from '@/features/property-mngt/accounting/types/accounting.types';

export async function listPendingLettrages(propertyId: string): Promise<PendingLettrage[]> {
  const { data } = await httpClient.get<PendingLettrage[]>(`/properties/${propertyId}/accounting/lettrage/pending`);
  return data;
}
