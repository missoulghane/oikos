import { httpClient } from '@/shared/api/httpClient';
import type {
  JournalEntryReference,
  RecordBankChargePayload,
} from '@/features/property-mngt/accounting/types/accounting.types';

export async function recordBankCharge(
  propertyId: string,
  payload: RecordBankChargePayload,
): Promise<JournalEntryReference> {
  const { data } = await httpClient.post<JournalEntryReference>(
    `/properties/${propertyId}/accounting/bank-charges`,
    payload,
  );
  return data;
}
