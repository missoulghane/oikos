import { useEffect, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useSendMessage } from '@/features/messaging/hooks/useSendMessage';
import { sendMessageSchema, type SendMessageFormValues } from '@/features/messaging/schemas/sendMessageSchema';
import { MessageBodyEditor } from '@/features/messaging/components/MessageBodyEditor';
import type { QuillEditorHandle } from '@/shared/components/RichText/QuillEditor';
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
    control,
    handleSubmit,
    getValues,
    reset,
    formState: { errors },
  } = useForm<SendMessageFormValues>({ resolver: zodResolver(sendMessageSchema), defaultValues: { body: '' } });
  const { mutate, isPending, isError, error } = useSendMessage(conversationId);
  const editorRef = useRef<QuillEditorHandle>(null);
  // A ref can only be read/written outside of render (event handlers,
  // effects) - `<form onSubmit={handleSubmit(onSubmit)}>` calls `onSubmit`
  // (and therefore its onSuccess callback) reachably from render, so the
  // actual editorRef.current.setHtml('') call is deferred into the effect
  // below instead of happening directly inside clearBody.
  const [clearSignal, setClearSignal] = useState(0);
  useEffect(() => {
    if (clearSignal > 0) {
      editorRef.current?.setHtml('');
    }
  }, [clearSignal]);

  function clearBody() {
    // Quill is uncontrolled after mount (see QuillEditor's doc comment) -
    // reset() alone only clears RHF's own state, not what's visibly typed.
    reset({ body: '' });
    setClearSignal((signal) => signal + 1);
  }

  function onSubmit(values: SendMessageFormValues) {
    mutate({ ...values, senderIdentity: identityChoiceNeeded ? selectedIdentity : undefined }, { onSuccess: clearBody });
  }

  function handleRetry() {
    // No optimistic UI in this app - the draft stays in the input until the
    // mutation actually succeeds, so retrying just resubmits the same text.
    mutate(
      { body: getValues('body'), senderIdentity: identityChoiceNeeded ? selectedIdentity : undefined },
      { onSuccess: clearBody },
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
      <MessageBodyEditor
        ref={editorRef}
        control={control}
        name="body"
        ariaLabel="Message"
        placeholder="Écrivez un message…"
        disabled={isPending}
        // Only rendered once the user explicitly clicks "Répondre"
        // (ConversationPage), never on initial page load.
        autoFocus
        error={errors.body?.message}
        minHeight={72}
      />
      <Button type="submit" isLoading={isPending} className="self-end">
        Envoyer
      </Button>
    </form>
  );
}
