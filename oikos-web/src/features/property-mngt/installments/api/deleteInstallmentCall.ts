import { httpClient } from '@/shared/api/httpClient';

export async function deleteInstallmentCall(installmentCallId: string): Promise<void> {
  await httpClient.delete(`/installment-calls/${installmentCallId}`);
}
