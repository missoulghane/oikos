import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useSendMessage } from '@/features/messaging/hooks/useSendMessage';
import { sendMessageSchema, type SendMessageFormValues } from '@/features/messaging/schemas/sendMessageSchema';

export function MessageComposer({ conversationId }: { conversationId: string }) {
  const {
    register,
    handleSubmit,
    getValues,
    reset,
    formState: { errors },
  } = useForm<SendMessageFormValues>({ resolver: zodResolver(sendMessageSchema), defaultValues: { body: '' } });
  const { mutate, isPending, isError, error } = useSendMessage(conversationId);

  function onSubmit(values: SendMessageFormValues) {
    mutate(values, { onSuccess: () => reset({ body: '' }) });
  }

  function handleRetry() {
    // No optimistic UI in this app - the draft stays in the input until the
    // mutation actually succeeds, so retrying just resubmits the same text.
    mutate({ body: getValues('body') }, { onSuccess: () => reset({ body: '' }) });
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
