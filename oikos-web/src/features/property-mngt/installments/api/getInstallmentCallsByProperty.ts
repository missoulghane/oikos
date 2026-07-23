import { httpClient } from '@/shared/api/httpClient';
import type { PagedInstallmentCalls } from '@/features/property-mngt/installments/types/installmentCall.types';

export interface GetInstallmentCallsByPropertyParams {
  propertyId: string;
  page: number;
  size: number;
}

export async function getInstallmentCallsByProperty({
  propertyId,
  page,
  size,
}: GetInstallmentCallsByPropertyParams): Promise<PagedInstallmentCalls> {
  const { data } = await httpClient.get<PagedInstallmentCalls>(`/properties/${propertyId}/installment-calls`, {
    params: { page, size },
  });
  return data;
}
