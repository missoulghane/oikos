import { StyleSheet, Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { ResetPasswordForm } from '@/features/identity/auth/components/ResetPasswordForm';
import { useResetPassword } from '@/features/identity/auth/hooks/useResetPassword';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { AuthStackParamList } from '@/app/navigation/AuthNavigator';
import type { ResetPasswordFormValues } from '@/features/identity/auth/schemas/resetPasswordSchema';

type Props = NativeStackScreenProps<AuthStackParamList, 'ResetPassword'>;

export function ResetPasswordScreen({ route, navigation }: Props) {
  const { token } = route.params ?? {};
  const { mutate, isPending, isSuccess, error } = useResetPassword();

  function handleSubmit(values: ResetPasswordFormValues) {
    if (token) {
      // Le formulaire nomme ses champs password / confirmPassword (pour réutiliser
      // la vérification de concordance) ; l'API attend newPassword.
      mutate({ token, newPassword: values.password });
    }
  }

  return (
    <AuthLayout>
      <Card style={styles.card}>
        <Text style={styles.title}>Réinitialisation du mot de passe</Text>
        {!token && <Alert message="Ce lien de réinitialisation est invalide." />}
        {token && isSuccess && (
          <Text style={styles.body}>
            Votre mot de passe a bien été réinitialisé. Vos autres appareils ont été déconnectés ; vous pouvez
            maintenant vous connecter.
          </Text>
        )}
        {token && !isSuccess && (
          <ResetPasswordForm
            onSubmit={handleSubmit}
            isSubmitting={isPending}
            errorMessage={error ? getErrorMessage(error) : undefined}
          />
        )}
        <Button variant="secondary" onPress={() => navigation.navigate('Login')} style={styles.linkButton}>
          Retour à la connexion
        </Button>
      </Card>
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: 8,
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  body: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.gray[600],
  },
  linkButton: {
    marginTop: 8,
  },
});
