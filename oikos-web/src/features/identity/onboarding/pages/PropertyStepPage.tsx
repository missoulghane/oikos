import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useAuthStore } from '@/app/store';
import { useCreateBoardAdminProperty } from '@/features/identity/onboarding/hooks/useCreateBoardAdminProperty';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { isOnboardingTokenUsable } from '@/features/identity/onboarding/state/onboardingDraft';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { BoardSeatSelect } from '@/features/identity/onboarding/components/BoardSeatSelect';
import { nextStepPath, previousStepPath, stepPath } from '@/features/identity/onboarding/constants/steps';
import { propertyStepSchema, type PropertyStepValues } from '@/features/identity/onboarding/schemas/onboardingSchemas';

export function PropertyStepPage() {
  const navigate = useNavigate();
  const { draft, update, reset } = useOnboarding();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const createProperty = useCreateBoardAdminProperty();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<PropertyStepValues>({
    // Erreurs signalées à la sortie du champ, puis à chaque frappe - comme sur
    // tous les formulaires - plutôt que seulement à la validation.
    mode: 'onTouched',
    resolver: zodResolver(propertyStepSchema),
    defaultValues: { ...draft.property, boardRole: draft.account.boardRole },
  });

  // Une copropriété déjà créée, sur laquelle on revient par le récapitulatif.
  // Continuer la reprend - c'est voulu -, mais plus en silence : le nom et
  // l'adresse saisis ici ne repartiraient nulle part, la copropriété ayant été
  // créée avec ceux du premier passage.
  const alreadyRegistered = draft.registration !== null;
  const accountRegistration = draft.accountRegistration;
  const accountTokenUsable = accountRegistration !== null && isOnboardingTokenUsable(accountRegistration);
  // Reprise connectée : un visiteur parti après l'étape 1 a vérifié son email et
  // s'est connecté. Il n'a pas repassé l'étape 1 dans cet onglet, d'où le choix
  // du siège proposé ici plutôt que perdu.
  const isResumingSignedIn = accountRegistration === null && isAuthenticated;
  const canCreate = accountTokenUsable || isAuthenticated;

  function onSubmit(values: PropertyStepValues) {
    const property = { name: values.name, address: values.address, city: values.city };
    update({ property, account: { ...draft.account, boardRole: values.boardRole } });

    if (alreadyRegistered) {
      navigate(nextStepPath('property')!);
      return;
    }

    createProperty.mutate(
      {
        payload: {
          propertyName: property.name,
          // L'adresse et la ville partent séparément depuis que la copropriété
          // porte les deux : les recoller ici les aurait rendues inséparables sur
          // la fiche, où le syndic doit pouvoir corriger l'une sans l'autre.
          propertyAddress: property.address,
          propertyCity: property.city,
          boardRole: values.boardRole,
        },
        // Jeton périmé ou absent : l'intercepteur pose celui de la session, que
        // l'API accepte aussi (canCreateBoardAdminProperty).
        onboardingToken: accountTokenUsable ? accountRegistration!.onboardingToken : undefined,
      },
      {
        onSuccess: (created) => {
          update({
            property,
            registration: {
              propertyId: created.propertyId,
              onboardingToken: created.onboardingToken,
              onboardingTokenExpiresAt: Date.now() + created.expiresInSeconds * 1000,
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
        {createProperty.isError && <Alert message={getErrorMessage(createProperty.error)} />}
        {alreadyRegistered && (
          <div className="flex flex-col gap-2">
            <Alert
              variant="warning"
              message={`Une copropriété a déjà été créée : « ${draft.property.name} ». Continuer reprendra sa configuration — le nom et l'adresse ci-dessous ne seront pas réappliqués.`}
            />
            {/* La seule sortie honnête : le compte et la copropriété précédents
                existent bel et bien, on ne peut pas les recréer. Repartir de
                zéro efface le brouillon, pas ce qui a été créé. */}
            <button
              type="button"
              onClick={reset}
              className="self-start text-sm font-medium text-brand-500 underline dark:text-brand-400"
            >
              Recommencer avec une nouvelle copropriété
            </button>
          </div>
        )}
        {!alreadyRegistered && !canCreate && <MissingAccountNotice accountCreated={accountRegistration !== null} />}
        <Input label="Nom de la copropriété" {...register('name')} errorMessage={errors.name?.message} />
        <Input label="Adresse" autoComplete="street-address" {...register('address')} errorMessage={errors.address?.message} />
        <Input label="Ville" autoComplete="address-level2" {...register('city')} errorMessage={errors.city?.message} />
        {isResumingSignedIn && !alreadyRegistered && (
          <BoardSeatSelect {...register('boardRole')} errorMessage={errors.boardRole?.message} />
        )}
        <Button type="submit" isLoading={createProperty.isPending} disabled={!canCreate && !alreadyRegistered}>
          Continuer
        </Button>
      </form>
    </WizardShell>
  );
}

/**
 * Pas de compte utilisable pour créer la copropriété : soit l'étape 1 n'a pas
 * été faite dans cet onglet (rechargement, lien direct), soit son jeton a
 * expiré (2 h). Dans le second cas le compte existe : la sortie est de
 * l'activer, se connecter, et revenir ici.
 */
function MissingAccountNotice({ accountCreated }: { accountCreated: boolean }) {
  if (accountCreated) {
    return (
      <div className="flex flex-col gap-2">
        <Alert
          variant="warning"
          message="Votre lien d'inscription a expiré (il est valable 2 heures). Votre compte est bien créé : activez-le depuis l'email reçu, connectez-vous, puis revenez sur cette page."
        />
        <Link
          to={`/login?returnTo=${encodeURIComponent(stepPath('property'))}`}
          className="text-sm font-medium text-brand-500 underline dark:text-brand-400"
        >
          Se connecter pour reprendre
        </Link>
      </div>
    );
  }
  return (
    <div className="flex flex-col gap-2">
      <Alert variant="warning" message="Commencez par créer votre compte avant de renseigner votre copropriété." />
      <Link to={stepPath('account')} className="text-sm font-medium text-brand-500 underline dark:text-brand-400">
        Créer mon compte
      </Link>
    </div>
  );
}
