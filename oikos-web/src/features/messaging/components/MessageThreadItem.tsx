import type { Message } from '@/features/messaging/types/messaging.types';

// Avatar-led card per message (initials circle, same bg-brand-50/text-brand-600
// treatment as the header's UserDropdown avatar - no profile photos in this
// app, so an initial stands in for one) rather than a flat text-only row:
// sender + timestamp header line, body paragraph below. Rounded-card visual
// only - no new behavior (no delete/archive/forward), a thread is still just
// a scroll of these, oldest first.
export function MessageThreadItem({ message }: { message: Message }) {
  const senderLabel = message.mine ? 'Vous' : message.senderName;

  return (
    <div className="flex gap-3 rounded-xl border border-gray-100 dark:border-gray-800 p-4">
      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-brand-50 dark:bg-brand-500/[0.12] text-sm font-medium text-brand-600 dark:text-brand-400">
        {senderLabel.charAt(0).toUpperCase()}
      </span>
      <div className="min-w-0 flex-1">
        <div className="flex items-baseline justify-between gap-2">
          <p className="flex min-w-0 items-baseline gap-1.5">
            <span className="truncate text-theme-sm font-semibold text-gray-900 dark:text-white/90">{senderLabel}</span>
            {/* OWNER is the unmarked default (matches the majority "un lot, une résidence" case);
                only BOARD is called out, since it is the exception that needs signaling - see the
                "en tant que" identity picker in NewConversationPage/MessageComposer. */}
            {message.senderIdentity === 'BOARD' && (
              <span className="shrink-0 rounded-full bg-warning-50 px-2 py-0.5 text-[11px] font-medium text-warning-700 dark:bg-warning-500/15 dark:text-warning-400">
                Bureau
              </span>
            )}
          </p>
          <span className="shrink-0 text-theme-xs text-gray-400 dark:text-gray-500">
            {new Date(message.createdAt).toLocaleString('fr-FR', {
              day: '2-digit',
              month: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
            })}
          </span>
        </div>
        <p className="mt-1 whitespace-pre-wrap break-words text-theme-sm text-gray-700 dark:text-gray-300">{message.body}</p>
      </div>
    </div>
  );
}
