import { httpClient } from '@/shared/api/httpClient';

export async function downloadDocument(documentId: string, fileName: string): Promise<void> {
  const { data } = await httpClient.get(`/documents/${documentId}/content`, { responseType: 'blob' });

  const url = URL.createObjectURL(data as Blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}
