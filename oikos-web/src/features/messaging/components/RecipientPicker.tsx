import { useEffect, useState } from 'react';
import { useRecipientCandidates } from '@/features/messaging/hooks/useRecipientCandidates';
import { useRecipientGroups } from '@/features/messaging/hooks/useRecipientGroups';
import { useProperty } from '@/features/property-mngt/properties/hooks/useProperty';
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
  unitNumbers: [],
  isStaff: false,
};

// Pseudo-candidate representing "the board" - like EVERYONE_RECIPIENT, no
// functional difference for the user between messaging a specific list of
// board members and messaging "the board" as a whole; picking it routes the
// send to the board-conversation endpoint (BOARD_PRIVATE) instead of the
// group-conversation one (see NewConversationPage). Audience is resolved
// server-side from the property's *current* staff roster at read time, never
// this fixed candidate - an ex-board-member loses access immediately even
// though this pseudo-candidate never changes.
export const BOARD_RECIPIENT: RecipientCandidate = {
  userId: '__board__',
  fullName: 'Le conseil syndical',
  roleLabel: 'Fil privé',
  unitNumbers: [],
  isStaff: true,
};

/** "Jean Dupont - 12B" (no lot suffix for a board/manager seat that owns nothing here). */
function candidateNameLabel(candidate: RecipientCandidate): string {
  return candidate.unitNumbers.length > 0
    ? `${candidate.fullName} - ${candidate.unitNumbers.join(', ')}`
    : candidate.fullName;
}

export function isEveryoneRecipient(candidate: RecipientCandidate): boolean {
  return candidate.userId === EVERYONE_RECIPIENT.userId;
}

export function isBoardRecipient(candidate: RecipientCandidate): boolean {
  return candidate.userId === BOARD_RECIPIENT.userId;
}

interface RecipientPickerProps {
  propertyId: string;
  value: RecipientCandidate[];
  onChange: (value: RecipientCandidate[]) => void;
  disabled?: boolean;
  /** Whether "Toute la copropriété" may be picked as a recipient (board/manager tier only). */
  canBroadcast?: boolean;
  /** Whether "Le bureau" (fil privé) may be picked as a recipient (board/manager tier only). */
  canBoardPrivate?: boolean;
  /** Les raccourcis « groupes ». Coupés là où l'on compose justement un groupe, sinon il s'offrirait lui-même. */
  showGroups?: boolean;
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
  canBoardPrivate = false,
  showGroups = true,
}: RecipientPickerProps) {
  const [searchInput, setSearchInput] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedSearch(searchInput.trim()), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [searchInput]);

  const isSearching = debouncedSearch.length > 0;
  const recipients = useRecipientCandidates(propertyId, debouncedSearch || undefined);
  // Every candidate belongs to this same property (the endpoint is scoped to
  // it) - fetched once here rather than per-candidate to name it alongside
  // the role: "Al Amal - Président" reads better than a bare "Président"
  // once messages start crossing several résidences in the same inbox.
  const property = useProperty(propertyId);
  const selectedIds = new Set(value.map((recipient) => recipient.userId));
  const availableCandidates = (recipients.data ?? []).filter(
    (candidate) => !selectedIds.has(candidate.userId),
  );
  // Les groupes de la copropriété : un carnet d'adresses, pas un destinataire.
  // Choisir un groupe ajoute ses membres en pastilles, comme si on les avait
  // saisis un par un - le message part aux personnes, et renommer le groupe
  // plus tard ne touche à rien de déjà envoyé.
  const groups = useRecipientGroups(showGroups ? propertyId : '');
  const isEveryoneSelected = value.some(isEveryoneRecipient);
  const isBoardSelected = value.some(isBoardRecipient);
  const isPseudoRecipientSelected = isEveryoneSelected || isBoardSelected;

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

  function selectBoard() {
    onChange([BOARD_RECIPIENT]);
    setSearchInput('');
    setDebouncedSearch('');
  }

  function addGroupMembers(members: { userId: string; fullName: string }[]) {
    const alreadyPicked = new Set(value.map((recipient) => recipient.userId));
    const added = members
      .filter((member) => !alreadyPicked.has(member.userId))
      .map((member) => ({
        userId: member.userId,
        fullName: member.fullName,
        roleLabel: '',
        unitNumbers: [],
        isStaff: false,
      }));
    onChange([...value, ...added]);
  }

  return (
    <div className="flex flex-col gap-3">
      {value.length > 0 && (
        <ul className="flex flex-wrap gap-2">
          {value.map((recipient) => (
            <li key={recipient.userId}>
              <span className="inline-flex min-h-8 items-center gap-1.5 rounded-full bg-brand-50 dark:bg-brand-500/[0.12] py-1 pl-3 pr-1.5 text-sm font-medium text-brand-700 dark:text-brand-400">
                {candidateNameLabel(recipient)}
                <button
                  type="button"
                  disabled={disabled}
                  onClick={() => removeRecipient(recipient.userId)}
                  aria-label={`Retirer ${recipient.fullName}`}
                  className="flex h-5 w-5 items-center justify-center rounded-full text-brand-500 dark:text-brand-400 hover:bg-brand-100 dark:hover:bg-brand-500/20 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  <CloseIcon className="h-3 w-3" />
                </button>
              </span>
            </li>
          ))}
        </ul>
      )}

      {(canBroadcast || canBoardPrivate) && !isPseudoRecipientSelected && value.length === 0 && (
        <div className="flex flex-wrap gap-2">
          {canBroadcast && (
            <button
              type="button"
              disabled={disabled}
              onClick={selectEveryone}
              className="inline-flex min-h-8 items-center self-start rounded-full border border-dashed border-gray-300 dark:border-gray-700 px-3 py-1 text-sm font-medium text-gray-600 dark:text-gray-400 hover:border-brand-300 hover:text-brand-600 dark:hover:text-brand-400 disabled:cursor-not-allowed disabled:opacity-60"
            >
              Envoyer à toute la copropriété
            </button>
          )}
          {canBoardPrivate && (
            <button
              type="button"
              disabled={disabled}
              onClick={selectBoard}
              className="inline-flex min-h-8 items-center self-start rounded-full border border-dashed border-gray-300 dark:border-gray-700 px-3 py-1 text-sm font-medium text-gray-600 dark:text-gray-400 hover:border-brand-300 hover:text-brand-600 dark:hover:text-brand-400 disabled:cursor-not-allowed disabled:opacity-60"
            >
              Écrire au conseil (fil privé)
            </button>
          )}
        </div>
      )}

      {showGroups && !isPseudoRecipientSelected && (groups.data?.length ?? 0) > 0 && (
        <div className="flex flex-col gap-1.5">
          <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Groupes</span>
          <div className="flex flex-wrap gap-2">
            {groups.data?.map((group) => (
              <button
                key={group.id}
                type="button"
                disabled={disabled}
                onClick={() => addGroupMembers(group.members)}
                title={group.members.map((member) => member.fullName).join(', ')}
                className="inline-flex min-h-8 items-center self-start rounded-full border border-dashed border-gray-300 dark:border-gray-700 px-3 py-1 text-sm font-medium text-gray-600 dark:text-gray-400 hover:border-brand-300 hover:text-brand-600 dark:hover:text-brand-400 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {group.name}
                <span className="ml-1.5 text-xs text-gray-400 dark:text-gray-500">({group.members.length})</span>
              </button>
            ))}
          </div>
        </div>
      )}

      {!isPseudoRecipientSelected && (
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
            <p className="rounded-lg border border-dashed border-gray-200 dark:border-gray-800 px-3 py-1.5 text-sm text-gray-400 dark:text-gray-500">
              Aucun destinataire trouvé.
            </p>
          )}
          {isSearching && availableCandidates.length > 0 && (
            <ul className="flex flex-col divide-y divide-gray-100 dark:divide-gray-800 rounded-lg border border-gray-200 dark:border-gray-800">
              {availableCandidates.map((candidate) => (
                <li key={candidate.userId}>
                  <button
                    type="button"
                    disabled={disabled}
                    onClick={() => addRecipient(candidate)}
                    className="flex min-h-11 w-full flex-col items-start gap-0.5 px-3 py-2 text-left hover:bg-gray-50 dark:hover:bg-white/[0.03] disabled:cursor-not-allowed disabled:opacity-60 sm:flex-row sm:items-center sm:justify-between"
                  >
                    <span className="text-sm font-medium text-gray-900 dark:text-white/90">
                      {candidateNameLabel(candidate)}
                    </span>
                    <span className="text-sm text-gray-500 dark:text-gray-400">
                      {property.data ? `${property.data.name} - ${candidate.roleLabel}` : candidate.roleLabel}
                    </span>
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
