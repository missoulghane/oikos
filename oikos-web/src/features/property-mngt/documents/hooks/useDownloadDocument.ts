import { useMutation } from '@tanstack/react-query';
import { downloadDocument } from '@/features/property-mngt/documents/api/downloadDocument';

export function useDownloadDocument() {
  return useMutation({
    mutationFn: ({ documentId, fileName }: { documentId: string; fileName: string }) =>
      downloadDocument(documentId, fileName),
  });
}
