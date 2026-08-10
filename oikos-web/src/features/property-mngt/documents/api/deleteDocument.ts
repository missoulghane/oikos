import { httpClient } from '@/shared/api/httpClient';

export async function deleteDocument(documentId: string): Promise<void> {
  await httpClient.delete(`/documents/${documentId}`);
}
