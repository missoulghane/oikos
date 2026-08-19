import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { PhoneField } from '@/shared/components/PhoneField/PhoneField';
import { Button } from '@/shared/components/Button/Button';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { useCaptureOnboardingLead } from '@/features/identity/onboarding/hooks/useCaptureOnboardingLead';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { nextStepPath } from '@/features/identity/onboarding/constants/steps';
import { accountStepSchema, type AccountStepValues } from '@/features/identity/onboarding/schemas/onboardingSchemas';

export function AccountStepPage() {
  const navigate = useNavigate();
  const { draft, update, password, setPassword } = useOnboarding();
  const captureLead = useCaptureOnboardingLead();

  const {
    register,
    control,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<AccountStepValues>({
    resolver: zodResolver(accountStepSchema),
    // Validation dès qu'un champ a été quitté, puis à chaque frappe : la
    // concordance des deux mots de passe se voit sur place, sans attendre le
    // « Continuer » ni le moindre aller-retour serveur (l'étape 1 n'en fait
    // aucun sur le mot de passe, il n'est envoyé qu'à l'étape 2).
    mode: 'onTouched',
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

  function onSubmit(values: AccountStepValues) {
    update({ account: { fullName: values.fullName, email: values.email, phone: values.phone } });
    setPassword(values.password);
    // Capture d'email « au mieux » : le compte n'existe qu'à la fin de l'étape 2,
    // et un échec ici ne doit surtout pas bloquer la progression du visiteur.
    captureLead.mutate({ email: values.email, fullName: values.fullName });
    navigate(nextStepPath('account')!);
  }

  return (
    <WizardShell step="account" title="Créez votre compte" subtitle="Quelques informations pour créer votre compte de syndic bénévole.">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        <Input label="Nom complet" autoComplete="name" {...register('fullName')} errorMessage={errors.fullName?.message} />
        <Input label="Adresse email" type="email" autoComplete="email" {...register('email')} errorMessage={errors.email?.message} />
        <Controller
          control={control}
          name="phone"
          // Comme sur les deux autres formulaires qui portent un téléphone : le
          // défaut vient du brouillon, et un brouillon peut toujours arriver
          // amputé d'un champ ajouté après lui.
          defaultValue=""
          render={({ field }) => (
            <PhoneField
              label="Téléphone"
              name={field.name}
              value={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              errorMessage={errors.phone?.message}
            />
          )}
        />
        {/* La règle de longueur ne s'affiche qu'en cas de manquement : une
            checklist verte permanente occupe la place et le regard pour dire
            que tout va bien, ce que l'absence d'erreur dit déjà. */}
        <Input
          label="Mot de passe"
          type="password"
          autoComplete="new-password"
          // deps : corriger le premier champ doit rafraîchir l'erreur de
          // concordance portée par le second, qui sinon resterait affichée
          // alors que les deux saisies viennent de redevenir identiques.
          {...register('password', { deps: ['confirmPassword'] })}
          errorMessage={errors.password?.message}
        />
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
