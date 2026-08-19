import { StyleSheet, Text } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Button } from '@/shared/components/Button/Button';
import { LoginForm } from '@/features/identity/auth/components/LoginForm';
import { BrandMark } from '@/shared/components/BrandMark/BrandMark';
import { useLogin } from '@/features/identity/auth/hooks/useLogin';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { AuthStackParamList } from '@/app/navigation/AuthNavigator';
import type { LoginFormValues } from '@/features/identity/auth/schemas/loginSchema';

type Props = NativeStackScreenProps<AuthStackParamList, 'Login'>;

export function LoginScreen({ navigation }: Props) {
  const { mutate, isPending, error } = useLogin();

  function handleSubmit(values: LoginFormValues) {
    // No explicit navigation call on success: RootNavigator reacts to
    // isAuthenticated flipping to true (set by useLogin's onSuccess) and
    // switches to the Main stack on its own.
    mutate(values);
  }

  return (
    <AuthLayout>
      <BrandMark size={64} />
      <Text style={styles.title}>Daba Syndic</Text>
      <Card style={styles.card}>
        <LoginForm onSubmit={handleSubmit} isSubmitting={isPending} errorMessage={error ? getErrorMessage(error) : undefined} />
        <Button variant="secondary" onPress={() => navigation.navigate('ForgotPassword')} style={styles.linkButton}>
          Mot de passe oublié ?
        </Button>
        <Button variant="secondary" onPress={() => navigation.navigate('RegisterUser', {})} style={styles.linkButton}>
          Créer un compte
        </Button>
      </Card>
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  title: {
    marginTop: 16,
    marginBottom: 24,
    textAlign: 'center',
    fontSize: 28,
    fontWeight: '700',
    color: colors.gray[900],
  },
  card: {
    gap: 4,
  },
  linkButton: {
    marginTop: 8,
  },
});
