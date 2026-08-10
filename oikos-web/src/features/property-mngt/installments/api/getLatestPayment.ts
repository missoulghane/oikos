import { httpClient } from '@/shared/api/httpClient';
import type { Payment } from '@/features/property-mngt/installments/types/payment.types';

export async function getLatestPayment(propertyId: string): Promise<Payment | null> {
  const response = await httpClient.get<Payment>(`/properties/${propertyId}/payments/latest`);
  return response.status === 204 ? null : response.data;
}
