import { useState } from 'react';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { ProgressBar } from '@/shared/components/ProgressBar/ProgressBar';
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
    <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800">
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
  const [downloadPercent, setDownloadPercent] = useState<number | undefined>(undefined);

  function download() {
    if (downloadDocument.isPending) {
      return;
    }
    setDownloadPercent(undefined);
    downloadDocument.mutate({ documentId: doc.id, fileName: doc.fileName, onProgress: setDownloadPercent });
  }

  return (
    <li className="flex flex-col gap-2 py-3">
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div className="min-w-0">
          {/* Le titre est le lien de téléchargement : c'est là que le regard et
              le clic vont d'eux-mêmes, un bouton « Télécharger » à part
              doublait le geste sans rien dire de plus. */}
          <button
            type="button"
            onClick={download}
            aria-busy={downloadDocument.isPending}
            title={`Télécharger ${doc.fileName}`}
            className="max-w-full truncate text-left text-sm font-medium text-brand-600 hover:underline dark:text-brand-400"
          >
            {doc.title}
          </button>
          <p className="truncate text-sm text-gray-500 dark:text-gray-400">
            {doc.title !== doc.fileName && `${doc.fileName} · `}
            {formatFileSize(doc.sizeBytes)} · {new Date(doc.uploadedAt).toLocaleDateString('fr-FR')}
          </p>
        </div>
        {!isConfirmingDelete && canWrite && (
          <div className="flex shrink-0 gap-2">
            <Button type="button" variant="secondary" onClick={() => setIsConfirmingDelete(true)}>
              Supprimer
            </Button>
          </div>
        )}
      </div>

      {downloadDocument.isPending && <ProgressBar value={downloadPercent} label={`Téléchargement de ${doc.fileName}`} />}
      {downloadDocument.isError && <Alert message={getErrorMessage(downloadDocument.error)} />}

      {isConfirmingDelete && (
        <div className="flex flex-col gap-2">
          <p className="text-sm text-gray-700 dark:text-gray-300">Supprimer ce document ? Cette action est irréversible.</p>
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
