import { httpClient } from '@/shared/api/httpClient';
import type {
  InstallmentListFilters,
  PagedInstallments,
} from '@/features/property-mngt/installments/types/installment.types';

export interface GetPropertyInstallmentsParams extends InstallmentListFilters {
  propertyId: string;
  page: number;
  size: number;
}

export async function getPropertyInstallments({
  propertyId,
  page,
  size,
  status,
  dueDateFrom,
  dueDateTo,
  installmentCallId,
  sortBy,
  sortDirection,
}: GetPropertyInstallmentsParams): Promise<PagedInstallments> {
  const { data } = await httpClient.get<PagedInstallments>(`/properties/${propertyId}/installments`, {
    params: { page, size, status, dueDateFrom, dueDateTo, installmentCallId, sortBy, sortDirection },
    // Spring binds repeated `status=A&status=B`, not axios's default `status[]=A&status[]=B`.
    paramsSerializer: { indexes: null },
  });
  return data;
}
