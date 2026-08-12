import { StyleSheet, Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { ForgotPasswordForm } from '@/features/identity/auth/components/ForgotPasswordForm';
import { useForgotPassword } from '@/features/identity/auth/hooks/useForgotPassword';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { AuthStackParamList } from '@/app/navigation/AuthNavigator';
import type { ForgotPasswordFormValues } from '@/features/identity/auth/schemas/forgotPasswordSchema';

type Props = NativeStackScreenProps<AuthStackParamList, 'ForgotPassword'>;

export function ForgotPasswordScreen({ navigation }: Props) {
  const { mutate, isPending, isSuccess, error } = useForgotPassword();

  function handleSubmit(values: ForgotPasswordFormValues) {
    mutate(values);
  }

  if (isSuccess) {
    return (
      <AuthLayout>
        <Card style={styles.card}>
          <Text style={styles.title}>Vérifiez votre boîte mail</Text>
          <Text style={styles.body}>
            Si un compte existe pour cette adresse, un email contenant un lien de réinitialisation vient de vous
            être envoyé.
          </Text>
          <Button variant="secondary" onPress={() => navigation.navigate('Login')} style={styles.linkButton}>
            Retour à la connexion
          </Button>
        </Card>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <Card style={styles.card}>
        <Text style={styles.title}>Mot de passe oublié</Text>
        <ForgotPasswordForm
          onSubmit={handleSubmit}
          isSubmitting={isPending}
          errorMessage={error ? getErrorMessage(error) : undefined}
        />
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
