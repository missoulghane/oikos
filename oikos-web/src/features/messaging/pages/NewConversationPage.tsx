import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useCurrentUser, isBoardTierOnProperty, isManagerTierOnProperty } from '@/features/identity/me';
import { useProperties } from '@/features/property-mngt/properties/hooks/useProperties';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { useStartConversation } from '@/features/messaging/hooks/useStartConversation';
import { useSendBroadcastMessage } from '@/features/messaging/hooks/useSendBroadcastMessage';
import { RecipientPicker } from '@/features/messaging/components/RecipientPicker';
import { sendMessageSchema, type SendMessageFormValues } from '@/features/messaging/schemas/sendMessageSchema';
import {
  startConversationSchema,
  type StartConversationFormValues,
} from '@/features/messaging/schemas/startConversationSchema';
import type { RecipientCandidate } from '@/features/messaging/types/messaging.types';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

// Outlook-style compose: pick recipients AND write the message body together,
// one "Envoyer" action - never a two-step "create an empty conversation,
// then go type into it afterwards". Sending a message never "starts a
// conversation" as a concept exposed to the user; it only becomes one later
// if someone replies (see ConversationListItem's "N messages" indicator).

// Large enough to fetch every managed property in one page, matching the
// picker pattern already used by PartiesPage (see PROPERTY_PICKER_SIZE there).
const PROPERTY_PICKER_SIZE = 100;

export function NewConversationPage() {
  const navigate = useNavigate();
  const currentUser = useCurrentUser();
  // GET /properties returns the properties this account manages (empty, not
  // an error, for a plain owner) - combined below with useMyUnits() (owned
  // units) to name every property in roleByProperty regardless of role.
  const properties = useProperties(0, PROPERTY_PICKER_SIZE);
  const myUnits = useMyUnits();
  const [selectedPropertyId, setSelectedPropertyId] = useState<string | null>(null);
  const [isBroadcasting, setIsBroadcasting] = useState(false);
  const [selectedRecipients, setSelectedRecipients] = useState<RecipientCandidate[]>([]);
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<StartConversationFormValues>({
    resolver: zodResolver(startConversationSchema),
    defaultValues: { subject: '', body: '' },
  });

  const user = currentUser.data;
  const propertyIds = user ? Object.keys(user.roleByProperty) : [];
  const effectivePropertyId = selectedPropertyId ?? (propertyIds.length === 1 ? propertyIds[0] : null);
  const startConversation = useStartConversation(effectivePropertyId ?? '');

  if (currentUser.isLoading) {
    return <Loader label="Chargement…" />;
  }
  if (currentUser.isError || !user) {
    return <Alert message={getErrorMessage(currentUser.error)} />;
  }

  const propertyNamesById = new Map<string, string>();
  (properties.data?.content ?? []).forEach((property) => propertyNamesById.set(property.id, property.name));
  (myUnits.data ?? []).forEach((unit) => propertyNamesById.set(unit.propertyId, unit.propertyName));

  const canBroadcast =
    effectivePropertyId !== null &&
    (isBoardTierOnProperty(user, effectivePropertyId) || isManagerTierOnProperty(user, effectivePropertyId));

  function onSubmit(values: StartConversationFormValues) {
    startConversation.mutate(
      {
        recipientUserIds: selectedRecipients.map((recipient) => recipient.userId),
        subject: values.subject,
        body: values.body,
      },
      {
        onSuccess: (result) => {
          reset({ subject: '', body: '' });
          navigate(`/messages/${result.conversationId}`);
        },
      },
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-gray-900">Nouveau message</h1>

      {!effectivePropertyId && (
        <div className="flex flex-col gap-2">
          <p className="text-sm text-gray-500">Choisissez une copropriété :</p>
          {propertyIds.length === 0 && (
            <Alert variant="warning" message="Vous n'êtes membre d'aucune copropriété." />
          )}
          <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
            {propertyIds.map((id) => (
              <li key={id}>
                <button
                  type="button"
                  onClick={() => {
                    setSelectedPropertyId(id);
                    setSelectedRecipients([]);
                  }}
                  className="flex min-h-11 w-full items-center px-3 py-2 text-left text-sm font-medium text-gray-900 hover:bg-gray-50"
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
          {propertyIds.length > 1 && (
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
            // message textarea is unaffected, Enter there just inserts a
            // newline and never submits a form on its own.
            onKeyDown={(e) => {
              if (e.key === 'Enter' && (e.target as HTMLElement).tagName !== 'TEXTAREA') {
                e.preventDefault();
              }
            }}
            className="flex flex-col gap-4"
            noValidate
          >
            {startConversation.isError && <Alert message={getErrorMessage(startConversation.error)} />}

            <RecipientPicker
              propertyId={effectivePropertyId}
              value={selectedRecipients}
              onChange={setSelectedRecipients}
              disabled={startConversation.isPending}
            />

            <div className="flex flex-col gap-1">
              <label htmlFor="new-message-subject" className="text-sm font-medium text-gray-700">
                Titre
              </label>
              <input
                id="new-message-subject"
                type="text"
                placeholder="Objet du message…"
                disabled={startConversation.isPending}
                className="min-h-11 rounded-lg border border-gray-300 bg-transparent px-3 py-2 text-base text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:outline-none focus:border-brand-300 focus:ring-3 focus:ring-brand-500/20 disabled:opacity-60"
                {...register('subject')}
              />
              {errors.subject && <p className="text-sm text-error-500">{errors.subject.message}</p>}
            </div>

            <div className="flex flex-col gap-1">
              <label htmlFor="new-message-body" className="text-sm font-medium text-gray-700">
                Message
              </label>
              <textarea
                id="new-message-body"
                rows={4}
                placeholder="Écrivez votre message…"
                disabled={startConversation.isPending}
                className="min-h-24 rounded-lg border border-gray-300 bg-transparent px-3 py-2 text-base text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:outline-none focus:border-brand-300 focus:ring-3 focus:ring-brand-500/20 disabled:opacity-60"
                {...register('body')}
              />
              {errors.body && <p className="text-sm text-error-500">{errors.body.message}</p>}
            </div>

            <Button
              type="submit"
              disabled={selectedRecipients.length === 0}
              isLoading={startConversation.isPending}
              className="self-start"
            >
              Envoyer
            </Button>
          </form>

          {canBroadcast && (
            <div className="flex flex-col gap-3 rounded-lg border border-gray-200 p-4">
              {!isBroadcasting ? (
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => setIsBroadcasting(true)}
                  className="self-start"
                >
                  Annoncer à toute la copropriété
                </Button>
              ) : (
                <BroadcastComposer propertyId={effectivePropertyId} onCancel={() => setIsBroadcasting(false)} />
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function BroadcastComposer({ propertyId, onCancel }: { propertyId: string; onCancel: () => void }) {
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SendMessageFormValues>({ resolver: zodResolver(sendMessageSchema) });
  const { mutate, isPending, isError, error } = useSendBroadcastMessage(propertyId);

  function onSubmit(values: SendMessageFormValues) {
    mutate(values, { onSuccess: (result) => navigate(`/messages/${result.conversationId}`) });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3" noValidate>
      {isError && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-1">
        <label htmlFor="broadcast-body" className="text-sm font-medium text-gray-700">
          Message à toute la copropriété
        </label>
        <textarea
          id="broadcast-body"
          rows={4}
          disabled={isPending}
          className="min-h-24 rounded-lg border border-gray-300 bg-transparent px-3 py-2 text-base text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:outline-none focus:border-brand-300 focus:ring-3 focus:ring-brand-500/20 disabled:opacity-60"
          {...register('body')}
        />
        {errors.body && <p className="text-sm text-error-500">{errors.body.message}</p>}
      </div>
      <div className="flex gap-2">
        <Button type="submit" isLoading={isPending}>
          Envoyer l'annonce
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel} disabled={isPending}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
