import { Link } from 'react-router-dom';
import { stepPath } from '@/features/identity/onboarding/constants/steps';

/**
 * Le compte d'un syndic bénévole naît à l'étape 1 du wizard, sa copropriété à
 * l'étape 2 : un visiteur parti entre les deux se connecte sur un espace sans
 * aucune copropriété. Plutôt que de le laisser devant une liste de lots vide,
 * on lui propose de reprendre là où il s'est arrêté - connecté, l'étape 2
 * accepte sa session (voir PropertyStepPage).
 *
 * <p>Formulé comme une proposition et non comme une étape manquante : un
 * copropriétaire dont l'adhésion est encore en attente n'a lui non plus aucune
 * copropriété, et n'a rien oublié.
 */
export function CreatePropertyPrompt() {
  return (
    <div className="flex flex-col gap-3 rounded-2xl border border-brand-200 bg-brand-50 p-4 sm:flex-row sm:items-center sm:justify-between dark:border-brand-400/30 dark:bg-brand-500/[0.12]">
      <div>
        <p className="text-sm font-medium text-gray-900 dark:text-white/90">
          Vous êtes syndic ou membre du conseil syndical ?
        </p>
        <p className="text-sm text-gray-600 dark:text-gray-400">Créez votre copropriété pour commencer à la gérer.</p>
      </div>
      <Link
        to={stepPath('property')}
        className="inline-flex min-h-11 shrink-0 items-center justify-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600"
      >
        Créer ma copropriété
      </Link>
    </div>
  );
}
