import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { StyleSheet, View } from 'react-native';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  forgotPasswordSchema,
  type ForgotPasswordFormValues,
} from '@/features/identity/auth/schemas/forgotPasswordSchema';

interface ForgotPasswordFormProps {
  onSubmit: (values: ForgotPasswordFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function ForgotPasswordForm({ onSubmit, isSubmitting, errorMessage }: ForgotPasswordFormProps) {
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: { email: '' },
  });

  return (
    <View style={styles.form}>
      {errorMessage && <Alert message={errorMessage} />}
      <ControlledInput
        control={control}
        name="email"
        label="Email"
        keyboardType="email-address"
        autoComplete="email"
        autoCapitalize="none"
        errorMessage={errors.email?.message}
      />
      <Button onPress={handleSubmit(onSubmit)} isLoading={isSubmitting} style={styles.submitButton}>
        Envoyer le lien de réinitialisation
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
