import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { StyleSheet, Text, View } from 'react-native';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { colors } from '@/shared/theme/colors';
import {
  acceptInvitationSchema,
  type AcceptInvitationFormValues,
} from '@/features/identity/register/schemas/acceptInvitationSchema';

interface AcceptInvitationFormProps {
  onSubmit: (values: AcceptInvitationFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function AcceptInvitationForm({ onSubmit, isSubmitting, errorMessage }: AcceptInvitationFormProps) {
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<AcceptInvitationFormValues>({
    resolver: zodResolver(acceptInvitationSchema),
    defaultValues: { password: '' },
  });

  return (
    <View style={styles.form}>
      {errorMessage && <Alert message={errorMessage} />}
      <Text style={styles.hint}>
        Si vous avez déjà un compte Daba Syndic, laissez ce champ vide. Sinon, choisissez un mot de passe pour créer votre
        compte.
      </Text>
      <ControlledInput
        control={control}
        name="password"
        label="Mot de passe (nouveau compte uniquement)"
        autoComplete="new-password"
        autoCapitalize="none"
        secureTextEntry
        errorMessage={errors.password?.message}
      />
      <Button onPress={handleSubmit(onSubmit)} isLoading={isSubmitting} style={styles.submitButton}>
        Accepter l'invitation
      </Button>
    </View>
  );
}

const styles = StyleSheet.create({
  form: {
    gap: 16,
  },
  hint: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.gray[500],
  },
  submitButton: {
    marginTop: 8,
  },
});
