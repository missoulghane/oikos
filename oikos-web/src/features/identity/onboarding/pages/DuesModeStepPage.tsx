import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/components/Button/Button';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { SelectableCard } from '@/features/identity/onboarding/components/SelectableCard';
import { nextStepPath, previousStepPath } from '@/features/identity/onboarding/constants/steps';
import type { DuesCalculationMode } from '@/features/property-mngt/properties/types/property.types';

export function DuesModeStepPage() {
  const navigate = useNavigate();
  const { draft, update } = useOnboarding();

  return (
    <WizardShell
      step="dues-mode"
      title="Comment souhaitez-vous gérer vos charges ?"
      subtitle="Choisissez le mode de gestion utilisé par votre copropriété."
      backPath={previousStepPath('dues-mode')}
    >
      <div className="flex flex-col gap-3">
        <SelectableCard
          name="dues-mode"
          value="FLAT_RATE"
          checked={draft.duesCalculationMode === 'FLAT_RATE'}
          onSelect={(value) => update({ duesCalculationMode: value as DuesCalculationMode })}
          title="Forfait"
          description="Un montant fixe est associé à chaque type de lot."
        />
        <SelectableCard
          name="dues-mode"
          value="SHARES"
          checked={draft.duesCalculationMode === 'SHARES'}
          onSelect={(value) => update({ duesCalculationMode: value as DuesCalculationMode })}
          title="Tantièmes"
          description="Les charges sont réparties selon les tantièmes de chaque lot."
        />
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Vous pourrez modifier ce choix plus tard dans les paramètres de votre copropriété.
        </p>
        <Button type="button" onClick={() => navigate(nextStepPath('dues-mode')!)}>
          Continuer
        </Button>
      </div>
    </WizardShell>
  );
}
