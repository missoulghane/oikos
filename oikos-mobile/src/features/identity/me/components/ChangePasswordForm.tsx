import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { StyleSheet, View } from 'react-native';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useChangePassword } from '@/features/identity/me/hooks/useChangePassword';
import {
  changePasswordSchema,
  type ChangePasswordFormValues,
} from '@/features/identity/me/schemas/changePasswordSchema';

export function ChangePasswordForm() {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
  });
  const { mutate, isPending, isSuccess, error } = useChangePassword();

  function onSubmit(values: ChangePasswordFormValues) {
    mutate(
      { currentPassword: values.currentPassword, newPassword: values.newPassword },
      { onSuccess: () => reset() },
    );
  }

  return (
    <View style={styles.form}>
      {isSuccess && <Alert variant="success" message="Votre mot de passe a été modifié." />}
      {error && <Alert message={getErrorMessage(error)} />}
      <ControlledInput
        control={control}
        name="currentPassword"
        label="Mot de passe actuel"
        autoComplete="current-password"
        autoCapitalize="none"
        secureTextEntry
        errorMessage={errors.currentPassword?.message}
      />
      <ControlledInput
        control={control}
        name="newPassword"
        label="Nouveau mot de passe"
        autoComplete="new-password"
        autoCapitalize="none"
        secureTextEntry
        errorMessage={errors.newPassword?.message}
      />
      <ControlledInput
        control={control}
        name="confirmPassword"
        label="Confirmer le nouveau mot de passe"
        autoComplete="new-password"
        autoCapitalize="none"
        secureTextEntry
        errorMessage={errors.confirmPassword?.message}
      />
      <Button onPress={handleSubmit(onSubmit)} isLoading={isPending} style={styles.submitButton}>
        Modifier le mot de passe
      </Button>
    </View>
  );
}

const styles = StyleSheet.create({
  form: {
    gap: 16,
  },
  submitButton: {
    alignSelf: 'flex-start',
  },
});
