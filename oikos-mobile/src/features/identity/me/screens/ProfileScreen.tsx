import { ScrollView, StyleSheet, Text } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useCurrentUser } from '@/features/identity/me/hooks/useCurrentUser';
import { EditProfileForm } from '@/features/identity/me/components/EditProfileForm';
import { AvatarUploadForm } from '@/features/identity/me/components/AvatarUploadForm';
import { ChangePasswordForm } from '@/features/identity/me/components/ChangePasswordForm';
import { useLogout } from '@/features/identity/auth';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { getFirstName } from '@/shared/utils/getFirstName';
import { getGreeting } from '@/shared/utils/getGreeting';
import { colors } from '@/shared/theme/colors';

export function ProfileScreen() {
  const currentUser = useCurrentUser();
  const logout = useLogout();

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        {currentUser.isLoading && <Loader label="Chargement de votre profil…" />}
        {currentUser.isError && <Alert message={getErrorMessage(currentUser.error)} />}

        {currentUser.data && (
          <>
            {/* Greeting rather than name + email: the address adds nothing for
                someone already signed in (mirrors oikos-web's UserDropdown). */}
            <Text style={styles.greeting}>
              {getGreeting()} {getFirstName(currentUser.data.fullName)}
            </Text>

            <Card>
              <AvatarUploadForm user={currentUser.data} />
            </Card>

            <Card>
              <EditProfileForm user={currentUser.data} />
            </Card>

            <Card style={styles.passwordCard}>
              <Text style={styles.sectionTitle}>Mot de passe</Text>
              <ChangePasswordForm />
            </Card>
          </>
        )}

        <Card style={styles.logoutCard}>
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
  passwordCard: {
    gap: 8,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.gray[900],
  },
  greeting: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  logoutCard: {
    alignItems: 'flex-start',
  },
});
