import { useEffect, useRef, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useCurrentUser, isBoardTierOnProperty, isManagerTierOnProperty, isOwnerOnProperty } from '@/features/identity/me';
import { useConversationMessages } from '@/features/messaging/hooks/useConversationMessages';
import { useMarkConversationRead } from '@/features/messaging/hooks/useMarkConversationRead';
import { MessageThreadItem } from '@/features/messaging/components/MessageThreadItem';
import { MessageComposer } from '@/features/messaging/components/MessageComposer';
import { participantsLine } from '@/features/messaging/components/ConversationListItem';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { MainStackParamList } from '@/app/navigation/MainNavigator';
import type { Message } from '@/features/messaging/types/messaging.types';

type Props = NativeStackScreenProps<MainStackParamList, 'Conversation'>;

// The reading pane. Opening a received message is read-only until the user
// explicitly taps "Répondre" - no reply-all/forward, a GROUP reply already
// goes to every participant and forwarding isn't a feature this app has.
export function ConversationScreen({ route, navigation }: Props) {
  const { conversation: summary } = route.params;
  const id = summary.id;
  const currentUser = useCurrentUser();

  const messages = useConversationMessages(id);
  const markRead = useMarkConversationRead(id);
  const [isReplying, setIsReplying] = useState(false);
  // "A :" label for messages the caller sent - same for every message in the
  // thread, since a GROUP/BOARD_PRIVATE conversation has a fixed participant
  // set and there's no per-message recipient list on the wire.
  const recipientLabel = summary.type === 'BROADCAST' ? 'Tous les propriétaires' : participantsLine(summary) ?? 'Vous';

  const lastMarkedReadConversationId = useRef<string | null>(null);
  useEffect(() => {
    if (lastMarkedReadConversationId.current !== id) {
      lastMarkedReadConversationId.current = id;
      // Fire-and-forget: marking read must not block rendering the thread,
      // and the backend upserts the read marker so a duplicate call is safe.
      markRead.mutate();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const isStaffOnThisProperty = Boolean(
    currentUser.data &&
      (isBoardTierOnProperty(currentUser.data, summary.propertyId) ||
        isManagerTierOnProperty(currentUser.data, summary.propertyId)),
  );

  // Any participant may reply in a GROUP conversation (already enforced by
  // the backend for read access to even be here). A BROADCAST or
  // BOARD_PRIVATE thread is read-only for everyone except board/manager.
  const canReply =
    summary.type === 'GROUP' || ((summary.type === 'BROADCAST' || summary.type === 'BOARD_PRIVATE') && isStaffOnThisProperty);

  // Same "envoyer en tant que" rule as composing a new message: only a real
  // choice for a GROUP thread where the sender holds both roles on this
  // property - BOARD_PRIVATE/BROADCAST replies are always BOARD regardless,
  // the backend enforces it either way.
  const identityChoiceNeeded =
    summary.type === 'GROUP' &&
    Boolean(currentUser.data && isOwnerOnProperty(currentUser.data, summary.propertyId)) &&
    isStaffOnThisProperty;

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Pressable onPress={() => navigation.goBack()} accessibilityLabel="Retour à la liste des messages" style={styles.backButton}>
          <Text style={styles.backButtonText}>‹</Text>
        </Pressable>
      </View>

      <FlatList
        data={messages.data?.content ?? []}
        keyExtractor={(message) => message.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }: { item: Message }) => <MessageThreadItem message={item} recipientLabel={recipientLabel} />}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          <>
            {messages.isLoading && <Loader label="Chargement des messages…" />}
            {messages.isError && <Alert message={getErrorMessage(messages.error)} />}
          </>
        }
      />

      {canReply && (
        <View style={styles.replyBar}>
          {isReplying ? (
            <MessageComposer conversationId={id} identityChoiceNeeded={identityChoiceNeeded} />
          ) : (
            <Button variant="secondary" onPress={() => setIsReplying(true)} style={styles.replyButton}>
              Répondre
            </Button>
          )}
        </View>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.gray[50],
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.gray[100],
  },
  backButton: {
    width: 36,
    height: 36,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.gray[200],
    alignItems: 'center',
    justifyContent: 'center',
  },
  backButtonText: {
    fontSize: 20,
    color: colors.gray[500],
  },
  list: {
    padding: 16,
    flexGrow: 1,
  },
  separator: {
    height: 12,
  },
  replyBar: {
    borderTopWidth: 1,
    borderTopColor: colors.gray[100],
    padding: 12,
  },
  replyButton: {
    alignSelf: 'flex-start',
    borderRadius: 999,
  },
});
