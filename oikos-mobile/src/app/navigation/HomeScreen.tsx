import { ScrollView, StyleSheet, Text } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useCurrentUser } from '@/features/identity/me/hooks/useCurrentUser';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getFirstName } from '@/shared/utils/getFirstName';
import { getGreeting } from '@/shared/utils/getGreeting';
import { colors } from '@/shared/theme/colors';

/**
 * Placeholder now that the bottom tab bar covers primary navigation (Mes
 * lots/Messagerie/Mon compte) - this screen used to be a flat list of links
 * to those sections, see git history if reviving a dashboard here.
 */
export function HomeScreen() {
  const currentUser = useCurrentUser();
  const firstName = currentUser.data ? getFirstName(currentUser.data.fullName) : '';

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.greeting}>
          {getGreeting()} {firstName}
        </Text>
        <EmptyState title="Accueil">Cette section sera bientôt disponible.</EmptyState>
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
  greeting: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
});
