import { useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useCurrentUser, isBoardTierOnProperty, isManagerTierOnProperty, isOwnerOnProperty } from '@/features/identity/me';
import { useEffectiveSpace, spaceQuerySuffix } from '@/shared/hooks/useEffectiveSpace';
import { useProperties } from '@/features/property-mngt/properties/hooks/useProperties';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
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
import {
  startConversationSchema,
  type StartConversationFormValues,
} from '@/features/messaging/schemas/startConversationSchema';
import { MessageBodyEditor } from '@/features/messaging/components/MessageBodyEditor';
import type { QuillEditorHandle } from '@/shared/components/RichText/QuillEditor';
import { BOX_PATH } from '@/features/messaging/utils/boxPath';
import type {
  RecipientCandidate,
  SaveMessageDraftPayload,
  SenderIdentity,
} from '@/features/messaging/types/messaging.types';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

// Outlook-style compose: pick recipients AND write the message body together,
// one "Envoyer" action - never a two-step "create an empty conversation,
// then go type into it afterwards". Sending a message never "starts a
// conversation" as a concept exposed to the user; it only becomes one later
// if someone replies (see ConversationListItem's "N messages" indicator).
//
// There is no separate "broadcast" composer: messaging the whole
// copropriété is just this same form with "Toute la copropriété" picked as
// the recipient (see RecipientPicker) - board/manager tiers only. Under the
// hood that still routes to the distinct broadcast endpoint (the backend
// models BROADCAST as a recipient-less, per-property singleton channel,
// structurally unlike a GROUP conversation), but that split never surfaces
// to the user as a different flow.
//
// This same form doubles as the draft editor: ?draftId= prefills it (see the
// prefill effect below) and "Envoyer" then goes through useSendDraft instead
// of starting a fresh conversation directly - the backend re-validates and
// deletes the draft atomically. "Enregistrer comme brouillon" bypasses the
// send-time schema entirely (getValues(), not handleSubmit) since a draft is
// explicitly allowed to be incomplete.

// Large enough to fetch every managed property in one page, matching the
// picker pattern already used by PartiesPage (see PROPERTY_PICKER_SIZE there).
const PROPERTY_PICKER_SIZE = 100;

function toDraftPayload(
  recipients: RecipientCandidate[],
  isEveryoneSelected: boolean,
  subject: string,
  body: string,
): SaveMessageDraftPayload {
  return {
    recipientUserIds: isEveryoneSelected ? [] : recipients.map((recipient) => recipient.userId),
    broadcast: isEveryoneSelected,
    subject: subject.trim() === '' ? null : subject,
    body: body.trim() === '' ? null : body,
  };
}

export function NewConversationPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const draftIdFromUrl = searchParams.get('draftId') ?? undefined;
  const currentUser = useCurrentUser();
  // GET /properties returns the properties this account manages (empty, not
  // an error, for a plain owner) - combined below with useMyUnits() (owned
  // units) to name every property in roleByProperty regardless of role.
  const properties = useProperties(0, PROPERTY_PICKER_SIZE);
  const myUnits = useMyUnits();
  const [selectedPropertyId, setSelectedPropertyId] = useState<string | null>(null);
  const [selectedRecipients, setSelectedRecipients] = useState<RecipientCandidate[]>([]);
  // Explicit override for "envoyer en tant que" - null means "follow the
  // active space" (see identitySentAs below), so switching space before
  // composing keeps defaulting correctly without this ever going stale.
  const [identityOverride, setIdentityOverride] = useState<SenderIdentity | null>(null);
  // Which of the sole selected recipient's units this thread concerns
  // (see RecipientCandidate.unitNumbers) - only meaningful while that
  // recipient is actually multi-lot (see concernsUnitChoiceAvailable below),
  // never reset explicitly on recipient changes since it's simply ignored
  // (not sent) once the condition stops holding.
  const [concernsUnit, setConcernsUnit] = useState<string | null>(null);
  const effectiveSpace = useEffectiveSpace();
  // Tracks which draft (if any) this compose session is attached to: seeded
  // from ?draftId=, then updated once "Enregistrer comme brouillon" creates a
  // fresh one, so a second save in the same session updates it instead of
  // creating a duplicate.
  const [currentDraftId, setCurrentDraftId] = useState<string | undefined>(draftIdFromUrl);
  // Sending an already-saved draft silently re-saves it first (see
  // onSubmit) via the same updateDraft mutation "Enregistrer comme
  // brouillon" uses - this flag is only there so the loading spinner lands
  // on "Envoyer" during that implicit save, not on "Enregistrer".
  const [isSendingViaDraft, setIsSendingViaDraft] = useState(false);
  const editorRef = useRef<QuillEditorHandle>(null);
  const {
    register,
    control,
    handleSubmit,
    reset,
    getValues,
    formState: { errors },
  } = useForm<StartConversationFormValues>({
    resolver: zodResolver(startConversationSchema),
    defaultValues: { subject: '', body: '' },
  });

  const draft = useDraft(draftIdFromUrl);
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
      // Quill is uncontrolled after mount (see QuillEditor's doc comment) -
      // reset() alone only seeds RHF's own state, not what's visibly shown.
      editorRef.current?.setHtml(draft.data.body ?? '');
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

  if (currentUser.isLoading || (draftIdFromUrl && draft.isLoading)) {
    return <Loader label="Chargement…" />;
  }
  if (currentUser.isError || !user) {
    return <Alert message={getErrorMessage(currentUser.error)} />;
  }
  if (draftIdFromUrl && draft.isError) {
    return <Alert message={getErrorMessage(draft.error)} />;
  }

  const propertyNamesById = new Map<string, string>();
  (properties.data?.content ?? []).forEach((property) => propertyNamesById.set(property.id, property.name));
  (myUnits.data ?? []).forEach((unit) => propertyNamesById.set(unit.propertyId, unit.propertyName));

  // Same population for both: any staff member (board or manager tier) may
  // both broadcast to everyone and start a private board thread.
  const isStaffOnProperty =
    effectivePropertyId !== null &&
    (isBoardTierOnProperty(user, effectivePropertyId) || isManagerTierOnProperty(user, effectivePropertyId));
  const canBroadcast = isStaffOnProperty;
  const canBoardPrivate = isStaffOnProperty;
  const isEveryoneSelected = selectedRecipients.some(isEveryoneRecipient);
  const isBoardSelected = selectedRecipients.some(isBoardRecipient);

  // "Envoyer en tant que" only means something for a GROUP message from an
  // account holding both roles on this property (case 2/4/6/7) - a
  // single-role sender is never asked a question with one answer, and
  // BOARD_PRIVATE/BROADCAST are always sent as BOARD regardless.
  const identityChoiceNeeded =
    !isEveryoneSelected &&
    !isBoardSelected &&
    effectivePropertyId !== null &&
    isOwnerOnProperty(user, effectivePropertyId) &&
    isStaffOnProperty;
  const defaultIdentity: SenderIdentity | undefined =
    effectiveSpace.kind === 'board' && effectiveSpace.propertyId === effectivePropertyId
      ? 'BOARD'
      : effectiveSpace.kind === 'owner'
        ? 'OWNER'
        : undefined;
  const selectedIdentity = identityOverride ?? defaultIdentity;

  // "Concerne (facultatif)" only makes sense once composing to exactly one
  // real recipient (not "toute la copropriété"/"le bureau") who owns more
  // than one lot here - the exact ambiguity it exists to resolve (e.g.
  // contacting a co-owner about Appartement 3 specifically, not their other
  // lot). Re-derived from the sole recipient's own units on every render
  // rather than reset via an effect, so swapping recipients can never leave
  // a stale lot label from a previous, different recipient selected.
  const soleRecipient =
    !isEveryoneSelected && !isBoardSelected && selectedRecipients.length === 1 ? selectedRecipients[0] : null;
  const concernsUnitChoiceAvailable = soleRecipient !== null && soleRecipient.unitNumbers.length > 1;
  const effectiveConcernsUnit =
    concernsUnitChoiceAvailable && concernsUnit && soleRecipient!.unitNumbers.includes(concernsUnit)
      ? concernsUnit
      : null;

  const isSending =
    startConversation.isPending ||
    startBoardConversation.isPending ||
    sendBroadcastMessage.isPending ||
    sendDraft.isPending ||
    (isSendingViaDraft && updateDraft.isPending);
  const isSavingDraft = createDraft.isPending || (!isSendingViaDraft && updateDraft.isPending);
  const sendError = startConversation.error ?? startBoardConversation.error ?? sendBroadcastMessage.error ?? sendDraft.error;
  const saveDraftError = createDraft.error ?? updateDraft.error;

  function onSubmit(values: StartConversationFormValues) {
    const onSuccess = (result: { conversationId: string }) => {
      // No editorRef.current.setHtml('') here (unlike the draft-prefill
      // effect above) - this navigates away immediately, so the page (and
      // the editor) unmounts before there'd be anything to visibly clear.
      reset({ subject: '', body: '' });
      setSelectedRecipients([]);
      // Lands on the *sent* thread (BOX_PATH.SENT), not reception: this
      // conversation only has the message the caller themselves just wrote,
      // and "Envoyé" is defined as "the caller has sent at least one message
      // in it" (see ConversationBox) - it doesn't belong under "Réception"
      // until someone else replies. Landing on /messages/reception/:id here
      // used to leave ConversationPage's conversationList.find(...) lookup
      // empty (the brand-new conversation isn't in the Réception box's list),
      // which silently broke everything derived from `summary` (the "De :/A :"
      // line, "Répondre" visibility, "envoyer en tant que").
      //
      // Carries the active space along so landing here doesn't silently drop
      // the viewer back into owner (see spaceQuerySuffix).
      navigate(`${BOX_PATH.SENT}/${result.conversationId}${spaceQuerySuffix(effectiveSpace)}`);
    };

    if (currentDraftId) {
      // The draft row on the server only has whatever was last explicitly
      // saved (see onSaveDraft) - persist the current form edits first, or
      // "Envoyer" would silently send stale content instead of what's on
      // screen right now.
      setIsSendingViaDraft(true);
      const payload = toDraftPayload(selectedRecipients, isEveryoneSelected, values.subject, values.body);
      updateDraft.mutate(payload, {
        onSuccess: () =>
          sendDraft.mutate(undefined, { onSuccess, onSettled: () => setIsSendingViaDraft(false) }),
        onError: () => setIsSendingViaDraft(false),
      });
      return;
    }

    if (isEveryoneSelected) {
      sendBroadcastMessage.mutate({ body: values.body }, { onSuccess });
      return;
    }

    if (isBoardSelected) {
      startBoardConversation.mutate({ subject: values.subject, body: values.body }, { onSuccess });
      return;
    }

    startConversation.mutate(
      {
        recipientUserIds: selectedRecipients.map((recipient) => recipient.userId),
        subject: values.subject,
        body: values.body,
        senderIdentity: identityChoiceNeeded ? selectedIdentity : undefined,
        concernsUnit: effectiveConcernsUnit ?? undefined,
      },
      { onSuccess },
    );
  }

  function onSaveDraft() {
    const values = getValues();
    const payload = toDraftPayload(selectedRecipients, isEveryoneSelected, values.subject, values.body);
    // Carries the active space along, same reason as onSubmit's onSuccess above.
    const draftsListHref = `/messages/drafts${spaceQuerySuffix(effectiveSpace)}`;

    if (currentDraftId) {
      updateDraft.mutate(payload, { onSuccess: () => navigate(draftsListHref) });
      return;
    }
    createDraft.mutate(payload, {
      onSuccess: (result) => {
        setCurrentDraftId(result.draftId);
        navigate(draftsListHref);
      },
    });
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Nouveau message</h1>

      {!effectivePropertyId && (
        <div className="flex flex-col gap-2">
          <p className="text-sm text-gray-500 dark:text-gray-400">Choisissez une copropriété :</p>
          {propertyIds.length === 0 && (
            <Alert variant="warning" message="Vous n'êtes membre d'aucune copropriété." />
          )}
          <ul className="flex flex-col divide-y divide-gray-200 dark:divide-gray-800 rounded-lg border border-gray-200 dark:border-gray-800">
            {propertyIds.map((id) => (
              <li key={id}>
                <button
                  type="button"
                  onClick={() => {
                    setSelectedPropertyId(id);
                    setSelectedRecipients([]);
                  }}
                  className="flex min-h-11 w-full items-center px-3 py-2 text-left text-sm font-medium text-gray-900 dark:text-white/90 hover:bg-gray-50 dark:hover:bg-white/[0.03]"
                >
                  {propertyNamesById.get(id) ?? id}
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}

      {effectivePropertyId && (
        <div className="flex flex-col gap-6">
          {propertyIds.length > 1 && !currentDraftId && (
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setSelectedPropertyId(null);
                setSelectedRecipients([]);
              }}
              className="self-start"
            >
              Changer de copropriété
            </Button>
          )}

          <form
            onSubmit={handleSubmit(onSubmit)}
            // Enter inside the recipient search field must not submit the
            // whole compose form (only the "Envoyer" button should) - the
            // message editor is unaffected, Enter there just inserts a
            // newline/paragraph break (via Quill's contenteditable) and
            // never submits a form on its own.
            onKeyDown={(e) => {
              const target = e.target as HTMLElement;
              if (e.key === 'Enter' && target.tagName !== 'TEXTAREA' && !target.closest('.ql-editor')) {
                e.preventDefault();
              }
            }}
            className="flex flex-col gap-4"
            noValidate
          >
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
              <div className="flex flex-col gap-1">
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Envoyer en tant que</span>
                <div
                  role="group"
                  aria-label="Envoyer en tant que"
                  className="flex w-fit rounded-lg border border-gray-200 p-0.5 dark:border-gray-800"
                >
                  {(['OWNER', 'BOARD'] as const).map((identity) => (
                    <button
                      key={identity}
                      type="button"
                      disabled={isSending || isSavingDraft}
                      aria-pressed={selectedIdentity === identity}
                      onClick={() => setIdentityOverride(identity)}
                      className={`rounded-md px-3 py-1.5 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${
                        selectedIdentity === identity
                          ? 'bg-brand-500 text-white'
                          : 'text-gray-600 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-white/[0.05]'
                      }`}
                    >
                      {identity === 'OWNER' ? 'Copropriétaire' : 'Membre du bureau'}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {concernsUnitChoiceAvailable && (
              <div className="flex flex-col gap-1">
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Concerne (facultatif)</span>
                <div role="group" aria-label="Concerne" className="flex flex-wrap gap-2">
                  <button
                    type="button"
                    disabled={isSending || isSavingDraft}
                    aria-pressed={effectiveConcernsUnit === null}
                    onClick={() => setConcernsUnit(null)}
                    className={`inline-flex min-h-8 items-center rounded-full border px-3 py-1 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${
                      effectiveConcernsUnit === null
                        ? 'border-brand-500 bg-brand-50 text-brand-700 dark:bg-brand-500/[0.12] dark:text-brand-400'
                        : 'border-gray-300 text-gray-600 hover:border-brand-300 hover:text-brand-600 dark:border-gray-700 dark:text-gray-400 dark:hover:text-brand-400'
                    }`}
                  >
                    Aucun lot
                  </button>
                  {soleRecipient!.unitNumbers.map((unitNumber) => (
                    <button
                      key={unitNumber}
                      type="button"
                      disabled={isSending || isSavingDraft}
                      aria-pressed={effectiveConcernsUnit === unitNumber}
                      onClick={() => setConcernsUnit(unitNumber)}
                      className={`inline-flex min-h-8 items-center rounded-full border px-3 py-1 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${
                        effectiveConcernsUnit === unitNumber
                          ? 'border-brand-500 bg-brand-50 text-brand-700 dark:bg-brand-500/[0.12] dark:text-brand-400'
                          : 'border-gray-300 text-gray-600 hover:border-brand-300 hover:text-brand-600 dark:border-gray-700 dark:text-gray-400 dark:hover:text-brand-400'
                      }`}
                    >
                      Lot {unitNumber}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <div className="flex flex-col gap-1">
              <label htmlFor="new-message-subject" className="text-sm font-medium text-gray-700 dark:text-gray-300">
                Titre
              </label>
              <input
                id="new-message-subject"
                type="text"
                placeholder="Objet du message…"
                disabled={isSending || isSavingDraft}
                className="min-h-11 rounded-lg border border-gray-300 dark:border-gray-700 bg-transparent px-3 py-2 text-base text-gray-800 dark:text-white/90 shadow-theme-xs placeholder:text-gray-400 dark:placeholder:text-white/30 focus:outline-none focus:border-brand-300 focus:ring-3 focus:ring-brand-500/20 disabled:opacity-60"
                {...register('subject')}
              />
              {errors.subject && <p className="text-sm text-error-500 dark:text-error-400">{errors.subject.message}</p>}
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Message</span>
              <MessageBodyEditor
                ref={editorRef}
                control={control}
                name="body"
                ariaLabel="Message"
                placeholder="Écrivez votre message…"
                disabled={isSending || isSavingDraft}
                error={errors.body?.message}
                minHeight={140}
              />
            </div>

            <div className="flex items-center gap-2">
              <Button type="submit" disabled={selectedRecipients.length === 0} isLoading={isSending}>
                Envoyer
              </Button>
              <Button
                type="button"
                variant="secondary"
                // Un brouillon "bureau" n'existe pas encore côté serveur (le
                // fil privé est toujours envoyé directement, jamais mis en
                // attente) - voir SendMessageDraftService.
                disabled={isSending || isBoardSelected}
                isLoading={isSavingDraft}
                onClick={onSaveDraft}
              >
                Enregistrer comme brouillon
              </Button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
