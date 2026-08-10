import { httpClient } from '@/shared/api/httpClient';
import type {
  JournalEntryReference,
  RecordTreasuryTransferPayload,
} from '@/features/property-mngt/accounting/types/accounting.types';

export async function recordTreasuryTransfer(
  propertyId: string,
  payload: RecordTreasuryTransferPayload,
): Promise<JournalEntryReference> {
  const { data } = await httpClient.post<JournalEntryReference>(
    `/properties/${propertyId}/accounting/treasury-transfers`,
    payload,
  );
  return data;
}
