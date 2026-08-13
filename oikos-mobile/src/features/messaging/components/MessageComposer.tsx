import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { StyleSheet, Text, View } from 'react-native';
import { MessageBodyEditor } from '@/features/messaging/components/MessageBodyEditor';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useSendMessage } from '@/features/messaging/hooks/useSendMessage';
import { SenderIdentityToggle } from '@/features/messaging/components/SenderIdentityToggle';
import { sendMessageSchema, type SendMessageFormValues } from '@/features/messaging/schemas/sendMessageSchema';
import { colors } from '@/shared/theme/colors';
import type { SenderIdentity } from '@/features/messaging/types/messaging.types';

interface MessageComposerProps {
  conversationId: string;
  /** Whether the sender holds both OWNER and BOARD on this thread's property - only then is "envoyer en tant que" a real choice. */
  identityChoiceNeeded?: boolean;
}

// No `defaultIdentity` prop, unlike oikos-web: there is no "active space"
// concept on mobile to preselect from (see PLAN.md) - when a choice is
// offered, it simply starts on OWNER and the sender picks explicitly.
export function MessageComposer({ conversationId, identityChoiceNeeded = false }: MessageComposerProps) {
  const [selectedIdentity, setSelectedIdentity] = useState<SenderIdentity>('OWNER');
  const {
    control,
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
    mutate(
      { body: getValues('body'), senderIdentity: identityChoiceNeeded ? selectedIdentity : undefined },
      { onSuccess: () => reset({ body: '' }) },
    );
  }

  return (
    <View style={styles.form}>
      {isError && (
        <View style={styles.errorRow}>
          <Alert message={getErrorMessage(error)} />
          <Button variant="secondary" onPress={handleRetry} disabled={isPending} style={styles.retryButton}>
            Réessayer
          </Button>
        </View>
      )}
      {identityChoiceNeeded && (
        <View style={styles.identityRow}>
          <Text style={styles.identityLabel}>Répondre en tant que</Text>
          <SenderIdentityToggle value={selectedIdentity} onChange={setSelectedIdentity} disabled={isPending} />
        </View>
      )}
      <View style={styles.inputRow}>
        <MessageBodyEditor
          control={control}
          name="body"
          label="Message"
          placeholder="Écrivez un message…"
          numberOfLines={2}
          editable={!isPending}
          errorMessage={errors.body?.message}
        />
        <Button onPress={handleSubmit(onSubmit)} isLoading={isPending} style={styles.sendButton}>
          Envoyer
        </Button>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  form: {
    gap: 8,
  },
  errorRow: {
    gap: 8,
  },
  retryButton: {
    alignSelf: 'flex-start',
  },
  identityRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  identityLabel: {
    fontSize: 14,
    color: colors.gray[500],
  },
  inputRow: {
    gap: 8,
  },
  sendButton: {
    alignSelf: 'flex-end',
  },
});
