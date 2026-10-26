import { httpClient } from '@/shared/api/httpClient';
import type { DocumentOwnerType, PagedDocuments } from '@/features/property-mngt/documents/types/document.types';

export interface ListDocumentsParams {
  ownerType: DocumentOwnerType;
  ownerId: string;
  page: number;
  size: number;
  /** Cherche dans le titre et le nom du fichier ; vide = tous les documents. */
  search?: string;
}

export async function listDocuments({ ownerType, ownerId, page, size, search }: ListDocumentsParams): Promise<PagedDocuments> {
  const { data } = await httpClient.get<PagedDocuments>('/documents', {
    params: { ownerType, ownerId, page, size, search: search || undefined },
  });
  return data;
}
