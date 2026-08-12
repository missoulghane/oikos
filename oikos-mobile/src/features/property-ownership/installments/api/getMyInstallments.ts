import { httpClient } from '@/shared/api/httpClient';
import type { Installment } from '@/features/property-ownership/installments/types/installment.types';

export async function getMyInstallments(): Promise<Installment[]> {
  const { data } = await httpClient.get<Installment[]>('/users/me/installments');
  return data;
}
