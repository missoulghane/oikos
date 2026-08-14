import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import type { NavigatorScreenParams } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import { HomeStackNavigator, type HomeStackParamList } from '@/app/navigation/HomeStackNavigator';
import { MessagingStackNavigator, type MessagingStackParamList } from '@/app/navigation/MessagingStackNavigator';
import { UnitsStackNavigator, type UnitsStackParamList } from '@/app/navigation/UnitsStackNavigator';
import { AccountStackNavigator, type AccountStackParamList } from '@/app/navigation/AccountStackNavigator';
import { colors } from '@/shared/theme/colors';

export type MainTabParamList = {
  HomeTab: NavigatorScreenParams<HomeStackParamList> | undefined;
  MessagingTab: NavigatorScreenParams<MessagingStackParamList> | undefined;
  UnitsTab: NavigatorScreenParams<UnitsStackParamList> | undefined;
  AccountTab: NavigatorScreenParams<AccountStackParamList> | undefined;
};

const Tab = createBottomTabNavigator<MainTabParamList>();

type IconName = keyof typeof Ionicons.glyphMap;

const TAB_ICONS: Record<keyof MainTabParamList, { focused: IconName; unfocused: IconName }> = {
  HomeTab: { focused: 'home', unfocused: 'home-outline' },
  MessagingTab: { focused: 'chatbubbles', unfocused: 'chatbubbles-outline' },
  UnitsTab: { focused: 'business', unfocused: 'business-outline' },
  AccountTab: { focused: 'person', unfocused: 'person-outline' },
};

/**
 * Each tab owns its own native-stack (see *StackNavigator files) so
 * push/back history stays independent per tab - switching tabs and back
 * preserves whatever screen was pushed. Headers (title, back button, bell)
 * live entirely inside those inner stacks, not here.
 */
export function MainTabNavigator() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: colors.brand[500],
        tabBarInactiveTintColor: colors.gray[400],
        tabBarIcon: ({ focused, color, size }) => {
          const icons = TAB_ICONS[route.name as keyof MainTabParamList];
          return <Ionicons name={focused ? icons.focused : icons.unfocused} size={size} color={color} />;
        },
      })}
    >
      <Tab.Screen name="HomeTab" component={HomeStackNavigator} options={{ title: 'Accueil' }} />
      <Tab.Screen name="MessagingTab" component={MessagingStackNavigator} options={{ title: 'Messagerie' }} />
      <Tab.Screen name="UnitsTab" component={UnitsStackNavigator} options={{ title: 'Mes lots' }} />
      <Tab.Screen name="AccountTab" component={AccountStackNavigator} options={{ title: 'Mon compte' }} />
    </Tab.Navigator>
  );
}
