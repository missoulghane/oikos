import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { StyleSheet, View } from 'react-native';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  resetPasswordSchema,
  type ResetPasswordFormValues,
} from '@/features/identity/auth/schemas/resetPasswordSchema';

interface ResetPasswordFormProps {
  onSubmit: (values: ResetPasswordFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function ResetPasswordForm({ onSubmit, isSubmitting, errorMessage }: ResetPasswordFormProps) {
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { password: '', confirmPassword: '' },
  });

  return (
    <View style={styles.form}>
      {errorMessage && <Alert message={errorMessage} />}
      <ControlledInput
        control={control}
        name="password"
        label="Nouveau mot de passe"
        autoComplete="new-password"
        autoCapitalize="none"
        secureTextEntry
        errorMessage={errors.password?.message}
      />
      <ControlledInput
        control={control}
        name="confirmPassword"
        label="Confirmation du mot de passe"
        autoComplete="new-password"
        autoCapitalize="none"
        secureTextEntry
        errorMessage={errors.confirmPassword?.message}
      />
      <Button onPress={handleSubmit(onSubmit)} isLoading={isSubmitting} style={styles.submitButton}>
        Réinitialiser le mot de passe
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
