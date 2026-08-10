import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Dropdown } from '@/shared/components/Dropdown/Dropdown';
import { Badge } from '@/shared/components/Badge/Badge';
import { ChatIcon } from '@/shared/icons';
import { useUnreadSummary } from '@/features/messaging/hooks/useUnreadSummary';
import { conversationTitle } from '@/features/messaging/components/ConversationListItem';
import { formatRelativeTime } from '@/features/messaging/utils/formatRelativeTime';

export function NotificationBell() {
  const [isOpen, setIsOpen] = useState(false);
  const unreadSummary = useUnreadSummary();
  const totalUnread = unreadSummary.data?.totalUnreadMessageCount ?? 0;
  const recentUnread = unreadSummary.data?.recentUnread ?? [];

  function closeDropdown() {
    setIsOpen(false);
  }

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen((value) => !value)}
        aria-label="Notifications de messagerie"
        className="dropdown-toggle relative flex h-11 w-11 items-center justify-center rounded-full text-gray-500 hover:bg-gray-100 hover:text-gray-700"
      >
        <ChatIcon className="h-5 w-5" />
        {totalUnread > 0 && (
          <Badge color="error" variant="solid" className="absolute -right-1 -top-1">
            {totalUnread > 99 ? '99+' : totalUnread}
          </Badge>
        )}
      </button>

      <Dropdown isOpen={isOpen} onClose={closeDropdown} className="flex w-[320px] flex-col p-3 sm:w-[380px]">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <h5 className="text-base font-semibold text-gray-800">Messages</h5>
        </div>
        <ul className="flex max-h-[360px] flex-col overflow-y-auto">
          {recentUnread.length === 0 && (
            <li className="px-1 py-6 text-center text-sm text-gray-500">Aucun message non lu.</li>
          )}
          {recentUnread.map((conversation) => {
            const title = conversationTitle(conversation);
            return (
              <li key={conversation.id}>
                <Link
                  to={`/messages/reception/${conversation.id}`}
                  onClick={closeDropdown}
                  className="flex flex-col gap-0.5 rounded-lg border-b border-gray-100 px-3 py-2.5 hover:bg-gray-100"
                >
                  <span className="flex items-center justify-between gap-2 text-sm font-medium text-gray-800">
                    <span className="truncate">{title}</span>
                    <span className="shrink-0 rounded-full bg-brand-500 px-1.5 text-xs font-medium text-white">
                      {conversation.unreadCount}
                    </span>
                  </span>
                  <span className="truncate text-xs text-gray-500">{conversation.lastMessagePreview}</span>
                  <span className="text-xs text-gray-400">
                    {conversation.propertyName}
                    {conversation.lastMessageAt && ` · ${formatRelativeTime(conversation.lastMessageAt)}`}
                  </span>
                </Link>
              </li>
            );
          })}
        </ul>
        <Link
          to="/messages/reception"
          onClick={closeDropdown}
          className="mt-3 block rounded-lg border border-gray-300 bg-white px-4 py-2 text-center text-sm font-medium text-gray-700 hover:bg-gray-100"
        >
          Voir tous les messages
        </Link>
      </Dropdown>
    </div>
  );
}
