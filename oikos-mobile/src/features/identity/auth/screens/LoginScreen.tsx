import { StyleSheet, Text } from 'react-native';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { LoginForm } from '@/features/identity/auth/components/LoginForm';
import { useLogin } from '@/features/identity/auth/hooks/useLogin';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { LoginFormValues } from '@/features/identity/auth/schemas/loginSchema';

export function LoginScreen() {
  const { mutate, isPending, error } = useLogin();

  function handleSubmit(values: LoginFormValues) {
    // No explicit navigation call on success: RootNavigator reacts to
    // isAuthenticated flipping to true (set by useLogin's onSuccess) and
    // switches to the Main stack on its own.
    mutate(values);
  }

  return (
    <AuthLayout>
      <Text style={styles.title}>Oikos</Text>
      <Card style={styles.card}>
        <LoginForm onSubmit={handleSubmit} isSubmitting={isPending} errorMessage={error ? getErrorMessage(error) : undefined} />
      </Card>
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  title: {
    marginBottom: 24,
    textAlign: 'center',
    fontSize: 28,
    fontWeight: '700',
    color: colors.gray[900],
  },
  card: {
    gap: 4,
  },
});
