import { httpClient } from '@/shared/api/httpClient';
import type { InstallmentCollectionSummary } from '@/features/property-mngt/installments/types/installment.types';

/**
 * What is left to collect: installments unpaid and already due, and their
 * total. An aggregate endpoint rather than a page the client adds up - the
 * figure covers the whole copropriété, not the first twenty rows of it.
 */
export async function getInstallmentCollectionSummary(propertyId: string): Promise<InstallmentCollectionSummary> {
  const { data } = await httpClient.get<InstallmentCollectionSummary>(
    `/properties/${propertyId}/installments/collection-summary`,
  );
  return data;
}
