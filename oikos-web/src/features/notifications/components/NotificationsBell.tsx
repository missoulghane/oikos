import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useEffectiveSpace, spaceQuerySuffix } from '@/shared/hooks/useEffectiveSpace';
import { Dropdown } from '@/shared/components/Dropdown/Dropdown';
import { Badge } from '@/shared/components/Badge/Badge';
import { BellIcon } from '@/shared/icons';
import { useMyNotifications } from '@/features/notifications/hooks/useMyNotifications';
import { useUnreadNotificationCount } from '@/features/notifications/hooks/useUnreadNotificationCount';
import { useMarkNotificationRead } from '@/features/notifications/hooks/useMarkNotificationRead';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';

const RECENT_PAGE_SIZE = 5;

// The generic notification center (target UX rule 6: "toujours globales") -
// distinct from messaging's own NotificationBell (really a "messages"
// inbox indicator, kept as-is under that name for now). Always empty today:
// no producer creates a Notification yet (see NotificationType's javadoc,
// backend) - this is real, wired infrastructure sitting ahead of that work.
export function NotificationsBell() {
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const unreadCount = useUnreadNotificationCount();
  // Non lues seulement : la cloche montre ce que sa pastille compte. Une
  // notification lue - ouverte ici, ou depuis la liste - en disparaît.
  const recent = useMyNotifications(0, RECENT_PAGE_SIZE, true);
  const markRead = useMarkNotificationRead();
  const totalUnread = unreadCount.data?.unreadCount ?? 0;
  // Carried onto "Voir toutes les notifications" so opening the inbox from
  // the board space doesn't silently drop the viewer back into owner (see
  // spaceQuerySuffix) - this bell is mounted in the header regardless of route.
  const spaceSuffix = spaceQuerySuffix(useEffectiveSpace());

  function closeDropdown() {
    setIsOpen(false);
  }

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen((value) => !value)}
        aria-label="Notifications"
        className="dropdown-toggle relative flex h-11 w-11 items-center justify-center rounded-full text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-white/[0.05] hover:text-gray-700 dark:hover:text-gray-300"
      >
        <BellIcon className="h-5 w-5" />
        {totalUnread > 0 && (
          <Badge color="error" variant="solid" className="absolute -right-1 -top-1">
            {totalUnread > 99 ? '99+' : totalUnread}
          </Badge>
        )}
      </button>

      <Dropdown isOpen={isOpen} onClose={closeDropdown} className="flex w-[320px] flex-col p-3 sm:w-[380px]">
        <div className="flex items-center justify-between border-b border-gray-100 dark:border-gray-800 pb-3">
          <h5 className="text-base font-semibold text-gray-800 dark:text-white/90">Notifications</h5>
        </div>
        <ul className="flex max-h-[360px] flex-col overflow-y-auto">
          {(recent.data?.content.length ?? 0) === 0 && (
            <li className="px-1 py-6 text-center text-sm text-gray-500 dark:text-gray-400">
              Aucune notification non lue.
            </li>
          )}
          {recent.data?.content.map((notification) => (
            <li key={notification.id}>
              <button
                type="button"
                onClick={() => {
                  // Ouvrir vaut lire : la ligne quitte la cloche et la pastille
                  // se décrémente. Le garde-fou sur `read` reste, la liste
                  // pourrait revenir du cache juste avant l'invalidation.
                  if (!notification.read) {
                    markRead.mutate(notification.id);
                  }
                  closeDropdown();
                  // Et ouvrir vaut aller voir : la cloche marquait comme lu
                  // puis ne menait nulle part, ce qui laissait au lecteur le
                  // soin de retrouver seul l'écran concerné - exactement ce que
                  // le producteur de la notification a déjà pris la peine
                  // d'indiquer. Même comportement que la liste complète (voir
                  // NotificationsListPage). Le chemin reste un simple indice
                  // d'affichage, jamais validé contre la table de routage : une
                  // notification sans lien ne fait que se refermer.
                  if (notification.linkPath) {
                    navigate(notification.linkPath);
                  }
                }}
                className="flex w-full flex-col gap-0.5 rounded-lg border-b border-gray-100 dark:border-gray-800 px-3 py-2.5 text-left hover:bg-gray-100 dark:hover:bg-white/[0.05]"
              >
                <span className="flex items-center justify-between gap-2 text-sm font-medium text-gray-800 dark:text-white/90">
                  <span className="truncate">{notification.title}</span>
                  {!notification.read && <span aria-hidden className="h-2 w-2 shrink-0 rounded-full bg-brand-500" />}
                </span>
                {notification.body && (
                  <span className="truncate text-xs text-gray-500 dark:text-gray-400">{notification.body}</span>
                )}
                <span className="text-xs text-gray-400 dark:text-gray-500">{formatRelativeTime(notification.createdAt)}</span>
              </button>
            </li>
          ))}
        </ul>
        <Link
          to={`/notifications${spaceSuffix}`}
          onClick={closeDropdown}
          className="mt-3 block rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-4 py-2 text-center text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-white/[0.05]"
        >
          Voir toutes les notifications
        </Link>
      </Dropdown>
    </div>
  );
}
