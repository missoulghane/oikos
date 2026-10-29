import { httpClient } from '@/shared/api/httpClient';
import type { RegularizeUnitInstallmentsResult } from '@/features/property-mngt/installments/types/payment.types';

/** Impute l'avance du lot sur ses échéances exigibles, à la date du jour. */
export async function regularizeUnitAdvance(
  propertyId: string,
  unitId: string,
): Promise<RegularizeUnitInstallmentsResult> {
  const { data } = await httpClient.post<RegularizeUnitInstallmentsResult>(
    `/properties/${propertyId}/units/${unitId}/regularization`,
  );
  return data;
}
