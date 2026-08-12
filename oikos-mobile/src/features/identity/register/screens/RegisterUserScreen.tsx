import { StyleSheet, Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { RegisterUserForm } from '@/features/identity/register/components/RegisterUserForm';
import { useRegisterUser } from '@/features/identity/register/hooks/useRegisterUser';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { AuthStackParamList } from '@/app/navigation/AuthNavigator';
import type { RegisterUserFormValues } from '@/features/identity/register/schemas/registerUserSchema';

type Props = NativeStackScreenProps<AuthStackParamList, 'RegisterUser'>;

export function RegisterUserScreen({ route, navigation }: Props) {
  const { invitationToken, unitId } = route.params ?? {};
  const { mutate, isPending, isSuccess, error } = useRegisterUser();

  // No `returnTo` forwarding here (unlike oikos-web): that param exists to send
  // the browser back to a page after clicking the email verification link. On
  // mobile the verification link is a deep link straight into the app, so
  // there's nothing to "return to" - VerifyEmailScreen is the destination already.
  function handleSubmit({ confirmPassword: _confirmPassword, ...payload }: RegisterUserFormValues) {
    mutate({
      ...payload,
      ...(invitationToken && unitId ? { invitationToken, unitId } : {}),
    });
  }

  if (isSuccess) {
    return (
      <AuthLayout>
        <Card style={styles.card}>
          <Text style={styles.title}>Vérifiez votre boîte mail</Text>
          <Text style={styles.body}>
            Un email de confirmation vient de vous être envoyé. Ouvrez-le sur cet appareil et touchez le lien qu'il
            contient pour activer votre compte.
          </Text>
          <Button variant="secondary" onPress={() => navigation.navigate('Login')} style={styles.linkButton}>
            Retour à la connexion
          </Button>
        </Card>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout scrollable>
      <Card style={styles.card}>
        <Text style={styles.title}>Créer un compte</Text>
        <RegisterUserForm
          onSubmit={handleSubmit}
          isSubmitting={isPending}
          errorMessage={error ? getErrorMessage(error) : undefined}
        />
        <Button variant="secondary" onPress={() => navigation.navigate('Login')} style={styles.linkButton}>
          Déjà un compte ? Se connecter
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
