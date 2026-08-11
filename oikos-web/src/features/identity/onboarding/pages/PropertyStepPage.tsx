import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useRegisterPropertyBoardAdmin } from '@/features/identity/register/hooks/useRegisterPropertyBoardAdmin';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { nextStepPath, previousStepPath } from '@/features/identity/onboarding/constants/steps';
import { formatAddress, fullName } from '@/features/identity/onboarding/state/onboardingDraft';
import {
  propertyStepSchema,
  PROPERTY_ADDRESS_MAX_LENGTH,
  type PropertyStepValues,
} from '@/features/identity/onboarding/schemas/onboardingSchemas';

export function PropertyStepPage() {
  const navigate = useNavigate();
  const { draft, update, password } = useOnboarding();
  const register_ = useRegisterPropertyBoardAdmin();

  const {
    register,
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
      navigate(nextStepPath('property')!);
      return;
    }

    register_.mutate(
      {
        fullName: fullName(draft.account),
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
          navigate(nextStepPath('property')!);
        },
      },
    );
  }

  return (
    <WizardShell
      step="property"
      title="Parlez-nous de votre copropriété"
      subtitle="Ces informations permettront d'identifier votre copropriété."
      backPath={previousStepPath('property')}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        {register_.isError && <Alert message={getErrorMessage(register_.error)} />}
        {!password && !alreadyRegistered && (
          <Alert
            variant="warning"
            message="Votre mot de passe n'a pas été conservé. Revenez à l'étape précédente pour le saisir à nouveau."
          />
        )}
        <Input label="Nom de la copropriété" {...register('name')} errorMessage={errors.name?.message} />
        <Input label="Adresse" autoComplete="street-address" {...register('address')} errorMessage={errors.address?.message} />
        <Input
          label="Complément d'adresse (optionnel)"
          {...register('addressComplement')}
          errorMessage={errors.addressComplement?.message}
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <Input label="Code postal" autoComplete="postal-code" {...register('postalCode')} errorMessage={errors.postalCode?.message} />
          <Input label="Ville" autoComplete="address-level2" {...register('city')} errorMessage={errors.city?.message} />
        </div>
        <Button type="submit" isLoading={register_.isPending} disabled={!password && !alreadyRegistered}>
          Continuer
        </Button>
      </form>
    </WizardShell>
  );
}
