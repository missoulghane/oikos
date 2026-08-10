import { useState } from 'react';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useDeleteDocument } from '@/features/property-mngt/documents/hooks/useDeleteDocument';
import { useDownloadDocument } from '@/features/property-mngt/documents/hooks/useDownloadDocument';
import type { Document } from '@/features/property-mngt/documents/types/document.types';

interface DocumentListProps {
  documents: Document[];
  canWrite: boolean;
}

function formatFileSize(sizeBytes: number): string {
  if (sizeBytes < 1024) {
    return `${sizeBytes} o`;
  }
  if (sizeBytes < 1024 * 1024) {
    return `${(sizeBytes / 1024).toFixed(1)} Ko`;
  }
  return `${(sizeBytes / (1024 * 1024)).toFixed(1)} Mo`;
}

export function DocumentList({ documents, canWrite }: DocumentListProps) {
  return (
    <ul className="flex flex-col divide-y divide-gray-100">
      {documents.map((doc) => (
        <DocumentRow key={doc.id} doc={doc} canWrite={canWrite} />
      ))}
    </ul>
  );
}

function DocumentRow({ doc, canWrite }: { doc: Document; canWrite: boolean }) {
  const [isConfirmingDelete, setIsConfirmingDelete] = useState(false);
  const deleteDocument = useDeleteDocument(doc.ownerType, doc.ownerId);
  const downloadDocument = useDownloadDocument();

  return (
    <li className="flex flex-col gap-2 py-3">
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm font-medium text-gray-900">{doc.fileName}</p>
          <p className="text-sm text-gray-500">
            {formatFileSize(doc.sizeBytes)} · {new Date(doc.uploadedAt).toLocaleDateString('fr-FR')}
          </p>
        </div>
        {!isConfirmingDelete && (
          <div className="flex shrink-0 gap-2">
            <Button
              type="button"
              variant="secondary"
              isLoading={downloadDocument.isPending}
              onClick={() => downloadDocument.mutate({ documentId: doc.id, fileName: doc.fileName })}
            >
              Télécharger
            </Button>
            {canWrite && (
              <Button type="button" variant="secondary" onClick={() => setIsConfirmingDelete(true)}>
                Supprimer
              </Button>
            )}
          </div>
        )}
      </div>

      {isConfirmingDelete && (
        <div className="flex flex-col gap-2">
          <p className="text-sm text-gray-700">Supprimer ce document ? Cette action est irréversible.</p>
          {deleteDocument.isError && <Alert message={getErrorMessage(deleteDocument.error)} />}
          <div className="flex gap-2">
            <Button type="button" isLoading={deleteDocument.isPending} onClick={() => deleteDocument.mutate(doc.id)}>
              Confirmer la suppression
            </Button>
            <Button type="button" variant="secondary" onClick={() => setIsConfirmingDelete(false)}>
              Annuler
            </Button>
          </div>
        </div>
      )}
    </li>
  );
}
