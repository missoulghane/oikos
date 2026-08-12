import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useSendMessage } from '@/features/messaging/hooks/useSendMessage';
import { sendMessageSchema, type SendMessageFormValues } from '@/features/messaging/schemas/sendMessageSchema';
import type { SenderIdentity } from '@/features/messaging/types/messaging.types';

interface MessageComposerProps {
  conversationId: string;
  /** Whether the sender holds both OWNER and BOARD on this thread's property - only then is "envoyer en tant que" a real choice. */
  identityChoiceNeeded?: boolean;
  /** Preselected from the active space (useEffectiveSpace) when the choice is offered. */
  defaultIdentity?: SenderIdentity;
}

export function MessageComposer({ conversationId, identityChoiceNeeded = false, defaultIdentity }: MessageComposerProps) {
  const [identityOverride, setIdentityOverride] = useState<SenderIdentity | null>(null);
  const selectedIdentity = identityOverride ?? defaultIdentity;
  const {
    register,
    handleSubmit,
    getValues,
    reset,
    formState: { errors },
  } = useForm<SendMessageFormValues>({ resolver: zodResolver(sendMessageSchema), defaultValues: { body: '' } });
  const { mutate, isPending, isError, error } = useSendMessage(conversationId);

  function onSubmit(values: SendMessageFormValues) {
    mutate(
      { ...values, senderIdentity: identityChoiceNeeded ? selectedIdentity : undefined },
      { onSuccess: () => reset({ body: '' }) },
    );
  }

  function handleRetry() {
    // No optimistic UI in this app - the draft stays in the input until the
    // mutation actually succeeds, so retrying just resubmits the same text.
    mutate(
      { body: getValues('body'), senderIdentity: identityChoiceNeeded ? selectedIdentity : undefined },
      { onSuccess: () => reset({ body: '' }) },
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-2" noValidate>
      {isError && (
        <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <Alert message={getErrorMessage(error)} />
          <Button
            type="button"
            variant="secondary"
            onClick={handleRetry}
            disabled={isPending}
            className="self-start sm:self-auto"
          >
            Réessayer
          </Button>
        </div>
      )}
      {identityChoiceNeeded && (
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-500 dark:text-gray-400">Répondre en tant que</span>
          <div role="group" aria-label="Répondre en tant que" className="flex rounded-lg border border-gray-200 p-0.5 dark:border-gray-800">
            {(['OWNER', 'BOARD'] as const).map((identity) => (
              <button
                key={identity}
                type="button"
                disabled={isPending}
                aria-pressed={selectedIdentity === identity}
                onClick={() => setIdentityOverride(identity)}
                className={`rounded-md px-2.5 py-1 text-xs font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${
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
      <div className="flex items-end gap-2">
        <div className="flex-1">
          <label htmlFor="message-body" className="sr-only">
            Message
          </label>
          <textarea
            id="message-body"
            rows={2}
            placeholder="Écrivez un message…"
            disabled={isPending}
            // Only rendered once the user explicitly clicks "Répondre"
            // (ConversationPage), never on initial page load.
            autoFocus
            className="w-full resize-none rounded-lg border border-gray-300 dark:border-gray-700 bg-transparent px-3 py-2 text-base text-gray-800 dark:text-white/90 shadow-theme-xs placeholder:text-gray-400 dark:placeholder:text-white/30 focus:outline-none focus:border-brand-300 focus:ring-3 focus:ring-brand-500/20 disabled:opacity-60"
            {...register('body')}
          />
          {errors.body && <p className="mt-1 text-sm text-error-500 dark:text-error-400">{errors.body.message}</p>}
        </div>
        <Button type="submit" isLoading={isPending}>
          Envoyer
        </Button>
      </div>
    </form>
  );
}
