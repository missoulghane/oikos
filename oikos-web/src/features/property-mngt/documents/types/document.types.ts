import type { Paged } from '@/shared/types/pagination.types';

export const DOCUMENT_OWNER_TYPES = ['PROPERTY', 'UNIT'] as const;
export type DocumentOwnerType = (typeof DOCUMENT_OWNER_TYPES)[number];

export interface Document {
  id: string;
  ownerType: DocumentOwnerType;
  ownerId: string;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  uploadedBy: string;
  uploadedAt: string;
}

export type PagedDocuments = Paged<Document>;
