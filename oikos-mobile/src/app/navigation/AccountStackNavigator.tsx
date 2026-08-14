import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { ProfileScreen } from '@/features/identity/me';
import { NotificationBell } from '@/shared/components/NotificationBell/NotificationBell';

export type AccountStackParamList = {
  Profile: undefined;
};

const Stack = createNativeStackNavigator<AccountStackParamList>();

export function AccountStackNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: true }}>
      <Stack.Screen
        name="Profile"
        component={ProfileScreen}
        options={{ title: 'Mon compte', headerRight: () => <NotificationBell /> }}
      />
    </Stack.Navigator>
  );
}
