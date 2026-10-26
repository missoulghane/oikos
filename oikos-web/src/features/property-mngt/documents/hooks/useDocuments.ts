import { useQuery } from '@tanstack/react-query';
import { listDocuments } from '@/features/property-mngt/documents/api/listDocuments';
import { queryKeys } from '@/shared/constants/queryKeys';
import type { DocumentOwnerType } from '@/features/property-mngt/documents/types/document.types';

export function useDocuments(ownerType: DocumentOwnerType, ownerId: string, page: number, size = 20, search = '') {
  return useQuery({
    queryKey: queryKeys.documents.list(ownerType, ownerId, page, size, search),
    queryFn: () => listDocuments({ ownerType, ownerId, page, size, search }),
    placeholderData: (previousData) => previousData,
  });
}
