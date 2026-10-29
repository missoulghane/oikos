import { Linking, Pressable, StyleSheet, Text, View } from 'react-native';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { useRegisterPropertyBoardAdmin } from '@/features/identity/register/hooks/useRegisterPropertyBoardAdmin';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import type { BoardSeat } from '@/features/identity/onboarding/constants/boardSeats';
import { useBoardRoles } from '@/shared/hooks/useBoardRoles';
import { CatalogLoadState } from '@/shared/components/CatalogLoadState/CatalogLoadState';
import { colors } from '@/shared/theme/colors';
import { WEB_URL } from '@/config/env';
import {
  accountStepSchema,
  PASSWORD_MIN_LENGTH,
  type AccountStepValues,
} from '@/features/identity/onboarding/schemas/onboardingSchemas';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'Account'>;


export function AccountStepScreen({ navigation }: Props) {
  const { draft, update, reset } = useOnboarding();
  const registerAccount = useRegisterPropertyBoardAdmin();
  // Même catalogue que « Ajouter un membre » (table board_role), restreint aux
  // sièges ouverts à l'inscription.
  const boardRoles = useBoardRoles();
  const boardSeatOptions = (boardRoles.data ?? [])
    .filter((role) => role.availableAtSignup)
    .map((role) => ({ value: role.code, label: role.label }));

  const {
    control,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<AccountStepValues>({
    resolver: zodResolver(accountStepSchema),
    // Le mot de passe n'est jamais prérempli : il part vers l'API à la
    // soumission de cette étape et n'est conservé nulle part ensuite.
    defaultValues: { ...draft.account, password: '', confirmPassword: '' },
  });

  const typedPassword = watch('password') ?? '';

  // Le compte est créé ici, dès l'étape 1 : revenir sur cette étape ne doit pas
  // le recréer (l'email est déjà pris), seulement laisser poursuivre.
  if (draft.accountRegistration) {
    return (
      <AuthLayout scrollable>
        <WizardShell step="Account" title="Votre compte est créé" subtitle={draft.account.email}>
          <View style={styles.form}>
            <Alert
              variant="success"
              message="Un email de confirmation vous a été envoyé. Vous pouvez poursuivre la création de votre copropriété dès maintenant."
            />
            <Button onPress={() => navigation.navigate('Property')}>Continuer</Button>
            <Button variant="secondary" onPress={reset}>
              Recommencer avec un autre compte
            </Button>
          </View>
        </WizardShell>
      </AuthLayout>
    );
  }

  function onSubmit(values: AccountStepValues) {
    const account = {
      fullName: values.fullName,
      email: values.email,
      boardRole: values.boardRole,
      acceptedTerms: values.acceptedTerms,
    };
    update({ account });
    registerAccount.mutate(
      {
        fullName: values.fullName,
        email: values.email,
        password: values.password,
        acceptedTerms: values.acceptedTerms,
      },
      {
        onSuccess: (registration) => {
          update({
            account,
            accountRegistration: { userId: registration.userId, onboardingToken: registration.onboardingToken },
          });
          navigation.navigate('Property');
        },
      },
    );
  }

  return (
    <AuthLayout scrollable>
      <WizardShell
        step="Account"
        title="Créez votre compte"
        subtitle="L'inscription est réservée au syndic et aux membres du conseil syndical de la copropriété."
      >
        <View style={styles.form}>
          {registerAccount.isError && <Alert message={getErrorMessage(registerAccount.error)} />}
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
          {boardRoles.data ? (
            <Controller
              control={control}
              name="boardRole"
              render={({ field: { value, onChange } }) => (
                <Select
                  label="Votre rôle dans le conseil syndical"
                  value={value}
                  onChange={(seat) => onChange(seat as BoardSeat)}
                  options={boardSeatOptions}
                  errorMessage={errors.boardRole?.message}
                />
              )}
            />
          ) : (
            <CatalogLoadState
              query={boardRoles}
              loadingLabel="Chargement des rôles…"
              errorMessage="Impossible de charger la liste des rôles. Vérifiez votre connexion, puis réessayez."
            />
          )}
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
          <Controller
            control={control}
            name="acceptedTerms"
            render={({ field: { value, onChange } }) => (
              <View>
                <Pressable
                  accessibilityRole="checkbox"
                  accessibilityState={{ checked: value }}
                  onPress={() => onChange(!value)}
                  style={styles.termsRow}
                >
                  <View style={[styles.checkbox, value && styles.checkboxChecked]}>
                    {value && <Text style={styles.checkmark}>✓</Text>}
                  </View>
                  {/* Les liens ouvrent le navigateur : le Text imbriqué capte le toucher,
                      la case ne se coche donc pas en les suivant. */}
                  <Text style={styles.termsLabel}>
                    {"J'accepte les "}
                    <Text style={styles.termsLink} onPress={() => Linking.openURL(`${WEB_URL}/cgu`)}>
                      {"conditions générales d'utilisation"}
                    </Text>
                    {' et les '}
                    <Text style={styles.termsLink} onPress={() => Linking.openURL(`${WEB_URL}/cgv`)}>
                      conditions générales de vente
                    </Text>
                  </Text>
                </Pressable>
                {errors.acceptedTerms?.message && <Text style={styles.error}>{errors.acceptedTerms.message}</Text>}
              </View>
            )}
          />
          <Button onPress={handleSubmit(onSubmit)} isLoading={registerAccount.isPending}>
            Continuer
          </Button>
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
  termsRow: {
    flexDirection: 'row',
    // Le libellé, avec ses deux liens, tient sur deux lignes : la case s'aligne
    // sur la première plutôt que de flotter au milieu.
    alignItems: 'flex-start',
    gap: 8,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderRadius: 4,
    borderWidth: 2,
    borderColor: colors.gray[300],
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkboxChecked: {
    borderColor: colors.brand[500],
    backgroundColor: colors.brand[500],
  },
  checkmark: {
    fontSize: 12,
    fontWeight: '700',
    color: colors.white,
  },
  termsLabel: {
    flex: 1,
    fontSize: 14,
    color: colors.gray[500],
  },
  termsLink: {
    color: colors.brand[500],
    textDecorationLine: 'underline',
  },
  error: {
    marginTop: 4,
    fontSize: 14,
    color: colors.error[500],
  },
});
