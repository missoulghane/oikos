import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useCurrentUser, canReadDocuments, canWriteDocuments } from '@/features/identity/me';
import { useDocuments } from '@/features/property-mngt/documents/hooks/useDocuments';
import { UploadDocumentForm } from '@/features/property-mngt/documents/components/UploadDocumentForm';
import { DocumentList } from '@/features/property-mngt/documents/components/DocumentList';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function PropertyDocumentsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const canRead = currentUser.data ? canReadDocuments(currentUser.data, property.id) : false;
  const canWrite = currentUser.data ? canWriteDocuments(currentUser.data, property.id) : false;

  const [page, setPage] = useState(0);
  const documents = useDocuments('PROPERTY', property.id, page);

  if (!canRead) {
    return <Alert message="Vous n'avez pas accès aux documents de cette copropriété." />;
  }

  return (
    <Card className="flex flex-col gap-4">
      {canWrite && <UploadDocumentForm ownerType="PROPERTY" ownerId={property.id} />}

      {documents.isLoading && <Loader label="Chargement des documents…" />}
      {documents.isError && <Alert message={getErrorMessage(documents.error)} />}
      {documents.data && documents.data.content.length === 0 && (
        <EmptyState title="Aucun document">Aucun document n'a encore été téléversé pour cette copropriété.</EmptyState>
      )}
      {documents.data && documents.data.content.length > 0 && (
        <div className="flex flex-col gap-3">
          <DocumentList documents={documents.data.content} canWrite={canWrite} />
          <Pagination pageNumber={documents.data.pageNumber} totalPages={documents.data.totalPages} onPageChange={setPage} />
        </div>
      )}
    </Card>
  );
}
