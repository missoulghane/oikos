import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { nextStepPath, previousStepPath } from '@/features/identity/onboarding/constants/steps';

/**
 * Ce que coûte un lot, selon le mode choisi juste avant : un montant par type
 * sélectionné en forfait, le budget prévisionnel en tantièmes - jamais les deux,
 * puisque chaque mode n'en lit qu'un.
 */
export function PricingStepPage() {
  const { draft } = useOnboarding();
  return draft.duesCalculationMode === 'FLAT_RATE' ? <FlatRatePricing /> : <SharesBudget />;
}

function FlatRatePricing() {
  const navigate = useNavigate();
  const { draft, update } = useOnboarding();
  // N'affiche les montants manquants qu'après une tentative de « Continuer » -
  // pas dès l'arrivée sur l'étape, où aucun champ n'a encore pu être rempli.
  const [attemptedContinue, setAttemptedContinue] = useState(false);

  function isPriceMissing(name: string) {
    return (draft.unitTypePrices[name] ?? '') === '';
  }

  function handleContinue() {
    if (draft.selectedUnitTypes.some(isPriceMissing)) {
      setAttemptedContinue(true);
      return;
    }
    navigate(nextStepPath('pricing')!);
  }

  return (
    <WizardShell
      step="pricing"
      title="Quel montant pour chaque type de lot ?"
      subtitle="En forfait, chaque lot est appelé du montant fixé pour son type."
      backPath={previousStepPath('pricing')}
    >
      <div className="flex flex-col gap-4">
        {draft.selectedUnitTypes.map((name) => (
          <Input
            key={name}
            label={`Montant ${name} (MAD)`}
            aria-label={`Montant ${name}`}
            type="number"
            min={0}
            step="0.01"
            id={`price-${name}`}
            name={`price-${name}`}
            placeholder="0"
            value={draft.unitTypePrices[name] ?? ''}
            onChange={(event) => update({ unitTypePrices: { ...draft.unitTypePrices, [name]: event.target.value } })}
            errorMessage={attemptedContinue && isPriceMissing(name) ? 'Le montant est requis' : undefined}
          />
        ))}
        <Button type="button" onClick={handleContinue}>
          Continuer
        </Button>
      </div>
    </WizardShell>
  );
}

function SharesBudget() {
  const navigate = useNavigate();
  const { draft, update } = useOnboarding();

  return (
    <WizardShell
      step="pricing"
      stepLabel="Budget prévisionnel"
      title="Quel est votre budget prévisionnel ?"
      subtitle="En tantièmes, les charges se répartissent à partir de ce budget. Vous pourrez le renseigner plus tard, ainsi que les tantièmes de chaque lot."
      backPath={previousStepPath('pricing')}
    >
      <div className="flex flex-col gap-4">
        {/* Même libellé que l'écran de configuration du syndic. */}
        <Input
          label="Budget prévisionnel annuel"
          type="number"
          min={0}
          step="0.01"
          name="projected-budget"
          placeholder="0"
          value={draft.projectedBudget}
          onChange={(event) => update({ projectedBudget: event.target.value })}
        />
        <Button type="button" onClick={() => navigate(nextStepPath('pricing')!)}>
          Continuer
        </Button>
      </div>
    </WizardShell>
  );
}
