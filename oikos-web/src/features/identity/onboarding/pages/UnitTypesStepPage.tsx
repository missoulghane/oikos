import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { SelectableCard } from '@/shared/components/SelectableCard/SelectableCard';
import { nextStepPath, previousStepPath } from '@/features/identity/onboarding/constants/steps';
import { Loader } from '@/shared/components/Loader/Loader';
import { useUnitTypeChoices } from '@/shared/hooks/useUnitTypeChoices';

/**
 * Le choix des types seul : leurs montants viennent deux étapes plus loin, une
 * fois le mode de gestion connu - en tantièmes, il n'y en a pas. Types et
 * descriptions viennent du catalogue (table unit_type_choice).
 */
export function UnitTypesStepPage() {
  const navigate = useNavigate();
  const { draft, update } = useOnboarding();
  const unitTypeChoices = useUnitTypeChoices();
  const choices = unitTypeChoices.data ?? [];

  function toggleType(name: string) {
    const selected = draft.selectedUnitTypes.includes(name)
      ? draft.selectedUnitTypes.filter((candidate) => candidate !== name)
      : // Conserve l'ordre d'affichage plutôt que l'ordre de clic, pour que les
        // montants et les bâtiments listent toujours les types dans le même ordre.
        choices.filter(
          (choice) => choice.name === name || draft.selectedUnitTypes.includes(choice.name),
        ).map((choice) => choice.name);
    update({ selectedUnitTypes: selected });
  }

  return (
    <WizardShell
      step="unit-types"
      title="Quels types de lots gérez-vous ?"
      subtitle="Sélectionnez les types de lots présents dans votre copropriété."
      backPath={previousStepPath('unit-types')}
    >
      <div className="flex flex-col gap-3">
        {unitTypeChoices.isLoading && <Loader label="Chargement des types de lots…" />}
        {choices.map((choice) => (
          <SelectableCard
            key={choice.name}
            type="checkbox"
            name="unit-types"
            value={choice.name}
            checked={draft.selectedUnitTypes.includes(choice.name)}
            onSelect={toggleType}
            title={choice.name}
            description={choice.description}
          />
        ))}

        {draft.selectedUnitTypes.length === 0 && (
          <Alert variant="warning" message="Sélectionnez au moins un type de lot pour continuer." />
        )}

        <Button
          type="button"
          disabled={draft.selectedUnitTypes.length === 0}
          onClick={() => navigate(nextStepPath('unit-types')!)}
        >
          Continuer
        </Button>
      </div>
    </WizardShell>
  );
}
