import type { Message } from '@/features/messaging/types/messaging.types';
import { renderMessageBody } from '@/features/messaging/utils/renderMessageBody';

// Avatar-led card per message (initials circle, same bg-brand-50/text-brand-600
// treatment as the header's UserDropdown avatar - no profile photos in this
// app, so an initial stands in for one) rather than a flat text-only row:
// "De: … à …" / "A: …" header lines, body paragraph below. Rounded-card
// visual only - no new behavior (no delete/archive/forward), a thread is
// still just a scroll of these, oldest first.
interface MessageThreadItemProps {
  message: Message;
  /** Who this message went to, e.g. the other participant(s)' names or a
   * broadcast audience label - conversation-level (same for every message in
   * the thread), since individual messages don't carry their own recipient
   * list. */
  recipientLabel: string;
}

export function MessageThreadItem({ message, recipientLabel }: MessageThreadItemProps) {
  const senderLabel = message.mine ? 'Vous' : message.senderName;
  const toLabel = message.mine ? recipientLabel : 'Vous';
  const dateLabel = new Date(message.createdAt).toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div className="flex gap-3 rounded-xl border border-gray-100 dark:border-gray-800 p-4">
      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-brand-50 dark:bg-brand-500/[0.12] text-sm font-medium text-brand-600 dark:text-brand-400">
        {senderLabel.charAt(0).toUpperCase()}
      </span>
      <div className="min-w-0 flex-1">
        <p className="flex min-w-0 flex-wrap items-baseline gap-x-1.5 gap-y-0.5">
          <span className="truncate text-theme-sm font-semibold text-gray-900 dark:text-white/90">
            De : {senderLabel}
          </span>
          <span className="shrink-0 text-theme-xs text-gray-400 dark:text-gray-500">à {dateLabel}</span>
          {/* OWNER is the unmarked default (matches the majority "un lot, une résidence" case);
              only BOARD is called out, since it is the exception that needs signaling - see the
              "en tant que" identity picker in NewConversationPage/MessageComposer. */}
          {message.senderIdentity === 'BOARD' && (
            <span className="shrink-0 rounded-full bg-warning-50 px-2 py-0.5 text-[11px] font-medium text-warning-700 dark:bg-warning-500/15 dark:text-warning-400">
              Bureau
            </span>
          )}
        </p>
        <p className="mt-0.5 truncate text-theme-xs text-gray-500 dark:text-gray-400">A : {toLabel}</p>
        <div className="mt-2 text-theme-sm text-gray-700 dark:text-gray-300">{renderMessageBody(message.body)}</div>
      </div>
    </div>
  );
}
