import { useMutation, useQueryClient } from '@tanstack/react-query';
import { uploadDocument } from '@/features/property-mngt/documents/api/uploadDocument';
import type { DocumentOwnerType } from '@/features/property-mngt/documents/types/document.types';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export interface UploadDocumentsParams {
  ownerType: DocumentOwnerType;
  ownerId: string;
  files: File[];
}

/**
 * Several files for an owner known only at call time - the counterpart of
 * useUploadDocument for a creation flow, where the id comes back from the
 * create call that precedes it.
 *
 * <p>One request per file, in sequence, like UploadDocumentForm: the API takes
 * a single file, and one refused file must not cost the others. Never rejects -
 * it resolves with one "name : reason" line per file that did not go through,
 * which the caller shows once the owner exists.
 */
export function useUploadDocuments() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ ownerType, ownerId, files }: UploadDocumentsParams): Promise<string[]> => {
      const failures: string[] = [];
      for (const file of files) {
        try {
          await uploadDocument({ ownerType, ownerId, file });
        } catch (error) {
          failures.push(`${file.name} : ${getErrorMessage(error)}`);
        }
      }
      return failures;
    },
    onSuccess: (_failures, { ownerType, ownerId }) => {
      queryClient.invalidateQueries({ queryKey: ['documents', ownerType, ownerId] });
    },
  });
}
