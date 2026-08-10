import { useEffect, useState } from 'react';
import { Link, Outlet, useParams } from 'react-router-dom';
import { useMyConversations } from '@/features/messaging/hooks/useMyConversations';
import { ConversationListItem } from '@/features/messaging/components/ConversationListItem';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { ChatIcon, AngleLeftIcon, AngleRightIcon } from '@/shared/icons';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { ConversationSummary } from '@/features/messaging/types/messaging.types';

const SEARCH_DEBOUNCE_MS = 300;

/** Value handed down to ConversationPage through the reading-pane <Outlet/>. */
export interface MessagingOutletContext {
  conversationList: ConversationSummary[];
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

// Outlook-style split view: a message-list pane on the left (this component)
// and a reading pane on the right (whatever /messages/:conversationId
// resolves to, via <Outlet/>) - not a single full-screen chat thread. On
// mobile, only one pane is visible at a time (list, or the open thread),
// matching how Outlook's own mobile app collapses down to one pane.
export function MessagingLayout() {
  const { conversationId } = useParams<{ conversationId?: string }>();
  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    const timeout = setTimeout(() => setSearch(searchInput.trim()), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [searchInput]);

  const conversations = useMyConversations(page, search || undefined);
  const hasConversationOpen = Boolean(conversationId);

  const data = conversations.data;
  const from = data && data.totalElements > 0 ? data.pageNumber * data.pageSize + 1 : 0;
  const to = data ? data.pageNumber * data.pageSize + data.content.length : 0;

  return (
    <div className="flex h-[calc(100vh-160px)] overflow-hidden rounded-lg border border-gray-200 bg-white">
      <aside
        className={`w-full flex-col border-gray-200 sm:flex sm:w-[340px] sm:shrink-0 sm:border-r ${
          hasConversationOpen ? 'hidden' : 'flex'
        }`}
      >
        <div className="flex items-center justify-between gap-2 border-b border-gray-200 p-4">
          <h1 className="text-base font-semibold text-gray-900">Messagerie</h1>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => conversations.refetch()}
              disabled={conversations.isFetching}
              aria-label="Actualiser les conversations"
              className="flex h-9 w-9 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-60"
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

        <div className="border-b border-gray-200 p-3">
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
              <EmptyState title="Aucun message">
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
                  isActive={conversation.id === conversationId}
                />
              ))}
            </ul>
          )}
        </div>

        {data && data.totalElements > 0 && (
          <div className="flex items-center justify-between gap-2 border-t border-gray-200 px-3 py-2">
            <span className="text-theme-xs text-gray-500">
              Affichage {from}-{to} sur {data.totalElements}
            </span>
            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={() => setPage((current) => current - 1)}
                disabled={data.pageNumber <= 0}
                aria-label="Page précédente"
                className="flex h-8 w-8 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-40"
              >
                <AngleLeftIcon className="h-4 w-4" />
              </button>
              <button
                type="button"
                onClick={() => setPage((current) => current + 1)}
                disabled={data.pageNumber + 1 >= data.totalPages}
                aria-label="Page suivante"
                className="flex h-8 w-8 items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-40"
              >
                <AngleRightIcon className="h-4 w-4" />
              </button>
            </div>
          </div>
        )}
      </aside>

      <main className={`min-w-0 flex-1 flex-col sm:flex ${hasConversationOpen ? 'flex' : 'hidden'}`}>
        {hasConversationOpen ? (
          <Outlet context={{ conversationList: data?.content ?? [] } satisfies MessagingOutletContext} />
        ) : (
          <div className="flex flex-1 flex-col items-center justify-center gap-2 p-8 text-center text-gray-400">
            <ChatIcon className="h-10 w-10" />
            <p className="text-sm">Sélectionnez un message pour l’afficher ici.</p>
          </div>
        )}
      </main>
    </div>
  );
}
