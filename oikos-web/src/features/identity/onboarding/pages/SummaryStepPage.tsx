import type { ReactNode } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { isAxiosError } from 'axios';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { useConfigureProperty } from '@/features/identity/onboarding/hooks/useConfigureProperty';
import { ONBOARDING_DONE_PATH, previousStepPath, stepPath } from '@/features/identity/onboarding/constants/steps';
import {
  buildingSummary,
  formatAddress,
  isOnboardingTokenUsable,
  totalUnitCount,
  unitCountOf,
} from '@/features/identity/onboarding/state/onboardingDraft';
import type { ConfigurePropertyPayload } from '@/features/identity/onboarding/types/onboarding.types';

export function SummaryStepPage() {
  const navigate = useNavigate();
  const { draft } = useOnboarding();
  const configure = useConfigureProperty();
  const isFlatRate = draft.duesCalculationMode === 'FLAT_RATE';

  function toPayload(): ConfigurePropertyPayload {
    const projectedBudget = Number.parseFloat(draft.projectedBudget);
    return {
      duesCalculationMode: draft.duesCalculationMode,
      projectedBudget: !isFlatRate && Number.isFinite(projectedBudget) ? projectedBudget : undefined,
      unitTypes: draft.selectedUnitTypes.map((name) => {
        const price = Number.parseFloat(draft.unitTypePrices[name] ?? '');
        return { name, price: isFlatRate && Number.isFinite(price) ? price : undefined };
      }),
      buildings: draft.buildings.map((building) => ({
        name: building.name.trim() || undefined,
        floorCount: building.floorCount,
        unitTypes: draft.selectedUnitTypes.map((unitTypeName) => ({
          unitTypeName,
          count: unitCountOf(building, unitTypeName),
        })),
      })),
      bankAccounts: draft.bankAccounts
        .filter((account) => account.label.trim() !== '')
        .map((account) => ({
          label: account.label.trim(),
          bankAccountNumber: account.bankAccountNumber.trim() || undefined,
        })),
    };
  }

  function finalize() {
    if (!draft.registration) {
      return;
    }
    configure.mutate(
      {
        propertyId: draft.registration.propertyId,
        payload: toPayload(),
        // Jeton périmé : ne pas l'envoyer laisse l'intercepteur poser celui de la
        // session, que l'API accepte aussi pour un syndic déjà connecté
        // (canConfigureOnboarding). Sinon l'appel partait avec un jeton mort et
        // échouait même pour un compte vérifié qui venait de se connecter.
        onboardingToken: isOnboardingTokenUsable(draft.registration)
          ? draft.registration.onboardingToken
          : undefined,
      },
      { onSuccess: () => navigate(ONBOARDING_DONE_PATH) },
    );
  }

  if (!draft.registration) {
    return (
      <WizardShell
        step="summary"
        title="Vérifiez votre configuration"
        subtitle="Votre compte n'a pas encore été créé."
      >
        <Alert message="Reprenez à l'étape « Votre copropriété » pour créer votre compte avant de finaliser." />
        <Link to={stepPath('property')} className="mt-4 inline-block text-sm font-medium underline">
          Revenir à cette étape
        </Link>
      </WizardShell>
    );
  }

  return (
    <WizardShell
      step="summary"
      title="Vérifiez votre configuration"
      subtitle="Tout est prêt. Vérifiez les informations avant de finaliser votre copropriété."
      backPath={previousStepPath('summary')}
    >
      <div className="flex flex-col gap-4">
        {configure.isError &&
          (isExpiredOnboarding(configure.error) ? (
            <div className="flex flex-col gap-2">
              <Alert
                variant="warning"
                message="Votre lien d'inscription a expiré (il est valable 2 heures). Votre compte et votre copropriété sont bien créés : activez votre compte depuis l'email reçu, connectez-vous, puis revenez sur cette page pour finaliser la configuration."
              />
              <Link
                to={`/login?returnTo=${encodeURIComponent(stepPath('summary'))}`}
                className="text-sm font-medium text-brand-500 underline dark:text-brand-400"
              >
                Se connecter pour reprendre
              </Link>
            </div>
          ) : (
            <Alert message={getErrorMessage(configure.error)} />
          ))}

        <RecapSection title="Votre copropriété" editPath={stepPath('property')}>
          <RecapLine label="Nom" value={draft.property.name} />
          <RecapLine label="Adresse" value={formatAddress(draft.property)} />
        </RecapSection>

        <RecapSection title="Mode de gestion" editPath={stepPath('dues-mode')}>
          <p className="text-sm text-gray-900 dark:text-white/90">{isFlatRate ? 'Forfait' : 'Tantièmes'}</p>
          {!isFlatRate && draft.projectedBudget && (
            <RecapLine label="Budget prévisionnel" value={draft.projectedBudget} />
          )}
        </RecapSection>

        <RecapSection title="Types de lots" editPath={stepPath('unit-types')}>
          {draft.selectedUnitTypes.map((name) => (
            <p key={name} className="text-sm text-gray-900 dark:text-white/90">
              {name}
              {isFlatRate && draft.unitTypePrices[name] ? ` — ${draft.unitTypePrices[name]} MAD` : ''}
            </p>
          ))}
        </RecapSection>

        <RecapSection title="Bâtiments" editPath={stepPath('buildings')}>
          <p className="mb-2 text-sm text-gray-500 dark:text-gray-400">
            {draft.buildings.length} bâtiment{draft.buildings.length > 1 ? 's' : ''} · {totalUnitCount(draft)} lot
            {totalUnitCount(draft) > 1 ? 's' : ''}
          </p>
          {draft.buildings.map((building, index) => (
            <div key={index} className="mb-1">
              <p className="text-sm font-medium text-gray-900 dark:text-white/90">
                {building.name || `Bâtiment ${index + 1}`}
              </p>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                {buildingSummary(building, draft.selectedUnitTypes) || 'Aucun lot'}
              </p>
            </div>
          ))}
        </RecapSection>

        <RecapSection title="Comptes bancaires" editPath={stepPath('bank-accounts')}>
          {draft.bankAccounts.length === 0 ? (
            <p className="text-sm text-gray-500 dark:text-gray-400">Aucun compte déclaré pour le moment.</p>
          ) : (
            draft.bankAccounts.map((account, index) => (
              <p key={index} className="text-sm text-gray-900 dark:text-white/90">
                {account.label}
                {account.bankAccountNumber ? ` — ${account.bankAccountNumber}` : ''}
              </p>
            ))
          )}
        </RecapSection>

        <Button type="button" isLoading={configure.isPending} onClick={finalize}>
          Finaliser ma copropriété
        </Button>
        {/* La copropriété existe depuis l'étape 2 : ce bouton la structure, il ne
            la crée pas - d'où « Finaliser » plutôt que « Créer ». */}
        <p className="text-center text-sm text-gray-500 dark:text-gray-400">
          Votre compte est déjà créé : vous pourrez reprendre cette configuration plus tard si besoin.
        </p>
      </div>
    </WizardShell>
  );
}

/**
 * 401 sur cette étape = le jeton d'onboarding n'est plus valide (expiré, ou
 * brouillon repris depuis un autre navigateur). Le message brut de l'API
 * (« Authentication is required... ») ne dit rien à un visiteur qui n'a jamais
 * eu à se connecter : la sortie est de vérifier son email puis de revenir.
 */
function isExpiredOnboarding(error: unknown): boolean {
  return isAxiosError(error) && error.response?.status === 401;
}

function RecapSection({
  title,
  editPath,
  children,
}: {
  title: string;
  editPath: string;
  children: ReactNode;
}) {
  return (
    <section className="rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
      <div className="mb-2 flex items-center justify-between gap-2">
        <h2 className="text-sm font-semibold text-gray-900 dark:text-white/90">{title}</h2>
        <Link to={editPath} className="text-sm font-medium text-brand-500 underline dark:text-brand-400">
          Modifier
        </Link>
      </div>
      {children}
    </section>
  );
}

function RecapLine({ label, value }: { label: string; value: string }) {
  return (
    <p className="text-sm text-gray-900 dark:text-white/90">
      <span className="text-gray-500 dark:text-gray-400">{label} : </span>
      {value}
    </p>
  );
}
