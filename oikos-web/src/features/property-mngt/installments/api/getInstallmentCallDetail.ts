import { httpClient } from '@/shared/api/httpClient';
import type { InstallmentCallDetail } from '@/features/property-mngt/installments/types/installmentCall.types';

export async function getInstallmentCallDetail(id: string): Promise<InstallmentCallDetail> {
  const { data } = await httpClient.get<InstallmentCallDetail>(`/installment-calls/${id}`);
  return data;
}
