import { Link } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { CheckCircleIcon } from '@/shared/icons';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';

export function OnboardingDonePage() {
  const { draft } = useOnboarding();
  const propertyId = draft.registration?.propertyId;

  return (
    <Card className="w-full max-w-2xl text-center">
      <span className="mx-auto mb-4 flex size-12 items-center justify-center rounded-full bg-success-50 text-success-600 dark:bg-success-500/15 dark:text-success-500">
        <CheckCircleIcon className="size-6" />
      </span>
      <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Votre copropriété est créée !</h1>
      <p className="mt-1 text-sm text-gray-600 dark:text-gray-400">
        Votre espace de gestion est prêt. Vous pouvez maintenant ajouter les copropriétaires et compléter les
        informations de vos lots.
      </p>

      {/* Le compte n'est pas encore vérifié : sans ce rappel, les deux boutons
          ci-dessous renverraient vers un écran de connexion inexplicable. */}
      <p className="mt-4 rounded-lg bg-gray-50 p-3 text-sm text-gray-600 dark:bg-white/[0.03] dark:text-gray-400">
        Un email de confirmation vous a été envoyé à <strong>{draft.account.email}</strong>. Activez votre compte
        pour accéder à votre espace.
      </p>

      <div className="mt-6 flex flex-col gap-3">
        <Link
          to="/login"
          className="inline-flex min-h-11 items-center justify-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
        >
          Accéder à ma copropriété
        </Link>
        <Link
          to={propertyId ? `/property-mngt/properties/${propertyId}/property/invitations` : '/login'}
          className="inline-flex min-h-11 items-center justify-center rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-theme-xs ring-1 ring-inset ring-gray-300 hover:bg-gray-50 dark:bg-gray-800 dark:text-gray-400 dark:ring-gray-700 dark:hover:bg-white/[0.03] dark:hover:text-gray-300"
        >
          Ajouter des copropriétaires
        </Link>
      </div>
    </Card>
  );
}
