import type { Message } from '@/features/messaging/types/messaging.types';

// Outlook-style thread item: plain flowing text, not a chat bubble - a
// compact sender + timestamp header line followed by the body paragraph,
// with no background tint, border box, or avatar. Messages are only
// separated from each other by the thin border-b divider below; "mine" is
// conveyed solely through the "Vous" label.
export function MessageThreadItem({ message }: { message: Message }) {
  const senderLabel = message.mine ? 'Vous' : message.senderName;

  return (
    <div className="border-b border-gray-100 px-4 py-4">
      <div className="flex items-baseline justify-between gap-2">
        <span className="truncate text-theme-sm font-semibold text-gray-900">{senderLabel}</span>
        <span className="shrink-0 text-theme-xs text-gray-400">
          {new Date(message.createdAt).toLocaleString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
          })}
        </span>
      </div>
      <p className="mt-1 whitespace-pre-wrap break-words text-theme-sm text-gray-700">{message.body}</p>
    </div>
  );
}
