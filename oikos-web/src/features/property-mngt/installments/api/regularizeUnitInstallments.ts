import { httpClient } from '@/shared/api/httpClient';
import type { RegularizeUnitInstallmentsResult } from '@/features/property-mngt/installments/types/payment.types';

export async function regularizeUnitInstallments(
  propertyId: string,
  unitId: string,
): Promise<RegularizeUnitInstallmentsResult> {
  const { data } = await httpClient.post<RegularizeUnitInstallmentsResult>(
    `/properties/${propertyId}/units/${unitId}/installments/regularization`,
  );
  return data;
}
