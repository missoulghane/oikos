import { useQuery } from '@tanstack/react-query';
import { getPropertyContacts } from '@/features/property-mngt/properties/api/getPropertyContacts';
import { queryKeys } from '@/shared/constants/queryKeys';

const DEFAULT_PAGE_SIZE = 5;

export function usePropertyContacts(
  propertyId: string,
  page: number,
  search?: string,
  hasLinkedAccount?: boolean,
  size: number = DEFAULT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: queryKeys.properties.contacts(propertyId, page, size, search, hasLinkedAccount),
    queryFn: () => getPropertyContacts({ propertyId, page, size, search, hasLinkedAccount }),
    placeholderData: (previousData) => previousData,
  });
}
