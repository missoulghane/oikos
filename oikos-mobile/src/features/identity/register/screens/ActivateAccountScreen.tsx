import { StyleSheet, Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { ActivateAccountForm } from '@/features/identity/register/components/ActivateAccountForm';
import { useActivateAccount } from '@/features/identity/register/hooks/useActivateAccount';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { AuthStackParamList } from '@/app/navigation/AuthNavigator';
import type { ActivateAccountFormValues } from '@/features/identity/register/schemas/activateAccountSchema';

type Props = NativeStackScreenProps<AuthStackParamList, 'ActivateAccount'>;

export function ActivateAccountScreen({ route, navigation }: Props) {
  const { token } = route.params ?? {};
  const { mutate, isPending, isSuccess, error } = useActivateAccount();

  function handleSubmit(values: ActivateAccountFormValues) {
    if (token) {
      mutate({ token, newPassword: values.newPassword });
    }
  }

  return (
    <AuthLayout>
      <Card style={styles.card}>
        <Text style={styles.title}>Activation du compte</Text>
        {!token && <Alert message="Ce lien d'activation est invalide." />}
        {token && isSuccess && (
          <Text style={styles.body}>Votre compte a bien été activé. Vous pouvez maintenant vous connecter.</Text>
        )}
        {token && !isSuccess && (
          <ActivateAccountForm
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
