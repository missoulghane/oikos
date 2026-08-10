import { httpClient } from '@/shared/api/httpClient';
import type { Payment } from '@/features/property-mngt/installments/types/payment.types';

export async function getUnitPayments(unitId: string): Promise<Payment[]> {
  const { data } = await httpClient.get<Payment[]>(`/units/${unitId}/payments`);
  return data;
}
