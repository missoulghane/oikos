import { Link } from 'react-router-dom';
import type { MessageDraftSummary } from '@/features/messaging/types/messaging.types';
import { formatRelativeTime } from '@/features/messaging/utils/formatRelativeTime';
import { TrashBinIcon } from '@/shared/icons';

function draftTitle(draft: MessageDraftSummary): string {
  return draft.subject?.trim() || 'Sans titre';
}

function recipientsLine(draft: MessageDraftSummary): string {
  if (draft.broadcast) {
    return 'Toute la copropriété';
  }
  if (draft.recipients.length === 0) {
    return 'Aucun destinataire';
  }
  return draft.recipients.map((recipient) => recipient.fullName).join(', ');
}

interface DraftListItemProps {
  draft: MessageDraftSummary;
  onDelete: (draftId: string) => void;
  isDeleting?: boolean;
}

// Two clickable zones side by side rather than one big <Link> (unlike
// ConversationListItem): the delete button must not be a descendant of the
// row's Link (nested interactive elements + click-through navigation), so
// only the content zone opens the draft in the compose form.
export function DraftListItem({ draft, onDelete, isDeleting = false }: DraftListItemProps) {
  return (
    <li className="flex items-center gap-1 border-b border-gray-100 dark:border-gray-800 pr-2">
      <Link
        to={`/messages/new?draftId=${draft.id}`}
        className="flex min-w-0 flex-1 flex-col gap-0.5 px-3 py-2.5 hover:bg-gray-50 dark:hover:bg-white/[0.03]"
      >
        <div className="flex items-baseline justify-between gap-2">
          <span className="truncate text-theme-sm font-medium text-gray-700 dark:text-gray-300">{draftTitle(draft)}</span>
          <span className="shrink-0 text-theme-xs text-gray-400 dark:text-gray-500">
            Modifié {formatRelativeTime(draft.lastModifiedAt)}
          </span>
        </div>
        <p className="truncate text-theme-xs text-gray-400 dark:text-gray-500">
          {recipientsLine(draft)} · {draft.propertyName}
        </p>
      </Link>
      <button
        type="button"
        disabled={isDeleting}
        onClick={() => onDelete(draft.id)}
        aria-label={`Supprimer le brouillon ${draftTitle(draft)}`}
        className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-white/[0.05] hover:text-error-500 dark:hover:text-error-400 disabled:cursor-not-allowed disabled:opacity-60"
      >
        <TrashBinIcon className="h-4 w-4" />
      </button>
    </li>
  );
}
