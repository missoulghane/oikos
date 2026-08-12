import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { StyleSheet, View } from 'react-native';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  activateAccountSchema,
  type ActivateAccountFormValues,
} from '@/features/identity/register/schemas/activateAccountSchema';

interface ActivateAccountFormProps {
  onSubmit: (values: ActivateAccountFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function ActivateAccountForm({ onSubmit, isSubmitting, errorMessage }: ActivateAccountFormProps) {
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<ActivateAccountFormValues>({
    resolver: zodResolver(activateAccountSchema),
    defaultValues: { newPassword: '' },
  });

  return (
    <View style={styles.form}>
      {errorMessage && <Alert message={errorMessage} />}
      <ControlledInput
        control={control}
        name="newPassword"
        label="Nouveau mot de passe"
        autoComplete="new-password"
        autoCapitalize="none"
        secureTextEntry
        errorMessage={errors.newPassword?.message}
      />
      <Button onPress={handleSubmit(onSubmit)} isLoading={isSubmitting} style={styles.submitButton}>
        Activer mon compte
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
