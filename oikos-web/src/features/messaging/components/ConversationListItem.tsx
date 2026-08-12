import { Link } from 'react-router-dom';
import { Badge } from '@/shared/components/Badge/Badge';
import type { ConversationBox, ConversationSummary } from '@/features/messaging/types/messaging.types';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { BOX_PATH } from '@/features/messaging/utils/boxPath';

export const BROADCAST_CONVERSATION_LABEL = 'Annonces de la copropriété';

// Outlook-style title: the message's subject (see ConversationSubject on
// the backend), not the participant list - "who it's with" is secondary
// info, shown separately (see participantsLine below).
export function conversationTitle(conversation: ConversationSummary): string {
  if (conversation.type === 'BROADCAST') {
    return BROADCAST_CONVERSATION_LABEL;
  }
  return conversation.subject ?? 'Conversation';
}

function participantsLine(conversation: ConversationSummary): string | null {
  if (conversation.type === 'BROADCAST' || conversation.participants.length === 0) {
    return null;
  }
  return conversation.participants.map((participant) => participant.fullName).join(', ');
}

interface ConversationListItemProps {
  conversation: ConversationSummary;
  box: ConversationBox;
  isActive?: boolean;
  /** "?space=…" from the caller's useEffectiveSpace() (see spaceQuerySuffix) - computed once by
   * the list page rather than per-row, and carried onto the link so opening a thread from the
   * board space doesn't silently drop the viewer back into owner. */
  spaceSuffix?: string;
}

// Dense, avatar-less table row (Outlook inbox style) rather than a
// rounded-card + circular-avatar chat entry: title/timestamp on one line,
// thin border-b divider between rows. No content preview - the list is
// purely "who/what/when", the message body only appears once you open it.
export function ConversationListItem({ conversation, box, isActive = false, spaceSuffix = '' }: ConversationListItemProps) {
  const title = conversationTitle(conversation);
  const participants = participantsLine(conversation);
  const hasUnread = conversation.unreadCount > 0;

  return (
    <li>
      <Link
        to={`${BOX_PATH[box]}/${conversation.id}${spaceSuffix}`}
        className={`flex flex-col gap-0.5 border-b border-l-2 border-gray-100 dark:border-gray-800 px-3 py-2.5 ${
          isActive ? 'border-l-brand-500 bg-brand-50 dark:bg-brand-500/[0.12]' : 'border-l-transparent hover:bg-gray-50 dark:hover:bg-white/[0.03]'
        }`}
      >
        <div className="flex items-baseline justify-between gap-2">
          <p className="flex min-w-0 items-baseline gap-1.5">
            <span
              className={`truncate text-theme-sm ${hasUnread ? 'font-semibold text-gray-900 dark:text-white/90' : 'font-medium text-gray-700 dark:text-gray-300'}`}
            >
              {title}
            </span>
            {/* A conversation is a message with replies - only shown once
                that's actually true, never for a plain single message. */}
            {conversation.messageCount > 1 && (
              <span className="shrink-0 text-theme-xs font-normal text-gray-400 dark:text-gray-500">
                ({conversation.messageCount} messages)
              </span>
            )}
            {conversation.type === 'BOARD_PRIVATE' && (
              <span className="shrink-0 rounded-full bg-warning-50 px-2 py-0.5 text-[11px] font-medium text-warning-700 dark:bg-warning-500/15 dark:text-warning-400">
                Privé · bureau
              </span>
            )}
            {conversation.concernsUnit && (
              <span className="shrink-0 rounded-full bg-gray-100 px-2 py-0.5 text-[11px] font-medium text-gray-500 dark:bg-white/[0.05] dark:text-gray-400">
                Concerne {conversation.concernsUnit}
              </span>
            )}
          </p>
          <span className="flex shrink-0 items-center gap-2">
            {conversation.lastMessageAt && (
              <span className="text-theme-xs text-gray-400 dark:text-gray-500">
                {formatRelativeTime(conversation.lastMessageAt)}
              </span>
            )}
            {hasUnread && (
              <Badge color="primary" variant="solid">
                {conversation.unreadCount}
              </Badge>
            )}
          </span>
        </div>
        <p className="truncate text-theme-xs text-gray-400 dark:text-gray-500">
          {participants ? `${participants} · ${conversation.propertyName}` : conversation.propertyName}
        </p>
      </Link>
    </li>
  );
}
