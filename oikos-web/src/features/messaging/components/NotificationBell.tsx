import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useEffectiveSpace, spaceQuerySuffix } from '@/shared/hooks/useEffectiveSpace';
import { Dropdown } from '@/shared/components/Dropdown/Dropdown';
import { Badge } from '@/shared/components/Badge/Badge';
import { ChatIcon } from '@/shared/icons';
import { useUnreadSummary } from '@/features/messaging/hooks/useUnreadSummary';
import { conversationTitle } from '@/features/messaging/components/ConversationListItem';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';

export function NotificationBell() {
  const [isOpen, setIsOpen] = useState(false);
  const unreadSummary = useUnreadSummary();
  const totalUnread = unreadSummary.data?.totalUnreadMessageCount ?? 0;
  const recentUnread = unreadSummary.data?.recentUnread ?? [];
  // Carried onto every link below so opening a notification from the board
  // space doesn't silently drop the viewer back into owner (see
  // spaceQuerySuffix) - this bell is mounted in the header regardless of
  // route, so it must never assume which space the viewer is currently in.
  const spaceSuffix = spaceQuerySuffix(useEffectiveSpace());

  function closeDropdown() {
    setIsOpen(false);
  }

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen((value) => !value)}
        aria-label="Notifications de messagerie"
        className="dropdown-toggle relative flex h-11 w-11 items-center justify-center rounded-full text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-white/[0.05] hover:text-gray-700 dark:hover:text-gray-300"
      >
        <ChatIcon className="h-5 w-5" />
        {totalUnread > 0 && (
          <Badge color="error" variant="solid" className="absolute -right-1 -top-1">
            {totalUnread > 99 ? '99+' : totalUnread}
          </Badge>
        )}
      </button>

      <Dropdown isOpen={isOpen} onClose={closeDropdown} className="flex w-[320px] flex-col p-3 sm:w-[380px]">
        <div className="flex items-center justify-between border-b border-gray-100 dark:border-gray-800 pb-3">
          <h5 className="text-base font-semibold text-gray-800 dark:text-white/90">Messages</h5>
        </div>
        <ul className="flex max-h-[360px] flex-col overflow-y-auto">
          {recentUnread.length === 0 && (
            <li className="px-1 py-6 text-center text-sm text-gray-500 dark:text-gray-400">Aucun message non lu.</li>
          )}
          {recentUnread.map((conversation) => {
            const title = conversationTitle(conversation);
            return (
              <li key={conversation.id}>
                <Link
                  to={`/messages/reception/${conversation.id}${spaceSuffix}`}
                  onClick={closeDropdown}
                  className="flex flex-col gap-0.5 rounded-lg border-b border-gray-100 dark:border-gray-800 px-3 py-2.5 hover:bg-gray-100 dark:hover:bg-white/[0.05]"
                >
                  <span className="flex items-center justify-between gap-2 text-sm font-medium text-gray-800 dark:text-white/90">
                    <span className="truncate">{title}</span>
                    <span className="shrink-0 rounded-full bg-brand-500 px-1.5 text-xs font-medium text-white">
                      {conversation.unreadCount}
                    </span>
                  </span>
                  <span className="truncate text-xs text-gray-500 dark:text-gray-400">{conversation.lastMessagePreview}</span>
                  <span className="text-xs text-gray-400 dark:text-gray-500">
                    {conversation.propertyName}
                    {conversation.lastMessageAt && ` · ${formatRelativeTime(conversation.lastMessageAt)}`}
                  </span>
                </Link>
              </li>
            );
          })}
        </ul>
        <Link
          to={`/messages/reception${spaceSuffix}`}
          onClick={closeDropdown}
          className="mt-3 block rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-4 py-2 text-center text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-white/[0.05]"
        >
          Voir tous les messages
        </Link>
      </Dropdown>
    </div>
  );
}
