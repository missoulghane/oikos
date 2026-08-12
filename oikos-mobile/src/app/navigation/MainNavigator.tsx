import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { HomeScreen } from '@/app/navigation/HomeScreen';
import { ProfileScreen } from '@/features/identity/me';
import { InvitationLandingScreen, usePendingInvitationConsumer } from '@/features/identity/invitations';
import { MyUnitsScreen, MyUnitDetailScreen } from '@/features/property-ownership/units';
import { MyInstallmentsScreen } from '@/features/property-ownership/installments';
import { MyPaymentsScreen } from '@/features/property-ownership/payments';
import { MyMembershipRequestsScreen } from '@/features/property-ownership/membership-requests';
import { NotificationsListScreen, usePushTokenRegistration } from '@/features/notifications';
import { ConversationListScreen, ConversationScreen, NewConversationScreen, DraftsListScreen } from '@/features/messaging';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';
import type { ConversationBox, ConversationSummary } from '@/features/messaging/types/messaging.types';

export type MainStackParamList = {
  Home: undefined;
  Profile: undefined;
  InvitationLanding: { token?: string; unitId?: string };
  MyUnits: undefined;
  // Passing the whole unit rather than just its id: MyUnitsScreen already has
  // it in memory (from useMyUnits), and oikos-web's separate getUnit() call
  // isn't ported here - see MyUnitDetailScreen's module doc for why.
  MyUnitDetail: { unit: OwnedUnit };
  MyInstallments: undefined;
  MyPayments: undefined;
  MyMembershipRequests: undefined;
  Notifications: undefined;
  ConversationList: undefined;
  // Same pattern as MyUnitDetail: the summary is already in memory from
  // whichever screen navigated here (list row, or freshly built after
  // sending - see NewConversationScreen's goToNewThread) - no dedicated
  // GET /conversations/:id in the wire contract to refetch it from.
  Conversation: { conversation: ConversationSummary; box: ConversationBox };
  NewConversation: { draftId?: string };
  Drafts: undefined;
};

const Stack = createNativeStackNavigator<MainStackParamList>();

export function MainNavigator() {
  // Mounted for the Main stack's whole lifetime (not tied to a single
  // screen) so it reliably finalizes an invitation stashed before the user
  // went off to log in or register - see pendingInvitationStore.
  usePendingInvitationConsumer();
  // Registers this device's Expo push token once per session - see its own
  // module doc for why every step in it is allowed to silently no-op.
  usePushTokenRegistration();

  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="Home" component={HomeScreen} />
      <Stack.Screen name="Profile" component={ProfileScreen} />
      <Stack.Screen name="InvitationLanding" component={InvitationLandingScreen} />
      <Stack.Screen name="MyUnits" component={MyUnitsScreen} />
      <Stack.Screen name="MyUnitDetail" component={MyUnitDetailScreen} />
      <Stack.Screen name="MyInstallments" component={MyInstallmentsScreen} />
      <Stack.Screen name="MyPayments" component={MyPaymentsScreen} />
      <Stack.Screen name="MyMembershipRequests" component={MyMembershipRequestsScreen} />
      <Stack.Screen name="Notifications" component={NotificationsListScreen} />
      <Stack.Screen name="ConversationList" component={ConversationListScreen} />
      <Stack.Screen name="Conversation" component={ConversationScreen} />
      <Stack.Screen name="NewConversation" component={NewConversationScreen} />
      <Stack.Screen name="Drafts" component={DraftsListScreen} />
    </Stack.Navigator>
  );
}
