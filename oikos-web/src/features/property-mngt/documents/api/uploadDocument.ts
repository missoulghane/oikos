import { httpClient } from '@/shared/api/httpClient';
import type { Document, DocumentOwnerType } from '@/features/property-mngt/documents/types/document.types';

export interface UploadDocumentParams {
  ownerType: DocumentOwnerType;
  ownerId: string;
  file: File;
  /** Facultatif : sans titre, l'API reprend le nom du fichier sans son extension. */
  title?: string;
  /** Pourcentage envoyé (0-100), pour afficher la progression d'un gros fichier. */
  onProgress?: (percent: number) => void;
}

export async function uploadDocument({ ownerType, ownerId, file, title, onProgress }: UploadDocumentParams): Promise<Document> {
  const formData = new FormData();
  formData.append('ownerType', ownerType);
  formData.append('ownerId', ownerId);
  formData.append('file', file);
  if (title && title.trim() !== '') {
    formData.append('title', title.trim());
  }

  const { data } = await httpClient.post<Document>('/documents', formData, {
    onUploadProgress: onProgress
      ? (event) => onProgress(event.total ? (event.loaded / event.total) * 100 : 0)
      : undefined,
  });
  return data;
}
