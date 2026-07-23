import { httpClient } from '@/shared/api/httpClient';
import type { Installment } from '@/features/property-mngt/installments/types/installment.types';

export async function getUnitInstallments(unitId: string): Promise<Installment[]> {
  const { data } = await httpClient.get<Installment[]>(`/units/${unitId}/installments`);
  return data;
}
