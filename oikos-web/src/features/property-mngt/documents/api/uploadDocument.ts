import { httpClient } from '@/shared/api/httpClient';
import type { Document, DocumentOwnerType } from '@/features/property-mngt/documents/types/document.types';

export interface UploadDocumentParams {
  ownerType: DocumentOwnerType;
  ownerId: string;
  file: File;
}

export async function uploadDocument({ ownerType, ownerId, file }: UploadDocumentParams): Promise<Document> {
  const formData = new FormData();
  formData.append('ownerType', ownerType);
  formData.append('ownerId', ownerId);
  formData.append('file', file);

  const { data } = await httpClient.post<Document>('/documents', formData);
  return data;
}
