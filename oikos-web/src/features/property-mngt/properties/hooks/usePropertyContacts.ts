import { useQuery } from '@tanstack/react-query';
import { getPropertyContacts } from '@/features/property-mngt/properties/api/getPropertyContacts';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { ContactListFilters } from '@/features/property-mngt/properties/types/property.types';

const DEFAULT_PAGE_SIZE = 5;

export function usePropertyContacts(
  propertyId: string,
  page: number,
  filters: ContactListFilters,
  size: number = DEFAULT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: queryKeys.properties.contacts(propertyId, page, size, filters),
    queryFn: () => getPropertyContacts({ propertyId, page, size, ...filters }),
    placeholderData: (previousData) => previousData,
  });
}
