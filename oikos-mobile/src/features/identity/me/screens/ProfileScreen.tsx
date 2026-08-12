import { ScrollView, StyleSheet, Text } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useCurrentUser } from '@/features/identity/me/hooks/useCurrentUser';
import { EditProfileForm } from '@/features/identity/me/components/EditProfileForm';
import { AvatarUploadForm } from '@/features/identity/me/components/AvatarUploadForm';
import { ChangePasswordForm } from '@/features/identity/me/components/ChangePasswordForm';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';

export function ProfileScreen() {
  const currentUser = useCurrentUser();

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Mes informations</Text>

        {currentUser.isLoading && <Loader label="Chargement de votre profil…" />}
        {currentUser.isError && <Alert message={getErrorMessage(currentUser.error)} />}

        {currentUser.data && (
          <>
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
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  passwordCard: {
    gap: 8,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.gray[900],
  },
});
