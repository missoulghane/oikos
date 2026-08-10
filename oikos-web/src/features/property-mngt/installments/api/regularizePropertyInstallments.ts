import { httpClient } from '@/shared/api/httpClient';
import type { RegularizePropertyInstallmentsResult } from '@/features/property-mngt/installments/types/payment.types';

export async function regularizePropertyInstallments(
  propertyId: string,
): Promise<RegularizePropertyInstallmentsResult> {
  const { data } = await httpClient.post<RegularizePropertyInstallmentsResult>(
    `/properties/${propertyId}/installments/regularization`,
  );
  return data;
}
