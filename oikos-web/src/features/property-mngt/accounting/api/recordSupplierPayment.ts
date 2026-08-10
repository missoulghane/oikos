import { httpClient } from '@/shared/api/httpClient';
import type {
  Expense,
  RecordSupplierPaymentPayload,
} from '@/features/property-mngt/accounting/types/accounting.types';

export async function recordSupplierPayment(
  propertyId: string,
  payload: RecordSupplierPaymentPayload,
): Promise<Expense> {
  const { data } = await httpClient.post<Expense>(
    `/properties/${propertyId}/accounting/supplier-payments`,
    payload,
  );
  return data;
}
