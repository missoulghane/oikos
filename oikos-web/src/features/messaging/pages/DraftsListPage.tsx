import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useMyDrafts } from '@/features/messaging/hooks/useMyDrafts';
import { useDeleteDraft } from '@/features/messaging/hooks/useDeleteDraft';
import { DraftListItem } from '@/features/messaging/components/DraftListItem';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { AngleLeftIcon, AngleRightIcon } from '@/shared/icons';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const SEARCH_DEBOUNCE_MS = 300;

// Same header/search/pagination chrome as MessagingLayout, but no split-pane
// Outlet: a draft is never "read" in place, clicking one always resumes
// editing via /messages/new?draftId= (see DraftListItem), so there is no
// detail route to nest under this page.
export function DraftsListPage() {
  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    const timeout = setTimeout(() => setSearch(searchInput.trim()), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [searchInput]);

  const drafts = useMyDrafts(page, search || undefined);
  const deleteDraft = useDeleteDraft();

  const data = drafts.data;
  const from = data && data.totalElements > 0 ? data.pageNumber * data.pageSize + 1 : 0;
  const to = data ? data.pageNumber * data.pageSize + data.content.length : 0;

  return (
    <div className="flex h-[calc(100vh-160px)] flex-col overflow-hidden rounded-lg border border-gray-200 dark:border-gray-800 bg-white dark:bg-white/[0.03]">
      <div className="flex items-center justify-between gap-2 border-b border-gray-200 dark:border-gray-800 p-4">
        <h1 className="text-base font-semibold text-gray-900 dark:text-white/90">Brouillon</h1>
        <Link
          to="/messages/new"
          className="inline-flex min-h-9 items-center rounded-lg bg-brand-500 px-3 py-1.5 text-sm font-medium text-white hover:bg-brand-600"
        >
          Nouveau message
        </Link>
      </div>

      <div className="border-b border-gray-200 dark:border-gray-800 p-3">
        <Input
          label="Rechercher"
          placeholder="Titre, contenu…"
          value={searchInput}
          onChange={(e) => {
            setSearchInput(e.target.value);
            setPage(0);
          }}
        />
      </div>

      <div className="flex-1 overflow-y-auto">
        {drafts.isLoading && <Loader label="Chargement de vos brouillons…" />}
        {drafts.isError && (
          <div className="p-4">
            <Alert message={getErrorMessage(drafts.error)} />
          </div>
        )}
        {drafts.data && drafts.data.content.length === 0 && (
          <div className="p-4">
            <EmptyState title="Aucun brouillon">
              Enregistrez un message en cours comme brouillon pour le retrouver ici.
            </EmptyState>
          </div>
        )}
        {drafts.data && drafts.data.content.length > 0 && (
          <ul className="flex flex-col">
            {drafts.data.content.map((draft) => (
              <DraftListItem
                key={draft.id}
                draft={draft}
                onDelete={(draftId) => deleteDraft.mutate(draftId)}
                isDeleting={deleteDraft.isPending && deleteDraft.variables === draft.id}
              />
            ))}
          </ul>
        )}
      </div>

      {data && data.totalElements > 0 && (
        <div className="flex items-center justify-between gap-2 border-t border-gray-200 dark:border-gray-800 px-3 py-2">
          <span className="text-theme-xs text-gray-500 dark:text-gray-400">
            Affichage {from}-{to} sur {data.totalElements}
          </span>
          <div className="flex items-center gap-1">
            <button
              type="button"
              onClick={() => setPage((current) => current - 1)}
              disabled={data.pageNumber <= 0}
              aria-label="Page précédente"
              className="flex h-8 w-8 items-center justify-center rounded-lg text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-white/[0.05] disabled:cursor-not-allowed disabled:opacity-40"
            >
              <AngleLeftIcon className="h-4 w-4" />
            </button>
            <button
              type="button"
              onClick={() => setPage((current) => current + 1)}
              disabled={data.pageNumber + 1 >= data.totalPages}
              aria-label="Page suivante"
              className="flex h-8 w-8 items-center justify-center rounded-lg text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-white/[0.05] disabled:cursor-not-allowed disabled:opacity-40"
            >
              <AngleRightIcon className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
