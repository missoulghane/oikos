import { httpClient } from '@/shared/api/httpClient';
import type {
  GenerateInstallmentCallPayload,
  GenerateInstallmentCallResult,
} from '@/features/property-mngt/installments/types/installmentCall.types';

export async function generateInstallmentCall(
  propertyId: string,
  payload: GenerateInstallmentCallPayload,
): Promise<GenerateInstallmentCallResult> {
  const { data } = await httpClient.post<GenerateInstallmentCallResult>(
    `/properties/${propertyId}/installment-calls`,
    payload,
  );
  return data;
}
