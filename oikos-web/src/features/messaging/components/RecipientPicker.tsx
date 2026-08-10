import { useEffect, useState } from 'react';
import { useRecipientCandidates } from '@/features/messaging/hooks/useRecipientCandidates';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { CloseIcon } from '@/shared/icons';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { RecipientCandidate } from '@/features/messaging/types/messaging.types';

const SEARCH_DEBOUNCE_MS = 300;

// Pseudo-candidate representing "everyone on this property" - there's no
// functional difference for the user between messaging a specific list of
// people and messaging the whole copropriété, it's just a recipient list
// that happens to mean everyone. Selecting it is handled by the parent
// (NewConversationPage), which routes the send to the broadcast endpoint
// instead of the group-conversation one - the backend still models
// BROADCAST as a structurally distinct, recipient-less channel, but that
// split never surfaces in this UI.
export const EVERYONE_RECIPIENT: RecipientCandidate = {
  userId: '__everyone__',
  fullName: 'Toute la copropriété',
  roleLabel: 'Diffusion',
};

export function isEveryoneRecipient(candidate: RecipientCandidate): boolean {
  return candidate.userId === EVERYONE_RECIPIENT.userId;
}

interface RecipientPickerProps {
  propertyId: string;
  value: RecipientCandidate[];
  onChange: (value: RecipientCandidate[]) => void;
  disabled?: boolean;
  /** Whether "Toute la copropriété" may be picked as a recipient (board/manager tier only). */
  canBroadcast?: boolean;
}

// Outlook "To:" style multi-select: selected recipients render as removable
// chips above the search input; clicking a search result adds a chip instead
// of navigating anywhere - starting the conversation is a separate explicit
// action driven by the parent (see NewConversationPage's "Créer la
// conversation" button).
//
// True autocomplete: the candidate list is never dumped automatically - it
// only appears once the user has typed something, and disappears again as
// soon as the field is cleared. Nothing is fetched (see
// useRecipientCandidates' enabled guard) or rendered before that.
export function RecipientPicker({
  propertyId,
  value,
  onChange,
  disabled = false,
  canBroadcast = false,
}: RecipientPickerProps) {
  const [searchInput, setSearchInput] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedSearch(searchInput.trim()), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [searchInput]);

  const isSearching = debouncedSearch.length > 0;
  const recipients = useRecipientCandidates(propertyId, debouncedSearch || undefined);
  const selectedIds = new Set(value.map((recipient) => recipient.userId));
  const availableCandidates = (recipients.data ?? []).filter(
    (candidate) => !selectedIds.has(candidate.userId),
  );
  const isEveryoneSelected = value.some(isEveryoneRecipient);

  function addRecipient(candidate: RecipientCandidate) {
    onChange([...value, candidate]);
    // Once a recipient is picked, the search is done with - clearing it
    // avoids leaving a stale query and a now-irrelevant results list sitting
    // open under the freshly added chip.
    setSearchInput('');
    setDebouncedSearch('');
  }

  function removeRecipient(userId: string) {
    onChange(value.filter((recipient) => recipient.userId !== userId));
  }

  function selectEveryone() {
    onChange([EVERYONE_RECIPIENT]);
    setSearchInput('');
    setDebouncedSearch('');
  }

  return (
    <div className="flex flex-col gap-3">
      {value.length > 0 && (
        <ul className="flex flex-wrap gap-2">
          {value.map((recipient) => (
            <li key={recipient.userId}>
              <span className="inline-flex min-h-8 items-center gap-1.5 rounded-full bg-brand-50 py-1 pl-3 pr-1.5 text-sm font-medium text-brand-700">
                {recipient.fullName}
                <button
                  type="button"
                  disabled={disabled}
                  onClick={() => removeRecipient(recipient.userId)}
                  aria-label={`Retirer ${recipient.fullName}`}
                  className="flex h-5 w-5 items-center justify-center rounded-full text-brand-500 hover:bg-brand-100 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  <CloseIcon className="h-3 w-3" />
                </button>
              </span>
            </li>
          ))}
        </ul>
      )}

      {canBroadcast && !isEveryoneSelected && value.length === 0 && (
        <button
          type="button"
          disabled={disabled}
          onClick={selectEveryone}
          className="inline-flex min-h-8 items-center self-start rounded-full border border-dashed border-gray-300 px-3 py-1 text-sm font-medium text-gray-600 hover:border-brand-300 hover:text-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
        >
          Envoyer à toute la copropriété
        </button>
      )}

      {!isEveryoneSelected && (
        <>
          <Input
            id="recipient-search"
            label="Ajouter un destinataire"
            placeholder="Nom du copropriétaire, gérant…"
            value={searchInput}
            disabled={disabled}
            onChange={(e) => setSearchInput(e.target.value)}
          />
          {isSearching && recipients.isLoading && <Loader label="Recherche…" />}
          {isSearching && recipients.isError && <Alert message={getErrorMessage(recipients.error)} />}
          {isSearching && recipients.data && availableCandidates.length === 0 && (
            <p className="rounded-lg border border-dashed border-gray-200 px-3 py-1.5 text-sm text-gray-400">
              Aucun destinataire trouvé.
            </p>
          )}
          {isSearching && availableCandidates.length > 0 && (
            <ul className="flex flex-col divide-y divide-gray-100 rounded-lg border border-gray-200">
              {availableCandidates.map((candidate) => (
                <li key={candidate.userId}>
                  <button
                    type="button"
                    disabled={disabled}
                    onClick={() => addRecipient(candidate)}
                    className="flex min-h-11 w-full flex-col items-start gap-0.5 px-3 py-2 text-left hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-60 sm:flex-row sm:items-center sm:justify-between"
                  >
                    <span className="text-sm font-medium text-gray-900">{candidate.fullName}</span>
                    <span className="text-sm text-gray-500">{candidate.roleLabel}</span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </div>
  );
}
