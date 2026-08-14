import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { useUnreadNotificationCount } from '@/features/notifications';
import { colors } from '@/shared/theme/colors';
import type { MainStackParamList } from '@/app/navigation/MainNavigator';

/**
 * Wired via `headerRight` on each tab's root screen only (not on pushed
 * screens) - see MainStackParamList/UnitsStackNavigator etc. `navigate`
 * targets the root-level `Notifications` screen; React Navigation resolves
 * it through the tab/stack ancestors automatically since the name is unique
 * across the whole tree, so no `getParent()` is needed here.
 */
export function NotificationBell() {
  const navigation = useNavigation<NativeStackNavigationProp<MainStackParamList>>();
  const unread = useUnreadNotificationCount();
  const count = unread.data?.unreadCount ?? 0;

  return (
    <Pressable onPress={() => navigation.navigate('Notifications')} accessibilityLabel="Notifications" style={styles.wrapper}>
      <Ionicons name="notifications-outline" size={24} color={colors.gray[700]} />
      {count > 0 && (
        <View style={styles.badge}>
          <Text style={styles.badgeText}>{count > 9 ? '9+' : count}</Text>
        </View>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    padding: 4,
    marginRight: 4,
  },
  badge: {
    position: 'absolute',
    top: 0,
    right: 0,
    minWidth: 16,
    height: 16,
    borderRadius: 8,
    paddingHorizontal: 3,
    backgroundColor: colors.error[500],
    alignItems: 'center',
    justifyContent: 'center',
  },
  badgeText: {
    fontSize: 10,
    fontWeight: '700',
    color: colors.white,
  },
});
