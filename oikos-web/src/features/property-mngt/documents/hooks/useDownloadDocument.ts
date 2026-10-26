import { useMutation } from '@tanstack/react-query';
import { downloadDocument } from '@/features/property-mngt/documents/api/downloadDocument';

export function useDownloadDocument() {
  return useMutation({
    mutationFn: ({
      documentId,
      fileName,
      onProgress,
    }: {
      documentId: string;
      fileName: string;
      onProgress?: (percent: number) => void;
    }) => downloadDocument(documentId, fileName, onProgress),
  });
}
