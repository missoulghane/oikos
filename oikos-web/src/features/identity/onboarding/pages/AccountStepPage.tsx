import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { useCaptureOnboardingLead } from '@/features/identity/onboarding/hooks/useCaptureOnboardingLead';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { nextStepPath } from '@/features/identity/onboarding/constants/steps';
import {
  accountStepSchema,
  PASSWORD_MIN_LENGTH,
  type AccountStepValues,
} from '@/features/identity/onboarding/schemas/onboardingSchemas';

export function AccountStepPage() {
  const navigate = useNavigate();
  const { draft, update, password, setPassword } = useOnboarding();
  const captureLead = useCaptureOnboardingLead();

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<AccountStepValues>({
    resolver: zodResolver(accountStepSchema),
    defaultValues: { ...draft.account, password, confirmPassword: password },
  });

  // Le mot de passe n'est pas persisté (voir OnboardingProvider) : sur une reprise
  // après rechargement, les deux champs repartent vides plutôt que de laisser
  // croire à une saisie encore présente.
  useEffect(() => {
    if (!password) {
      setValue('password', '');
      setValue('confirmPassword', '');
    }
  }, [password, setValue]);

  const typedPassword = watch('password') ?? '';

  function onSubmit(values: AccountStepValues) {
    update({ account: { firstName: values.firstName, lastName: values.lastName, email: values.email } });
    setPassword(values.password);
    // Capture d'email « au mieux » : le compte n'existe qu'à la fin de l'étape 2,
    // et un échec ici ne doit surtout pas bloquer la progression du visiteur.
    captureLead.mutate({ email: values.email, firstName: values.firstName, lastName: values.lastName });
    navigate(nextStepPath('account')!);
  }

  return (
    <WizardShell step="account" title="Créez votre compte" subtitle="Quelques informations pour créer votre compte de syndic bénévole.">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        <div className="grid gap-4 sm:grid-cols-2">
          <Input label="Prénom" autoComplete="given-name" {...register('firstName')} errorMessage={errors.firstName?.message} />
          <Input label="Nom" autoComplete="family-name" {...register('lastName')} errorMessage={errors.lastName?.message} />
        </div>
        <Input label="Adresse email" type="email" autoComplete="email" {...register('email')} errorMessage={errors.email?.message} />
        {/* Pas d'errorMessage ici : la checklist juste en dessous porte déjà la
            règle, et l'afficher deux fois (en rouge puis en gris) brouille plus
            qu'il n'informe. */}
        <Input label="Mot de passe" type="password" autoComplete="new-password" {...register('password')} />
        <ul className="-mt-2 flex flex-col gap-1 text-sm">
          <PasswordRule
            satisfied={typedPassword.length >= PASSWORD_MIN_LENGTH}
            label={`Au moins ${PASSWORD_MIN_LENGTH} caractères`}
          />
        </ul>
        <Input
          label="Confirmation du mot de passe"
          type="password"
          autoComplete="new-password"
          {...register('confirmPassword')}
          errorMessage={errors.confirmPassword?.message}
        />
        <Button type="submit">Continuer</Button>
      </form>
      <p className="mt-4 text-center text-sm text-gray-600 dark:text-gray-400">
        Déjà un compte ?{' '}
        <Link to="/login" className="font-medium text-gray-900 underline dark:text-white/90">
          Se connecter
        </Link>
      </p>
    </WizardShell>
  );
}

function PasswordRule({ satisfied, label }: { satisfied: boolean; label: string }) {
  return (
    <li
      className={`flex items-center gap-2 ${
        satisfied ? 'text-success-600 dark:text-success-500' : 'text-gray-500 dark:text-gray-400'
      }`}
    >
      <span aria-hidden="true">{satisfied ? '✓' : '○'}</span>
      {label}
    </li>
  );
}
