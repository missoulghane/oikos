import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { StyleSheet, View } from 'react-native';
import type { CurrentUser } from '@/features/identity/me/types/me.types';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useUpdateProfile } from '@/features/identity/me/hooks/useUpdateProfile';
import {
  updateProfileSchema,
  type UpdateProfileFormValues,
} from '@/features/identity/me/schemas/updateProfileSchema';

export function EditProfileForm({ user }: { user: CurrentUser }) {
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdateProfileFormValues>({
    resolver: zodResolver(updateProfileSchema),
    defaultValues: { fullName: user.fullName, email: user.email, phone: user.phone ?? '' },
  });
  const { mutate, isPending, isSuccess, error } = useUpdateProfile();

  function onSubmit(values: UpdateProfileFormValues) {
    mutate(values);
  }

  return (
    <View style={styles.form}>
      {isSuccess && <Alert variant="success" message="Vos informations ont été mises à jour." />}
      {error && <Alert message={getErrorMessage(error)} />}
      <ControlledInput control={control} name="fullName" label="Nom complet" errorMessage={errors.fullName?.message} />
      <ControlledInput
        control={control}
        name="email"
        label="Email"
        keyboardType="email-address"
        autoCapitalize="none"
        errorMessage={errors.email?.message}
      />
      <ControlledInput
        control={control}
        name="phone"
        label="Téléphone"
        keyboardType="phone-pad"
        errorMessage={errors.phone?.message}
      />
      <Button onPress={handleSubmit(onSubmit)} isLoading={isPending} style={styles.submitButton}>
        Enregistrer
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
