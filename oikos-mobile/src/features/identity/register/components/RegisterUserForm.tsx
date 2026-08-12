import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { StyleSheet, View } from 'react-native';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  registerUserSchema,
  type RegisterUserFormValues,
} from '@/features/identity/register/schemas/registerUserSchema';

interface RegisterUserFormProps {
  onSubmit: (values: RegisterUserFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function RegisterUserForm({ onSubmit, isSubmitting, errorMessage }: RegisterUserFormProps) {
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterUserFormValues>({
    resolver: zodResolver(registerUserSchema),
    defaultValues: { fullName: '', email: '', password: '', confirmPassword: '' },
  });

  return (
    <View style={styles.form}>
      {errorMessage && <Alert message={errorMessage} />}
      <ControlledInput
        control={control}
        name="fullName"
        label="Nom complet"
        autoComplete="name"
        errorMessage={errors.fullName?.message}
      />
      <ControlledInput
        control={control}
        name="email"
        label="Email"
        keyboardType="email-address"
        autoComplete="email"
        autoCapitalize="none"
        errorMessage={errors.email?.message}
      />
      <ControlledInput
        control={control}
        name="password"
        label="Mot de passe"
        autoComplete="new-password"
        autoCapitalize="none"
        secureTextEntry
        errorMessage={errors.password?.message}
      />
      <ControlledInput
        control={control}
        name="confirmPassword"
        label="Confirmer le mot de passe"
        autoComplete="new-password"
        autoCapitalize="none"
        secureTextEntry
        errorMessage={errors.confirmPassword?.message}
      />
      <Button onPress={handleSubmit(onSubmit)} isLoading={isSubmitting} style={styles.submitButton}>
        Créer mon compte
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
