import { StyleSheet, Text, View } from 'react-native';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { useCaptureOnboardingLead } from '@/features/identity/onboarding/hooks/useCaptureOnboardingLead';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { colors } from '@/shared/theme/colors';
import {
  accountStepSchema,
  PASSWORD_MIN_LENGTH,
  type AccountStepValues,
} from '@/features/identity/onboarding/schemas/onboardingSchemas';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'Account'>;

export function AccountStepScreen({ navigation }: Props) {
  const { draft, update, password, setPassword } = useOnboarding();
  const captureLead = useCaptureOnboardingLead();

  const {
    control,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<AccountStepValues>({
    resolver: zodResolver(accountStepSchema),
    defaultValues: { ...draft.account, password, confirmPassword: password },
  });

  const typedPassword = watch('password') ?? '';

  function onSubmit(values: AccountStepValues) {
    update({ account: { fullName: values.fullName, email: values.email } });
    setPassword(values.password);
    // Capture d'email « au mieux » : le compte n'existe qu'à la fin de l'étape 2,
    // et un échec ici ne doit surtout pas bloquer la progression du visiteur.
    captureLead.mutate({ email: values.email, fullName: values.fullName });
    navigation.navigate('Property');
  }

  return (
    <AuthLayout scrollable>
      <WizardShell
        step="Account"
        title="Créez votre compte"
        subtitle="Quelques informations pour créer votre compte de syndic bénévole."
      >
        <View style={styles.form}>
          <ControlledInput control={control} name="fullName" label="Nom complet" autoComplete="name" errorMessage={errors.fullName?.message} />
          <ControlledInput
            control={control}
            name="email"
            label="Adresse email"
            keyboardType="email-address"
            autoCapitalize="none"
            autoComplete="email"
            errorMessage={errors.email?.message}
          />
          <ControlledInput
            control={control}
            name="password"
            label="Mot de passe"
            autoComplete="new-password"
            autoCapitalize="none"
            secureTextEntry
          />
          <View style={styles.passwordRule}>
            <Text style={typedPassword.length >= PASSWORD_MIN_LENGTH ? styles.ruleSatisfied : styles.ruleUnsatisfied}>
              {typedPassword.length >= PASSWORD_MIN_LENGTH ? '✓' : '○'} Au moins {PASSWORD_MIN_LENGTH} caractères
            </Text>
          </View>
          <ControlledInput
            control={control}
            name="confirmPassword"
            label="Confirmation du mot de passe"
            autoComplete="new-password"
            autoCapitalize="none"
            secureTextEntry
            errorMessage={errors.confirmPassword?.message}
          />
          <Button onPress={handleSubmit(onSubmit)}>Continuer</Button>
        </View>
      </WizardShell>
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  form: {
    gap: 16,
  },
  passwordRule: {
    marginTop: -8,
  },
  ruleSatisfied: {
    fontSize: 14,
    color: colors.success[500],
  },
  ruleUnsatisfied: {
    fontSize: 14,
    color: colors.gray[500],
  },
});
