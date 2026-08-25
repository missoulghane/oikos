import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useRecipientCandidates } from '@/features/messaging/hooks/useRecipientCandidates';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { RecipientCandidate } from '@/features/messaging/types/messaging.types';

const SEARCH_DEBOUNCE_MS = 300;

// Pseudo-candidate representing "everyone on this property" - selecting it
// is handled by the parent (NewConversationScreen), which routes the send
// to the broadcast endpoint instead of the group-conversation one.
export const EVERYONE_RECIPIENT: RecipientCandidate = {
  userId: '__everyone__',
  fullName: 'Toute la copropriété',
  roleLabel: 'Diffusion',
  unitNumbers: [],
  isStaff: false,
};

// Pseudo-candidate representing "the board" - picking it routes the send to
// the board-conversation endpoint (BOARD_PRIVATE). Audience is resolved
// server-side from the property's *current* staff roster at read time.
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
  canBroadcast?: boolean;
  canBoardPrivate?: boolean;
}

// Outlook "To:" style multi-select: selected recipients render as removable
// chips above the search input; tapping a search result adds a chip instead
// of navigating anywhere. True autocomplete - the candidate list only
// appears once the user has typed something (see useRecipientCandidates'
// enabled guard).
export function RecipientPicker({
  propertyId,
  value,
  onChange,
  disabled = false,
  canBroadcast = false,
  canBoardPrivate = false,
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
  const availableCandidates = (recipients.data ?? []).filter((candidate) => !selectedIds.has(candidate.userId));
  const isEveryoneSelected = value.some(isEveryoneRecipient);
  const isBoardSelected = value.some(isBoardRecipient);
  const isPseudoRecipientSelected = isEveryoneSelected || isBoardSelected;

  function addRecipient(candidate: RecipientCandidate) {
    onChange([...value, candidate]);
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

  return (
    <View style={styles.container}>
      {value.length > 0 && (
        <View style={styles.chipsRow}>
          {value.map((recipient) => (
            <View key={recipient.userId} style={styles.chip}>
              <Text style={styles.chipText}>{candidateNameLabel(recipient)}</Text>
              <Pressable
                disabled={disabled}
                onPress={() => removeRecipient(recipient.userId)}
                accessibilityLabel={`Retirer ${recipient.fullName}`}
                style={styles.chipRemove}
              >
                <Text style={styles.chipRemoveText}>×</Text>
              </Pressable>
            </View>
          ))}
        </View>
      )}

      {(canBroadcast || canBoardPrivate) && !isPseudoRecipientSelected && value.length === 0 && (
        <View style={styles.pseudoRow}>
          {canBroadcast && (
            <Pressable disabled={disabled} onPress={selectEveryone} style={styles.pseudoButton}>
              <Text style={styles.pseudoButtonText}>Envoyer à toute la copropriété</Text>
            </Pressable>
          )}
          {canBoardPrivate && (
            <Pressable disabled={disabled} onPress={selectBoard} style={styles.pseudoButton}>
              <Text style={styles.pseudoButtonText}>Écrire au conseil (fil privé)</Text>
            </Pressable>
          )}
        </View>
      )}

      {!isPseudoRecipientSelected && (
        <>
          <Input
            label="Ajouter un destinataire"
            placeholder="Nom du copropriétaire, gérant…"
            value={searchInput}
            editable={!disabled}
            onChangeText={setSearchInput}
          />
          {isSearching && recipients.isLoading && <Loader label="Recherche…" />}
          {isSearching && recipients.isError && <Alert message={getErrorMessage(recipients.error)} />}
          {isSearching && recipients.data && availableCandidates.length === 0 && (
            <Text style={styles.emptyText}>Aucun destinataire trouvé.</Text>
          )}
          {isSearching && availableCandidates.length > 0 && (
            <View style={styles.candidateList}>
              {availableCandidates.map((candidate) => (
                <Pressable
                  key={candidate.userId}
                  disabled={disabled}
                  onPress={() => addRecipient(candidate)}
                  style={styles.candidateRow}
                >
                  <Text style={styles.candidateName}>{candidateNameLabel(candidate)}</Text>
                  <Text style={styles.candidateRole}>{candidate.roleLabel}</Text>
                </Pressable>
              ))}
            </View>
          )}
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 12,
  },
  chipsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    borderRadius: 999,
    backgroundColor: colors.brand[50],
    paddingLeft: 12,
    paddingRight: 6,
    paddingVertical: 4,
  },
  chipText: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.brand[500],
  },
  chipRemove: {
    width: 20,
    height: 20,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  chipRemoveText: {
    fontSize: 16,
    lineHeight: 16,
    color: colors.brand[500],
  },
  pseudoRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  pseudoButton: {
    borderRadius: 999,
    borderWidth: 1,
    borderStyle: 'dashed',
    borderColor: colors.gray[300],
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  pseudoButtonText: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[600],
  },
  emptyText: {
    borderRadius: 8,
    borderWidth: 1,
    borderStyle: 'dashed',
    borderColor: colors.gray[200],
    paddingHorizontal: 12,
    paddingVertical: 8,
    fontSize: 14,
    color: colors.gray[400],
  },
  candidateList: {
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.gray[200],
  },
  candidateRow: {
    gap: 2,
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: colors.gray[100],
  },
  candidateName: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[900],
  },
  candidateRole: {
    fontSize: 13,
    color: colors.gray[500],
  },
});
