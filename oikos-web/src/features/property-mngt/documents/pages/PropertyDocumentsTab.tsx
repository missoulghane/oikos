import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useCurrentUser, canReadDocuments, canWriteDocuments } from '@/features/identity/me';
import { useDocuments } from '@/features/property-mngt/documents/hooks/useDocuments';
import { UploadDocumentForm } from '@/features/property-mngt/documents/components/UploadDocumentForm';
import { DocumentList } from '@/features/property-mngt/documents/components/DocumentList';
import { Card } from '@/shared/components/Card/Card';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

const PAGE_SIZE = 10;
const SEARCH_DEBOUNCE_MS = 300;

export function PropertyDocumentsTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const currentUser = useCurrentUser();
  const canRead = currentUser.data ? canReadDocuments(currentUser.data, property.id) : false;
  const canWrite = currentUser.data ? canWriteDocuments(currentUser.data, property.id) : false;

  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');

  // La recherche part après une courte pause de frappe, pas à chaque touche,
  // et repart de la première page : la page 3 d'une liste filtrée n'a souvent
  // plus rien à montrer.
  useEffect(() => {
    const timeout = setTimeout(() => {
      setSearch(searchInput.trim());
      setPage(0);
    }, SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [searchInput]);

  const documents = useDocuments('PROPERTY', property.id, page, PAGE_SIZE, search);

  if (!canRead) {
    return <Alert message="Vous n'avez pas accès aux documents de cette copropriété." />;
  }

  const items = documents.data?.content ?? [];

  return (
    <Card className="flex flex-col gap-4">
      {canWrite && <UploadDocumentForm ownerType="PROPERTY" ownerId={property.id} />}

      <Input
        id="document-search"
        label="Rechercher un document"
        type="search"
        placeholder="Titre ou nom du fichier"
        value={searchInput}
        onChange={(event) => setSearchInput(event.target.value)}
      />

      {documents.isLoading && <Loader label="Chargement des documents…" />}
      {documents.isError && <Alert message={getErrorMessage(documents.error)} />}
      {documents.data && items.length === 0 && (
        search ? (
          <EmptyState title="Aucun résultat">Aucun document ne correspond à « {search} ».</EmptyState>
        ) : (
          <EmptyState title="Aucun document">Aucun document n'a encore été ajouté pour cette copropriété.</EmptyState>
        )
      )}
      {items.length > 0 && documents.data && (
        <div className="flex flex-col gap-3">
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {documents.data.totalElements} document{documents.data.totalElements > 1 ? 's' : ''}
          </p>
          <DocumentList documents={items} canWrite={canWrite} />
          <Pagination pageNumber={documents.data.pageNumber} totalPages={documents.data.totalPages} onPageChange={setPage} />
        </div>
      )}
    </Card>
  );
}
