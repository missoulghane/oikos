import { httpClient } from '@/shared/api/httpClient';
import type {
  RecordOwnerPaymentPayload,
  RecordOwnerPaymentResult,
} from '@/features/property-mngt/installments/types/payment.types';

export async function recordOwnerPayment(
  propertyId: string,
  unitId: string,
  payload: RecordOwnerPaymentPayload,
): Promise<RecordOwnerPaymentResult> {
  const { data } = await httpClient.post<RecordOwnerPaymentResult>(
    `/properties/${propertyId}/units/${unitId}/payments`,
    payload,
  );
  return data;
}
