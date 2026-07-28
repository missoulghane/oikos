import { useQuery } from '@tanstack/react-query';
import { getPropertyContacts } from '@/features/property-mngt/properties/api/getPropertyContacts';
import { queryKeys } from '@/shared/constants/queryKeys';

export function usePropertyContacts(propertyId: string) {
  return useQuery({
    queryKey: queryKeys.properties.contacts(propertyId),
    queryFn: () => getPropertyContacts(propertyId),
  });
}
