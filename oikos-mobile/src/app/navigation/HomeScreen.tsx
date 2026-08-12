import { ScrollView, StyleSheet, Text } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { useLogout } from '@/features/identity/auth';
import { useUnreadNotificationCount } from '@/features/notifications';
import { useUnreadSummary } from '@/features/messaging';
import { colors } from '@/shared/theme/colors';
import type { MainStackParamList } from '@/app/navigation/MainNavigator';

type Props = NativeStackScreenProps<MainStackParamList, 'Home'>;

/**
 * Owner-space landing screen: flat list of links to each section, mirroring
 * what oikos-web's sidebar gives free navigation-wise. No tab bar / sidebar
 * equivalent built yet - revisit once more Main screens land (property-mngt)
 * and a flat button list stops scaling.
 *
 * Messages/Notifications show their unread count in the button label itself
 * rather than a separate badge overlay (like oikos-web's header bells do):
 * there's no persistent header/chrome on mobile to mount a bell in, and
 * Button only accepts a plain string label - see PLAN.md for why the
 * dropdown-preview bells weren't ported as-is.
 */
export function HomeScreen({ navigation }: Props) {
  const logout = useLogout();
  const unreadNotifications = useUnreadNotificationCount();
  const unreadMessages = useUnreadSummary();

  const notificationsLabel =
    unreadNotifications.data && unreadNotifications.data.unreadCount > 0
      ? `Notifications (${unreadNotifications.data.unreadCount})`
      : 'Notifications';
  const messagesLabel =
    unreadMessages.data && unreadMessages.data.totalUnreadMessageCount > 0
      ? `Messages (${unreadMessages.data.totalUnreadMessageCount})`
      : 'Messages';

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Oikos</Text>

        <Card style={styles.card}>
          <Button variant="secondary" onPress={() => navigation.navigate('MyUnits')}>
            Mes lots
          </Button>
          <Button variant="secondary" onPress={() => navigation.navigate('MyInstallments')}>
            Mes échéances
          </Button>
          <Button variant="secondary" onPress={() => navigation.navigate('MyPayments')}>
            Mes paiements
          </Button>
          <Button variant="secondary" onPress={() => navigation.navigate('MyMembershipRequests')}>
            Mes invitations
          </Button>
        </Card>

        <Card style={styles.card}>
          <Button variant="secondary" onPress={() => navigation.navigate('ConversationList')}>
            {messagesLabel}
          </Button>
          <Button variant="secondary" onPress={() => navigation.navigate('Notifications')}>
            {notificationsLabel}
          </Button>
        </Card>

        <Card style={styles.card}>
          <Button variant="secondary" onPress={() => navigation.navigate('Profile')}>
            Mon profil
          </Button>
          <Button variant="secondary" onPress={logout}>
            Se déconnecter
          </Button>
        </Card>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.gray[50],
  },
  content: {
    padding: 16,
    gap: 16,
  },
  title: {
    fontSize: 22,
    fontWeight: '700',
    color: colors.gray[900],
  },
  card: {
    gap: 8,
  },
});
