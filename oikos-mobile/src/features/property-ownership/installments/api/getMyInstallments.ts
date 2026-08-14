import { httpClient } from '@/shared/api/httpClient';
import type { OwnedInstallment } from '@/features/property-ownership/installments/types/installment.types';

export async function getMyInstallments(): Promise<OwnedInstallment[]> {
  const { data } = await httpClient.get<OwnedInstallment[]>('/users/me/installments');
  return data;
}
