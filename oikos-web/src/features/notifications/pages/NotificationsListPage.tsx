import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCurrentUser, isOwnerOnProperty, boardPropertyIds } from '@/features/identity/me';
import { useMandateProperties } from '@/shared/hooks/useMandateProperties';
import { useMyNotifications } from '@/features/notifications/hooks/useMyNotifications';
import { useMarkNotificationRead } from '@/features/notifications/hooks/useMarkNotificationRead';
import type { Notification } from '@/features/notifications/types/notification.types';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { AngleLeftIcon, AngleRightIcon } from '@/shared/icons';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

// 'ALL' and 'OWNED' are fixed tabs; any other value is a mandate property id
// (see mandateIds below) - one tab per copropriété the caller has a board
// mandate on, rather than a single combined "Mes mandats" tab, so a
// multi-mandate account can tell its résidences' notifications apart.
type Scope = 'ALL' | 'OWNED' | string;

const PAGE_SIZE = 20;

// Rule 6 of the target UX (GAP.md): notifications are always global, never
// cloisonnées by the active space - this page is reached the same way
// regardless of which space the viewer is in, and lists across every
// property at once. Tabs filter the already-fetched page in memory against
// the caller's own roleByProperty (see access.ts), the same client-side
// classification pattern AppSidebar/DashboardPage already use, rather than
// a second server-side scope parameter - simpler, and correct-enough at the
// small list sizes a personal inbox has.
export function NotificationsListPage() {
  const navigate = useNavigate();
  const currentUser = useCurrentUser();
  const [page, setPage] = useState(0);
  const [scope, setScope] = useState<Scope>('ALL');
  const notifications = useMyNotifications(page, PAGE_SIZE);
  const markRead = useMarkNotificationRead();

  const data = notifications.data;
  const user = currentUser.data;
  const mandateIds = user ? boardPropertyIds(user) : [];
  const mandateProperties = useMandateProperties(mandateIds);

  const scopeTabs: { key: Scope; label: string }[] = [
    { key: 'ALL', label: 'Tout' },
    { key: 'OWNED', label: 'Mes lots' },
    ...mandateIds.map((propertyId) => ({
      key: propertyId,
      label: mandateProperties.byId.get(propertyId)?.name ?? '…',
    })),
  ];

  function matchesScope(notification: Notification): boolean {
    if (scope === 'ALL' || !user) {
      return true;
    }
    if (!notification.propertyId) {
      return false;
    }
    if (scope === 'OWNED') {
      return isOwnerOnProperty(user, notification.propertyId);
    }
    // Any other scope value is a specific mandate property id (see scopeTabs above).
    return notification.propertyId === scope;
  }

  function openNotification(notification: Notification) {
    if (!notification.read) {
      markRead.mutate(notification.id);
    }
    if (notification.linkPath) {
      navigate(notification.linkPath);
    }
  }

  const visibleContent = (data?.content ?? []).filter(matchesScope);
  const from = data && data.totalElements > 0 ? data.pageNumber * data.pageSize + 1 : 0;
  const to = data ? data.pageNumber * data.pageSize + data.content.length : 0;

  return (
    <div className="flex h-[calc(100vh-160px)] flex-col overflow-hidden rounded-lg border border-gray-200 dark:border-gray-800 bg-white dark:bg-white/[0.03]">
      <div className="flex items-center justify-between gap-2 border-b border-gray-200 dark:border-gray-800 p-4">
        <h1 className="text-base font-semibold text-gray-900 dark:text-white/90">Notifications</h1>
      </div>

      <div className="flex flex-wrap gap-2 border-b border-gray-200 dark:border-gray-800 p-3">
        {scopeTabs.map((tab) => (
          <button
            key={tab.key}
            type="button"
            aria-pressed={scope === tab.key}
            onClick={() => setScope(tab.key)}
            className={`rounded-lg px-3 py-1.5 text-sm font-medium transition-colors ${
              scope === tab.key
                ? 'bg-brand-500 text-white'
                : 'text-gray-600 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/[0.05]'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="flex-1 overflow-y-auto">
        {notifications.isLoading && <Loader label="Chargement des notifications…" />}
        {notifications.isError && (
          <div className="p-4">
            <Alert message={getErrorMessage(notifications.error)} />
          </div>
        )}
        {data && visibleContent.length === 0 && (
          <div className="p-4">
            <EmptyState title="Aucune notification">
              {scope === 'ALL'
                ? 'Vous serez notifié ici des événements qui vous concernent (échéances, assemblées, demandes…).'
                : 'Aucune notification dans cette catégorie pour le moment.'}
            </EmptyState>
          </div>
        )}
        {visibleContent.length > 0 && (
          <ul className="flex flex-col">
            {visibleContent.map((notification) => (
              <li key={notification.id}>
                <button
                  type="button"
                  onClick={() => openNotification(notification)}
                  className="flex w-full flex-col gap-0.5 border-b border-gray-100 dark:border-gray-800 px-4 py-3 text-left hover:bg-gray-50 dark:hover:bg-white/[0.03]"
                >
                  <div className="flex items-center justify-between gap-2">
                    <span className="flex items-center gap-2 min-w-0">
                      {!notification.read && (
                        <span aria-hidden className="h-2 w-2 shrink-0 rounded-full bg-brand-500" />
                      )}
                      <span
                        className={`truncate text-theme-sm ${notification.read ? 'font-medium text-gray-700 dark:text-gray-300' : 'font-semibold text-gray-900 dark:text-white/90'}`}
                      >
                        {notification.title}
                      </span>
                    </span>
                    <span className="shrink-0 text-theme-xs text-gray-400 dark:text-gray-500">
                      {formatRelativeTime(notification.createdAt)}
                    </span>
                  </div>
                  {notification.body && (
                    <p className="truncate text-theme-xs text-gray-500 dark:text-gray-400">{notification.body}</p>
                  )}
                </button>
              </li>
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
