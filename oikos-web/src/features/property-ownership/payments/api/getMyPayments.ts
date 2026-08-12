import { httpClient } from '@/shared/api/httpClient';
import type { Payment } from '@/features/property-mngt/installments/types/payment.types';

export async function getMyPayments(): Promise<Payment[]> {
  const { data } = await httpClient.get<Payment[]>('/users/me/payments');
  return data;
}
