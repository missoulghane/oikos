import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { useAuthStore } from '@/app/store';
import { useLogout } from '@/features/identity/auth';
import { colors } from '@/shared/theme/colors';

/**
 * Placeholder landing screen for the Main stack - proves the auth flow (login,
 * token persistence, logout) end to end. Will be replaced once
 * features/property-mngt lands.
 */
export function HomeScreen() {
  const roles = useAuthStore((state) => state.roles);
  const logout = useLogout();

  return (
    <SafeAreaView style={styles.container}>
      <Card>
        <Text style={styles.title}>Connecté</Text>
        <Text style={styles.subtitle}>Rôles : {roles.length > 0 ? roles.join(', ') : 'aucun'}</Text>
        <Button variant="secondary" onPress={logout} style={styles.logoutButton}>
          Se déconnecter
        </Button>
      </Card>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.gray[50],
    padding: 16,
    justifyContent: 'center',
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  subtitle: {
    marginTop: 4,
    fontSize: 14,
    color: colors.gray[500],
  },
  logoutButton: {
    marginTop: 16,
  },
});
