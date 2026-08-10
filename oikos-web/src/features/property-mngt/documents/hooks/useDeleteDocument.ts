import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteDocument } from '@/features/property-mngt/documents/api/deleteDocument';
import type { DocumentOwnerType } from '@/features/property-mngt/documents/types/document.types';

export function useDeleteDocument(ownerType: DocumentOwnerType, ownerId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteDocument,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documents', ownerType, ownerId] });
    },
  });
}
