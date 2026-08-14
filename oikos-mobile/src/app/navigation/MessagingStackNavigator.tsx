import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { ConversationListScreen, ConversationScreen, NewConversationScreen, DraftsListScreen } from '@/features/messaging';
import { BOX_LABEL } from '@/features/messaging/utils/boxPath';
import { NotificationBell } from '@/shared/components/NotificationBell/NotificationBell';
import type { ConversationBox, ConversationSummary } from '@/features/messaging/types/messaging.types';

export type MessagingStackParamList = {
  ConversationList: undefined;
  // Same pattern as MyUnitDetail: the summary is already in memory from
  // whichever screen navigated here (list row, or freshly built after
  // sending - see NewConversationScreen's goToNewThread) - no dedicated
  // GET /conversations/:id in the wire contract to refetch it from.
  Conversation: { conversation: ConversationSummary; box: ConversationBox };
  NewConversation: { draftId?: string };
  Drafts: undefined;
};

const Stack = createNativeStackNavigator<MessagingStackParamList>();

export function MessagingStackNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: true }}>
      <Stack.Screen
        name="ConversationList"
        component={ConversationListScreen}
        options={{ title: 'Messagerie', headerRight: () => <NotificationBell /> }}
      />
      <Stack.Screen
        name="Conversation"
        component={ConversationScreen}
        options={({ route }) => ({ title: BOX_LABEL[route.params.box] })}
      />
      <Stack.Screen name="NewConversation" component={NewConversationScreen} options={{ title: 'Nouveau message' }} />
      <Stack.Screen name="Drafts" component={DraftsListScreen} options={{ title: 'Brouillons' }} />
    </Stack.Navigator>
  );
}
