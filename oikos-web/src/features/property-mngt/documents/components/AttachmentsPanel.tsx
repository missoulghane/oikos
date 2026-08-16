import { useState } from 'react';
import { useDocuments } from '@/features/property-mngt/documents/hooks/useDocuments';
import { UploadDocumentForm } from '@/features/property-mngt/documents/components/UploadDocumentForm';
import { DocumentList } from '@/features/property-mngt/documents/components/DocumentList';
import type { DocumentOwnerType } from '@/features/property-mngt/documents/types/document.types';
import { Alert } from '@/shared/components/Alert/Alert';
import { Loader } from '@/shared/components/Loader/Loader';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

interface AttachmentsPanelProps {
  ownerType: DocumentOwnerType;
  ownerId: string;
  canWrite: boolean;
  /** Shown instead of a card's worth of chrome when there is nothing yet. */
  emptyLabel?: string;
  /**
   * Render nothing at all when there is no attachment and nothing to upload.
   * For read-only lists repeated per row - a copropriétaire reading an agenda
   * of fifteen points does not need "aucune pièce jointe" fifteen times.
   */
  hideWhenEmpty?: boolean;
}

/**
 * Attachments of one thing, without the page around them: the upload field,
 * the list, and nothing else.
 *
 * <p>Extracted from PropertyDocumentsTab when a second and a third owner type
 * appeared (a general meeting, and every point of its agenda). A whole tab per
 * owner would not survive an agenda of fifteen points - the panel has to nest
 * inside a row.
 */
export function AttachmentsPanel({
  ownerType,
  ownerId,
  canWrite,
  emptyLabel,
  hideWhenEmpty = false,
}: AttachmentsPanelProps) {
  const [page, setPage] = useState(0);
  const documents = useDocuments(ownerType, ownerId, page);

  const items = documents.data?.content ?? [];

  if (hideWhenEmpty && !canWrite && items.length === 0) {
    return null;
  }

  return (
    <div className="flex flex-col gap-2">
      {documents.isLoading && <Loader label="Chargement des pièces jointes…" />}
      {documents.isError && <Alert message={getErrorMessage(documents.error)} />}

      {documents.data && items.length === 0 && !hideWhenEmpty && (
        <p className="text-sm text-gray-500 dark:text-gray-400">{emptyLabel ?? 'Aucune pièce jointe.'}</p>
      )}

      {items.length > 0 && (
        <>
          <DocumentList documents={items} canWrite={canWrite} />
          {documents.data && documents.data.totalPages > 1 && (
            <Pagination
              pageNumber={documents.data.pageNumber}
              totalPages={documents.data.totalPages}
              onPageChange={setPage}
            />
          )}
        </>
      )}

      {canWrite && <UploadDocumentForm ownerType={ownerType} ownerId={ownerId} />}
    </div>
  );
}
