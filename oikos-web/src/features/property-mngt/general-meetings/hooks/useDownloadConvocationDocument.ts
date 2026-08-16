import { useMutation } from '@tanstack/react-query';
import { downloadConvocationDocument } from '@/features/property-mngt/general-meetings/api/downloadConvocationDocument';

export function useDownloadConvocationDocument() {
  return useMutation({
    mutationFn: ({ convocationId, fileName }: { convocationId: string; fileName: string }) =>
      downloadConvocationDocument(convocationId, fileName),
  });
}
