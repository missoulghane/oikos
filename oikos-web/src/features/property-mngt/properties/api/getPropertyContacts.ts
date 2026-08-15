import { httpClient } from '@/shared/api/httpClient';
import type {
  ContactListFilters,
  PagedPropertyContacts,
} from '@/features/property-mngt/properties/types/property.types';

export interface GetPropertyContactsParams extends ContactListFilters {
  propertyId: string;
  page: number;
  size: number;
}

export async function getPropertyContacts({
  propertyId,
  page,
  size,
  search,
  hasLinkedAccount,
  sortBy,
  sortDirection,
}: GetPropertyContactsParams): Promise<PagedPropertyContacts> {
  const { data } = await httpClient.get<PagedPropertyContacts>(`/properties/${propertyId}/contacts`, {
    params: { page, size, search, hasLinkedAccount, sortBy, sortDirection },
  });
  return data;
}
