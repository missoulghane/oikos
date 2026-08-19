import { StyleSheet, View } from 'react-native';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useRegisterPropertyBoardAdmin } from '@/features/identity/register/hooks/useRegisterPropertyBoardAdmin';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { formatAddress } from '@/features/identity/onboarding/state/onboardingDraft';
import {
  propertyStepSchema,
  PROPERTY_ADDRESS_MAX_LENGTH,
  type PropertyStepValues,
} from '@/features/identity/onboarding/schemas/onboardingSchemas';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'Property'>;

export function PropertyStepScreen({ navigation }: Props) {
  const { draft, update, password } = useOnboarding();
  const registerBoardAdmin = useRegisterPropertyBoardAdmin();

  const {
    control,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<PropertyStepValues>({
    resolver: zodResolver(propertyStepSchema),
    defaultValues: draft.property,
  });

  const alreadyRegistered = draft.registration !== null;

  function onSubmit(values: PropertyStepValues) {
    const property = {
      name: values.name,
      address: values.address,
      addressComplement: values.addressComplement ?? '',
      postalCode: values.postalCode ?? '',
      city: values.city,
    };
    const address = formatAddress(property);
    if (address.length > PROPERTY_ADDRESS_MAX_LENGTH) {
      setError('address', { message: `Adresse complète trop longue (${PROPERTY_ADDRESS_MAX_LENGTH} caractères maximum)` });
      return;
    }
    update({ property });

    // Le compte est créé ici, et pas à l'étape 1 : la party qui relie le compte à
    // sa copropriété ne peut pas exister avant que celle-ci ait un nom. À partir
    // de maintenant, un abandon laisse un compte utilisable derrière lui.
    if (alreadyRegistered) {
      navigation.navigate('DuesMode');
      return;
    }

    registerBoardAdmin.mutate(
      {
        fullName: draft.account.fullName,
        email: draft.account.email,
        password,
        propertyName: property.name,
        propertyAddress: address,
      },
      {
        onSuccess: (registration) => {
          update({
            property,
            registration: {
              propertyId: registration.propertyId,
              onboardingToken: registration.onboardingToken,
            },
          });
          navigation.navigate('DuesMode');
        },
      },
    );
  }

  return (
    <AuthLayout scrollable>
      <WizardShell
        step="Property"
        title="Parlez-nous de votre copropriété"
        subtitle="Ces informations permettront d'identifier votre copropriété."
        onBack={() => navigation.goBack()}
      >
        <View style={styles.form}>
          {registerBoardAdmin.isError && <Alert message={getErrorMessage(registerBoardAdmin.error)} />}
          {!password && !alreadyRegistered && (
            <Alert
              variant="warning"
              message="Votre mot de passe n'a pas été conservé. Revenez à l'étape précédente pour le saisir à nouveau."
            />
          )}
          <ControlledInput control={control} name="name" label="Nom de la copropriété" errorMessage={errors.name?.message} />
          <ControlledInput
            control={control}
            name="address"
            label="Adresse"
            autoComplete="street-address"
            errorMessage={errors.address?.message}
          />
          <ControlledInput
            control={control}
            name="addressComplement"
            label="Complément d'adresse (optionnel)"
            errorMessage={errors.addressComplement?.message}
          />
          <ControlledInput
            control={control}
            name="postalCode"
            label="Code postal"
            autoComplete="postal-code"
            errorMessage={errors.postalCode?.message}
          />
          <ControlledInput control={control} name="city" label="Ville" errorMessage={errors.city?.message} />
          <Button
            onPress={handleSubmit(onSubmit)}
            isLoading={registerBoardAdmin.isPending}
            disabled={!password && !alreadyRegistered}
          >
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
});
