import { useEffect, useState } from 'react';
import { Link, Outlet, useParams } from 'react-router-dom';
import { useMyConversations } from '@/features/messaging/hooks/useMyConversations';
import { ConversationListItem } from '@/features/messaging/components/ConversationListItem';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { AngleLeftIcon, AngleRightIcon } from '@/shared/icons';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { ConversationBox, ConversationSummary } from '@/features/messaging/types/messaging.types';

const SEARCH_DEBOUNCE_MS = 300;

const BOX_LABEL: Record<ConversationBox, string> = {
  RECEIVED: 'Réception',
  SENT: 'Envoyé',
};

const BOX_EMPTY_STATE: Record<ConversationBox, string> = {
  RECEIVED: 'Aucun message reçu',
  SENT: 'Aucun message envoyé',
};

/** Value handed down to ConversationPage through the reading-pane <Outlet/>. */
export interface MessagingOutletContext {
  conversationList: ConversationSummary[];
  box: ConversationBox;
}

interface MessagingLayoutProps {
  box: ConversationBox;
}

function RefreshIcon({ className = '' }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 20 20"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
      aria-hidden="true"
    >
      <path d="M16.25 10a6.25 6.25 0 1 1-2.007-4.594M16.25 3.75v3.75h-3.75" />
    </svg>
  );
}

// Single-pane view: either the message list (this component's <aside/>) or
// the open thread (whatever /messages/:conversationId resolves to, via
// <Outlet/>) - never both side by side. The list is the whole page until a
// message is clicked, which then replaces it with the detail view.
export function MessagingLayout({ box }: MessagingLayoutProps) {
  const { conversationId } = useParams<{ conversationId?: string }>();
  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    const timeout = setTimeout(() => setSearch(searchInput.trim()), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [searchInput]);

  const conversations = useMyConversations(page, box, search || undefined);
  const hasConversationOpen = Boolean(conversationId);

  const data = conversations.data;
  const from = data && data.totalElements > 0 ? data.pageNumber * data.pageSize + 1 : 0;
  const to = data ? data.pageNumber * data.pageSize + data.content.length : 0;

  return (
    <div className="flex h-[calc(100vh-160px)] overflow-hidden rounded-lg border border-gray-200 dark:border-gray-800 bg-white dark:bg-white/[0.03]">
      <aside className={`w-full flex-col border-gray-200 dark:border-gray-800 ${hasConversationOpen ? 'hidden' : 'flex'}`}>
        <div className="flex items-center justify-between gap-2 border-b border-gray-200 dark:border-gray-800 p-4">
          <h1 className="text-base font-semibold text-gray-900 dark:text-white/90">{BOX_LABEL[box]}</h1>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => conversations.refetch()}
              disabled={conversations.isFetching}
              aria-label="Actualiser les conversations"
              className="flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-white/[0.05] disabled:cursor-not-allowed disabled:opacity-60"
            >
              <RefreshIcon className={`h-4 w-4 ${conversations.isFetching ? 'animate-spin' : ''}`} />
            </button>
            <Link
              to="/messages/new"
              className="inline-flex min-h-9 items-center rounded-lg bg-brand-500 px-3 py-1.5 text-sm font-medium text-white hover:bg-brand-600"
            >
              Nouveau message
            </Link>
          </div>
        </div>

        <div className="border-b border-gray-200 dark:border-gray-800 p-3">
          <Input
            label="Rechercher"
            placeholder="Nom, copropriété…"
            value={searchInput}
            onChange={(e) => {
              setSearchInput(e.target.value);
              setPage(0);
            }}
          />
        </div>

        <div className="flex-1 overflow-y-auto">
          {conversations.isLoading && <Loader label="Chargement de vos conversations…" />}
          {conversations.isError && (
            <div className="p-4">
              <Alert message={getErrorMessage(conversations.error)} />
            </div>
          )}
          {conversations.data && conversations.data.content.length === 0 && (
            <div className="p-4">
              <EmptyState title={BOX_EMPTY_STATE[box]}>
                Envoyez un nouveau message avec le bouton ci-dessus.
              </EmptyState>
            </div>
          )}
          {conversations.data && conversations.data.content.length > 0 && (
            <ul className="flex flex-col">
              {conversations.data.content.map((conversation) => (
                <ConversationListItem
                  key={conversation.id}
                  conversation={conversation}
                  box={box}
                  isActive={conversation.id === conversationId}
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
      </aside>

      {hasConversationOpen && (
        <main className="flex w-full min-w-0 flex-1 flex-col">
          <Outlet context={{ conversationList: data?.content ?? [], box } satisfies MessagingOutletContext} />
        </main>
      )}
    </div>
  );
}
