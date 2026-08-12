import { httpClient } from '@/shared/api/httpClient';
import type { Payment } from '@/features/property-mngt/installments/types/payment.types';

export async function getLatestPayments(propertyId: string, size: number): Promise<Payment[]> {
  const { data } = await httpClient.get<Payment[]>(`/properties/${propertyId}/payments/latest`, {
    params: { size },
  });
  return data;
}
