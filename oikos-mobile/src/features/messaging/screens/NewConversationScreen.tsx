import { useEffect, useRef, useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useCurrentUser, isBoardTierOnProperty, isManagerTierOnProperty, isOwnerOnProperty } from '@/features/identity/me';
import { useMyUnits } from '@/features/property-ownership/units';
import { useStartConversation } from '@/features/messaging/hooks/useStartConversation';
import { useStartBoardConversation } from '@/features/messaging/hooks/useStartBoardConversation';
import { useSendBroadcastMessage } from '@/features/messaging/hooks/useSendBroadcastMessage';
import { useDraft } from '@/features/messaging/hooks/useDraft';
import { useCreateDraft } from '@/features/messaging/hooks/useCreateDraft';
import { useUpdateDraft } from '@/features/messaging/hooks/useUpdateDraft';
import { useSendDraft } from '@/features/messaging/hooks/useSendDraft';
import {
  RecipientPicker,
  EVERYONE_RECIPIENT,
  isEveryoneRecipient,
  isBoardRecipient,
} from '@/features/messaging/components/RecipientPicker';
import { SenderIdentityToggle } from '@/features/messaging/components/SenderIdentityToggle';
import { MessageBodyEditor } from '@/features/messaging/components/MessageBodyEditor';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import {
  startConversationSchema,
  type StartConversationFormValues,
} from '@/features/messaging/schemas/startConversationSchema';
import type {
  ConversationSummary,
  ConversationType,
  RecipientCandidate,
  SaveMessageDraftPayload,
  SenderIdentity,
} from '@/features/messaging/types/messaging.types';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { MainStackParamList } from '@/app/navigation/MainNavigator';

type Props = NativeStackScreenProps<MainStackParamList, 'NewConversation'>;

// Outlook-style compose: pick recipients AND write the message body
// together, one "Envoyer" action. No separate "broadcast composer" - see
// RecipientPicker's EVERYONE/BOARD pseudo-candidates, which route the send
// to the distinct broadcast/board-conversation endpoints under the hood
// without that split ever surfacing as a different flow to the user.
//
// This same screen doubles as the draft editor: a `draftId` route param
// prefills it and "Envoyer" then goes through useSendDraft instead of
// starting a fresh conversation directly.
//
// Property names come from useMyUnits() only (properties owned), not
// oikos-web's useProperties() (property-mngt, out of scope on mobile) - a
// property held via a staff-only role (no owned unit) falls back to its raw
// id, same fallback web itself uses when a name can't be resolved.
export function NewConversationScreen({ route, navigation }: Props) {
  const draftIdFromParams = route.params?.draftId;
  const currentUser = useCurrentUser();
  const myUnits = useMyUnits();
  const [selectedPropertyId, setSelectedPropertyId] = useState<string | null>(null);
  const [selectedRecipients, setSelectedRecipients] = useState<RecipientCandidate[]>([]);
  const [identityOverride, setIdentityOverride] = useState<SenderIdentity>('OWNER');
  const [concernsUnit, setConcernsUnit] = useState<string | null>(null);
  const [currentDraftId, setCurrentDraftId] = useState<string | undefined>(draftIdFromParams);
  const [isSendingViaDraft, setIsSendingViaDraft] = useState(false);
  const {
    control,
    handleSubmit,
    reset,
    getValues,
    formState: { errors },
  } = useForm<StartConversationFormValues>({
    resolver: zodResolver(startConversationSchema),
    defaultValues: { subject: '', body: '' },
  });

  const draft = useDraft(draftIdFromParams);
  const hasPrefilledDraft = useRef(false);
  useEffect(() => {
    if (draft.data && !hasPrefilledDraft.current) {
      hasPrefilledDraft.current = true;
      setSelectedPropertyId(draft.data.propertyId);
      setSelectedRecipients(
        draft.data.broadcast
          ? [EVERYONE_RECIPIENT]
          : draft.data.recipients.map((recipient) => ({ ...recipient, roleLabel: '', unitNumbers: [], isStaff: false })),
      );
      reset({ subject: draft.data.subject ?? '', body: draft.data.body ?? '' });
    }
  }, [draft.data, reset]);

  const user = currentUser.data;
  const propertyIds = user ? Object.keys(user.roleByProperty) : [];
  const effectivePropertyId = selectedPropertyId ?? (propertyIds.length === 1 ? propertyIds[0] : null);
  const startConversation = useStartConversation(effectivePropertyId ?? '');
  const startBoardConversation = useStartBoardConversation(effectivePropertyId ?? '');
  const sendBroadcastMessage = useSendBroadcastMessage(effectivePropertyId ?? '');
  const createDraft = useCreateDraft(effectivePropertyId ?? '');
  const updateDraft = useUpdateDraft(currentDraftId ?? '');
  const sendDraft = useSendDraft(currentDraftId ?? '');

  if (currentUser.isLoading || (draftIdFromParams && draft.isLoading)) {
    return <Loader label="Chargement…" />;
  }
  if (currentUser.isError || !user) {
    return <Alert message={getErrorMessage(currentUser.error)} />;
  }
  if (draftIdFromParams && draft.isError) {
    return <Alert message={getErrorMessage(draft.error)} />;
  }

  const propertyNamesById = new Map<string, string>();
  (myUnits.data ?? []).forEach((unit) => propertyNamesById.set(unit.propertyId, unit.propertyName));

  const isStaffOnProperty =
    effectivePropertyId !== null &&
    (isBoardTierOnProperty(user, effectivePropertyId) || isManagerTierOnProperty(user, effectivePropertyId));
  const canBroadcast = isStaffOnProperty;
  const canBoardPrivate = isStaffOnProperty;
  const isEveryoneSelected = selectedRecipients.some(isEveryoneRecipient);
  const isBoardSelected = selectedRecipients.some(isBoardRecipient);

  const identityChoiceNeeded =
    !isEveryoneSelected &&
    !isBoardSelected &&
    effectivePropertyId !== null &&
    isOwnerOnProperty(user, effectivePropertyId) &&
    isStaffOnProperty;

  const soleRecipient =
    !isEveryoneSelected && !isBoardSelected && selectedRecipients.length === 1 ? selectedRecipients[0] : null;
  const concernsUnitChoiceAvailable = soleRecipient !== null && soleRecipient.unitNumbers.length > 1;
  const effectiveConcernsUnit =
    concernsUnitChoiceAvailable && concernsUnit && soleRecipient!.unitNumbers.includes(concernsUnit) ? concernsUnit : null;

  const isSending =
    startConversation.isPending ||
    startBoardConversation.isPending ||
    sendBroadcastMessage.isPending ||
    sendDraft.isPending ||
    (isSendingViaDraft && updateDraft.isPending);
  const isSavingDraft = createDraft.isPending || (!isSendingViaDraft && updateDraft.isPending);
  const sendError = startConversation.error ?? startBoardConversation.error ?? sendBroadcastMessage.error ?? sendDraft.error;
  const saveDraftError = createDraft.error ?? updateDraft.error;

  function toDraftPayload(subject: string, body: string): SaveMessageDraftPayload {
    return {
      recipientUserIds: isEveryoneSelected ? [] : selectedRecipients.map((recipient) => recipient.userId),
      broadcast: isEveryoneSelected,
      subject: subject.trim() === '' ? null : subject,
      body: body.trim() === '' ? null : body,
    };
  }

  function goToNewThread(type: ConversationType, subject: string, body: string) {
    // No dedicated GET /conversations/:id in the wire contract (see
    // oikos-web's own ConversationPage) - a summary good enough for
    // ConversationScreen's canReply/identityChoiceNeeded logic is built here
    // from what's already known, rather than re-fetched.
    const propertyId = effectivePropertyId as string;
    const conversation: ConversationSummary = {
      id: '',
      type,
      propertyId,
      propertyName: propertyNamesById.get(propertyId) ?? propertyId,
      subject: type === 'BROADCAST' ? null : subject,
      concernsUnit: effectiveConcernsUnit,
      participants: isEveryoneSelected || isBoardSelected ? [] : selectedRecipients.map((r) => ({ userId: r.userId, fullName: r.fullName })),
      lastMessagePreview: body,
      lastMessageAt: new Date().toISOString(),
      unreadCount: 0,
      messageCount: 1,
    };
    reset({ subject: '', body: '' });
    setSelectedRecipients([]);
    navigation.replace('ConversationList');
    navigation.navigate('Conversation', { conversation, box: 'RECEIVED' });
  }

  function onSubmit(values: StartConversationFormValues) {
    if (currentDraftId) {
      setIsSendingViaDraft(true);
      const payload = toDraftPayload(values.subject, values.body);
      updateDraft.mutate(payload, {
        onSuccess: () =>
          sendDraft.mutate(undefined, {
            onSuccess: () => goToNewThread(isEveryoneSelected ? 'BROADCAST' : 'GROUP', values.subject, values.body),
            onSettled: () => setIsSendingViaDraft(false),
          }),
        onError: () => setIsSendingViaDraft(false),
      });
      return;
    }

    if (isEveryoneSelected) {
      sendBroadcastMessage.mutate(
        { body: values.body },
        { onSuccess: () => goToNewThread('BROADCAST', values.subject, values.body) },
      );
      return;
    }

    if (isBoardSelected) {
      startBoardConversation.mutate(
        { subject: values.subject, body: values.body },
        { onSuccess: () => goToNewThread('BOARD_PRIVATE', values.subject, values.body) },
      );
      return;
    }

    startConversation.mutate(
      {
        recipientUserIds: selectedRecipients.map((recipient) => recipient.userId),
        subject: values.subject,
        body: values.body,
        senderIdentity: identityChoiceNeeded ? identityOverride : undefined,
        concernsUnit: effectiveConcernsUnit ?? undefined,
      },
      { onSuccess: () => goToNewThread('GROUP', values.subject, values.body) },
    );
  }

  function onSaveDraft() {
    const values = getValues();
    const payload = toDraftPayload(values.subject, values.body);

    if (currentDraftId) {
      updateDraft.mutate(payload, { onSuccess: () => navigation.navigate('Drafts') });
      return;
    }
    createDraft.mutate(payload, {
      onSuccess: (result) => {
        setCurrentDraftId(result.draftId);
        navigation.navigate('Drafts');
      },
    });
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Nouveau message</Text>

        {!effectivePropertyId && (
          <View style={styles.propertyPicker}>
            <Text style={styles.pickerLabel}>Choisissez une copropriété :</Text>
            {propertyIds.length === 0 && <Alert variant="warning" message="Vous n'êtes membre d'aucune copropriété." />}
            <View style={styles.propertyList}>
              {propertyIds.map((id) => (
                <Button
                  key={id}
                  variant="secondary"
                  onPress={() => {
                    setSelectedPropertyId(id);
                    setSelectedRecipients([]);
                  }}
                  style={styles.propertyButton}
                >
                  {propertyNamesById.get(id) ?? id}
                </Button>
              ))}
            </View>
          </View>
        )}

        {effectivePropertyId && (
          <View style={styles.form}>
            {propertyIds.length > 1 && !currentDraftId && (
              <Button
                variant="secondary"
                onPress={() => {
                  setSelectedPropertyId(null);
                  setSelectedRecipients([]);
                }}
                style={styles.changePropertyButton}
              >
                Changer de copropriété
              </Button>
            )}

            {sendError && <Alert message={getErrorMessage(sendError)} />}
            {saveDraftError && <Alert message={getErrorMessage(saveDraftError)} />}

            <RecipientPicker
              propertyId={effectivePropertyId}
              value={selectedRecipients}
              onChange={setSelectedRecipients}
              disabled={isSending || isSavingDraft}
              canBroadcast={canBroadcast}
              canBoardPrivate={canBoardPrivate}
            />

            {identityChoiceNeeded && (
              <View style={styles.fieldGroup}>
                <Text style={styles.fieldLabel}>Envoyer en tant que</Text>
                <SenderIdentityToggle value={identityOverride} onChange={setIdentityOverride} disabled={isSending || isSavingDraft} />
              </View>
            )}

            {concernsUnitChoiceAvailable && (
              <View style={styles.fieldGroup}>
                <Text style={styles.fieldLabel}>Concerne (facultatif)</Text>
                <View style={styles.chipsRow}>
                  <Button
                    variant={effectiveConcernsUnit === null ? 'primary' : 'secondary'}
                    disabled={isSending || isSavingDraft}
                    onPress={() => setConcernsUnit(null)}
                    style={styles.chipButton}
                  >
                    Aucun lot
                  </Button>
                  {soleRecipient!.unitNumbers.map((unitNumber) => (
                    <Button
                      key={unitNumber}
                      variant={effectiveConcernsUnit === unitNumber ? 'primary' : 'secondary'}
                      disabled={isSending || isSavingDraft}
                      onPress={() => setConcernsUnit(unitNumber)}
                      style={styles.chipButton}
                    >
                      {`Lot ${unitNumber}`}
                    </Button>
                  ))}
                </View>
              </View>
            )}

            <ControlledInput
              control={control}
              name="subject"
              label="Titre"
              placeholder="Objet du message…"
              editable={!(isSending || isSavingDraft)}
              errorMessage={errors.subject?.message}
            />

            <MessageBodyEditor
              control={control}
              name="body"
              label="Message"
              placeholder="Écrivez votre message…"
              numberOfLines={4}
              editable={!(isSending || isSavingDraft)}
              errorMessage={errors.body?.message}
            />

            <View style={styles.actionsRow}>
              <Button onPress={handleSubmit(onSubmit)} disabled={selectedRecipients.length === 0} isLoading={isSending}>
                Envoyer
              </Button>
              <Button
                variant="secondary"
                // Un brouillon "bureau" n'existe pas encore côté serveur (le
                // fil privé est toujours envoyé directement) - voir
                // SendMessageDraftService.
                disabled={isSending || isBoardSelected}
                isLoading={isSavingDraft}
                onPress={onSaveDraft}
              >
                Enregistrer comme brouillon
              </Button>
            </View>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.gray[50],
  },
  content: {
    padding: 16,
    gap: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  propertyPicker: {
    gap: 8,
  },
  pickerLabel: {
    fontSize: 14,
    color: colors.gray[500],
  },
  propertyList: {
    gap: 8,
  },
  propertyButton: {
    alignSelf: 'stretch',
  },
  form: {
    gap: 16,
  },
  changePropertyButton: {
    alignSelf: 'flex-start',
  },
  fieldGroup: {
    gap: 6,
  },
  fieldLabel: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[700],
  },
  chipsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  chipButton: {
    minHeight: 32,
    paddingVertical: 6,
    borderRadius: 999,
  },
  actionsRow: {
    gap: 8,
  },
});
