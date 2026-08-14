import { createNativeStackNavigator } from '@react-navigation/native-stack';
import type { NavigatorScreenParams } from '@react-navigation/native';
import { MainTabNavigator, type MainTabParamList } from '@/app/navigation/MainTabNavigator';
import { InvitationLandingScreen, usePendingInvitationConsumer } from '@/features/identity/invitations';
import { NotificationsListScreen, usePushTokenRegistration } from '@/features/notifications';

export type MainStackParamList = {
  MainTabs: NavigatorScreenParams<MainTabParamList> | undefined;
  Notifications: undefined;
  InvitationLanding: { token?: string; unitId?: string };
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
      <Stack.Screen name="MainTabs" component={MainTabNavigator} />
      <Stack.Screen name="Notifications" component={NotificationsListScreen} options={{ headerShown: true, title: 'Notifications' }} />
      <Stack.Screen name="InvitationLanding" component={InvitationLandingScreen} />
    </Stack.Navigator>
  );
}
