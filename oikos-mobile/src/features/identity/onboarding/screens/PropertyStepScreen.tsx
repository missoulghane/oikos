import { StyleSheet, View } from 'react-native';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { ControlledInput } from '@/shared/components/Input/ControlledInput';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useCreateBoardAdminProperty } from '@/features/identity/onboarding/hooks/useCreateBoardAdminProperty';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { formatStoredAddress } from '@/features/identity/onboarding/state/onboardingDraft';
import {
  propertyStepSchema,
  PROPERTY_ADDRESS_MAX_LENGTH,
  type PropertyStepValues,
} from '@/features/identity/onboarding/schemas/onboardingSchemas';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'Property'>;

export function PropertyStepScreen({ navigation }: Props) {
  const { draft, update } = useOnboarding();
  const createProperty = useCreateBoardAdminProperty();

  const {
    control,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<PropertyStepValues>({
    // Erreurs signalées à la sortie du champ, puis à chaque frappe - comme sur
    // tous les formulaires - plutôt que seulement à la validation.
    mode: 'onTouched',
    resolver: zodResolver(propertyStepSchema),
    defaultValues: draft.property,
  });

  const alreadyRegistered = draft.registration !== null;
  const accountRegistration = draft.accountRegistration;

  function onSubmit(values: PropertyStepValues) {
    const property = {
      name: values.name,
      address: values.address,
      addressComplement: values.addressComplement ?? '',
      postalCode: values.postalCode ?? '',
      city: values.city,
    };
    const address = formatStoredAddress(property);
    if (address.length > PROPERTY_ADDRESS_MAX_LENGTH) {
      setError('address', { message: `Adresse complète trop longue (${PROPERTY_ADDRESS_MAX_LENGTH} caractères maximum)` });
      return;
    }
    update({ property });

    // La copropriété est créée ici, pour le compte né à l'étape 1 : la party qui
    // relie le compte à sa copropriété ne peut pas exister avant que celle-ci ait
    // un nom.
    if (alreadyRegistered) {
      navigation.navigate('UnitTypes');
      return;
    }
    if (!accountRegistration) {
      return;
    }

    createProperty.mutate(
      {
        payload: {
          propertyName: property.name,
          propertyAddress: address,
          propertyCity: property.city,
          boardRole: draft.account.boardRole,
        },
        onboardingToken: accountRegistration.onboardingToken,
      },
      {
        onSuccess: (created) => {
          update({
            property,
            registration: {
              propertyId: created.propertyId,
              onboardingToken: created.onboardingToken,
            },
          });
          navigation.navigate('UnitTypes');
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
          {createProperty.isError && <Alert message={getErrorMessage(createProperty.error)} />}
          {!accountRegistration && !alreadyRegistered && (
            <Alert
              variant="warning"
              message="Commencez par créer votre compte : revenez à l'étape précédente."
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
            isLoading={createProperty.isPending}
            disabled={!accountRegistration && !alreadyRegistered}
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
