import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { StyleSheet, Text, View } from 'react-native';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { colors } from '@/shared/theme/colors';
import {
  registerPropertyAdminSchema,
  type RegisterPropertyAdminFormValues,
} from '@/features/identity/register/schemas/registerPropertyAdminSchema';

interface RegisterPropertyAdminFormProps {
  onSubmit: (values: RegisterPropertyAdminFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
  submitLabel: string;
}

export function RegisterPropertyAdminForm({
  onSubmit,
  isSubmitting,
  errorMessage,
  submitLabel,
}: RegisterPropertyAdminFormProps) {
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterPropertyAdminFormValues>({ resolver: zodResolver(registerPropertyAdminSchema) });

  return (
    <View style={styles.form}>
      {errorMessage && <Alert message={errorMessage} />}
      <View style={styles.fieldset}>
        <Text style={styles.legend}>Vos informations</Text>
        <ControlledInput control={control} name="fullName" label="Nom complet" autoComplete="name" errorMessage={errors.fullName?.message} />
        <ControlledInput
          control={control}
          name="email"
          label="Email"
          keyboardType="email-address"
          autoCapitalize="none"
          autoComplete="email"
          errorMessage={errors.email?.message}
        />
        <ControlledInput control={control} name="phone" label="Téléphone" autoComplete="tel" errorMessage={errors.phone?.message} />
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
      </View>
      <View style={styles.fieldset}>
        <Text style={styles.legend}>Votre copropriété</Text>
        <ControlledInput control={control} name="propertyName" label="Nom de la copropriété" errorMessage={errors.propertyName?.message} />
        <ControlledInput control={control} name="propertyAddress" label="Adresse" errorMessage={errors.propertyAddress?.message} />
        <ControlledInput control={control} name="propertyCity" label="Ville" errorMessage={errors.propertyCity?.message} />
      </View>
      <Button onPress={handleSubmit(onSubmit)} isLoading={isSubmitting} style={styles.submitButton}>
        {submitLabel}
      </Button>
    </View>
  );
}

const styles = StyleSheet.create({
  form: {
    gap: 16,
  },
  fieldset: {
    gap: 16,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.gray[200],
    padding: 16,
  },
  legend: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[700],
  },
  submitButton: {
    marginTop: 8,
  },
});
