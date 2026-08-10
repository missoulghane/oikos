import { httpClient } from '@/shared/api/httpClient';
import type { PagedPropertyContacts } from '@/features/property-mngt/properties/types/property.types';

export interface GetPropertyContactsParams {
  propertyId: string;
  page: number;
  size: number;
  search?: string;
  hasLinkedAccount?: boolean;
}

export async function getPropertyContacts({
  propertyId,
  page,
  size,
  search,
  hasLinkedAccount,
}: GetPropertyContactsParams): Promise<PagedPropertyContacts> {
  const { data } = await httpClient.get<PagedPropertyContacts>(`/properties/${propertyId}/contacts`, {
    params: { page, size, search, hasLinkedAccount },
  });
  return data;
}
