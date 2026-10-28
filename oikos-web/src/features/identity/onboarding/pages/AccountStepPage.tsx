import { Link, useNavigate } from 'react-router-dom';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { PhoneField } from '@/shared/components/PhoneField/PhoneField';
import { Checkbox } from '@/shared/components/Checkbox/Checkbox';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { useRegisterPropertyBoardAdmin } from '@/features/identity/register/hooks/useRegisterPropertyBoardAdmin';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { BoardSeatSelect } from '@/features/identity/onboarding/components/BoardSeatSelect';
import { nextStepPath } from '@/features/identity/onboarding/constants/steps';
import { TERMS_OF_SALE_PATH, TERMS_OF_USE_PATH } from '@/features/legal';
import { accountStepSchema, type AccountStepValues } from '@/features/identity/onboarding/schemas/onboardingSchemas';

export function AccountStepPage() {
  const navigate = useNavigate();
  const { draft, update, reset } = useOnboarding();
  const registerAccount = useRegisterPropertyBoardAdmin();

  const {
    register,
    control,
    handleSubmit,
    formState: { errors, touchedFields },
  } = useForm<AccountStepValues>({
    resolver: zodResolver(accountStepSchema),
    // Validation dès qu'un champ a été quitté, puis à chaque frappe : la
    // concordance des deux mots de passe se voit sur place, sans attendre le
    // « Continuer » ni le moindre aller-retour serveur.
    mode: 'onTouched',
    // Le mot de passe n'est jamais prérempli : il part vers l'API à la
    // soumission de cette étape et n'est conservé nulle part ensuite.
    defaultValues: { ...draft.account, password: '', confirmPassword: '' },
  });

  // Le compte est créé ici, dès l'étape 1 : revenir sur cette étape ne doit pas
  // le recréer (l'email est déjà pris), seulement laisser poursuivre.
  if (draft.accountRegistration) {
    return (
      <WizardShell step="account" title="Votre compte est créé" subtitle={draft.account.email}>
        <div className="flex flex-col gap-4">
          <Alert
            variant="success"
            message="Un email de confirmation vous a été envoyé. Vous pouvez poursuivre la création de votre copropriété dès maintenant."
          />
          <Button type="button" onClick={() => navigate(nextStepPath('account')!)}>
            Continuer
          </Button>
          {/* Même sortie que sur l'étape 2 : le compte créé existe bel et bien,
              repartir de zéro efface le brouillon pour en créer un autre. */}
          <button
            type="button"
            onClick={reset}
            className="self-start text-sm font-medium text-brand-500 underline dark:text-brand-400"
          >
            Recommencer avec un autre compte
          </button>
        </div>
      </WizardShell>
    );
  }

  function onSubmit(values: AccountStepValues) {
    const account = {
      fullName: values.fullName,
      email: values.email,
      phone: values.phone,
      boardRole: values.boardRole,
      acceptedTerms: values.acceptedTerms,
    };
    update({ account });
    registerAccount.mutate(
      {
        fullName: values.fullName,
        email: values.email,
        phone: values.phone,
        password: values.password,
        acceptedTerms: values.acceptedTerms,
      },
      {
        onSuccess: (registration) => {
          update({
            account,
            accountRegistration: {
              userId: registration.userId,
              onboardingToken: registration.onboardingToken,
              onboardingTokenExpiresAt: Date.now() + registration.expiresInSeconds * 1000,
            },
          });
          navigate(nextStepPath('account')!);
        },
      },
    );
  }

  return (
    <WizardShell
      step="account"
      title="Créez votre compte"
      subtitle="L'inscription est réservée au syndic et aux membres du conseil syndical de la copropriété."
    >
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        {registerAccount.isError && <Alert message={getErrorMessage(registerAccount.error)} />}
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
        <BoardSeatSelect {...register('boardRole')} errorMessage={errors.boardRole?.message} />
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
          // Revalider ce champ dès que le mot de passe change (deps ci-dessus) sert
          // à rafraîchir une erreur de concordance déjà affichée - pas à en faire
          // apparaître une avant que l'utilisateur n'ait lui-même quitté ce champ.
          errorMessage={touchedFields.confirmPassword ? errors.confirmPassword?.message : undefined}
        />
        <Checkbox
          label={
            <span>
              J'accepte les{' '}
              <TermsLink to={TERMS_OF_USE_PATH}>conditions générales d'utilisation</TermsLink> et les{' '}
              <TermsLink to={TERMS_OF_SALE_PATH}>conditions générales de vente</TermsLink>
            </span>
          }
          {...register('acceptedTerms')}
          errorMessage={errors.acceptedTerms?.message}
        />
        <Button type="submit" isLoading={registerAccount.isPending}>
          Continuer
        </Button>
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

/**
 * Nouvel onglet : quitter l'étape pour lire les conditions ferait perdre la
 * saisie en cours, mots de passe compris (ils ne sont jamais conservés).
 */
function TermsLink({ to, children }: { to: string; children: string }) {
  return (
    <a href={to} target="_blank" rel="noopener noreferrer" className="font-medium text-brand-500 underline dark:text-brand-400">
      {children}
    </a>
  );
}
