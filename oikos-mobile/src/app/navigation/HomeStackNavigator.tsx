import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { HomeScreen } from '@/app/navigation/HomeScreen';
import { NotificationBell } from '@/shared/components/NotificationBell/NotificationBell';

export type HomeStackParamList = {
  Home: undefined;
};

const Stack = createNativeStackNavigator<HomeStackParamList>();

export function HomeStackNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: true }}>
      <Stack.Screen name="Home" component={HomeScreen} options={{ title: 'Accueil', headerRight: () => <NotificationBell /> }} />
    </Stack.Navigator>
  );
}
