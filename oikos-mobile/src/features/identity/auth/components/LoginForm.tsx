import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { StyleSheet, View } from 'react-native';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { loginSchema, type LoginFormValues } from '@/features/identity/auth/schemas/loginSchema';

interface LoginFormProps {
  onSubmit: (values: LoginFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function LoginForm({ onSubmit, isSubmitting, errorMessage }: LoginFormProps) {
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema), defaultValues: { identifier: '', password: '' } });

  return (
    <View style={styles.form}>
      {errorMessage && <Alert message={errorMessage} />}
      <ControlledInput
        control={control}
        name="identifier"
        label="Identifiant"
        autoComplete="username"
        autoCapitalize="none"
        errorMessage={errors.identifier?.message}
      />
      <ControlledInput
        control={control}
        name="password"
        label="Mot de passe"
        autoComplete="current-password"
        autoCapitalize="none"
        secureTextEntry
        errorMessage={errors.password?.message}
      />
      <Button onPress={handleSubmit(onSubmit)} isLoading={isSubmitting} style={styles.submitButton}>
        Se connecter
      </Button>
    </View>
  );
}

const styles = StyleSheet.create({
  form: {
    gap: 16,
  },
  submitButton: {
    marginTop: 8,
  },
});
