import { useMutation, useQueryClient } from '@tanstack/react-query';
import { uploadDocument } from '@/features/property-mngt/documents/api/uploadDocument';
import type { DocumentOwnerType } from '@/features/property-mngt/documents/types/document.types';

export function useUploadDocument(ownerType: DocumentOwnerType, ownerId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (file: File) => uploadDocument({ ownerType, ownerId, file }),
    onSuccess: () => {
      // Prefix match: covers every cached page of this owner's document list.
      queryClient.invalidateQueries({ queryKey: ['documents', ownerType, ownerId] });
    },
  });
}
