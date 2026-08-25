import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { Alert } from '@/shared/components/Alert/Alert';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { SelectableCard } from '@/shared/components/SelectableCard/SelectableCard';
import { nextStepPath, previousStepPath } from '@/features/identity/onboarding/constants/steps';
import { UNIT_TYPE_CHOICES } from '@/features/identity/onboarding/state/onboardingDraft';

const UNIT_TYPE_DESCRIPTIONS: Record<string, string> = {
  Appartement: 'Logements de la copropriété.',
  Box: 'Garages et emplacements de stationnement.',
  Bureau: 'Locaux professionnels.',
};

export function UnitTypesStepPage() {
  const navigate = useNavigate();
  const { draft, update } = useOnboarding();
  const isFlatRate = draft.duesCalculationMode === 'FLAT_RATE';

  function toggleType(name: string) {
    const selected = draft.selectedUnitTypes.includes(name)
      ? draft.selectedUnitTypes.filter((candidate) => candidate !== name)
      : // Conserve l'ordre d'affichage plutôt que l'ordre de clic, pour que les
        // montants et les bâtiments listent toujours les types dans le même ordre.
        UNIT_TYPE_CHOICES.filter(
          (choice) => choice.name === name || draft.selectedUnitTypes.includes(choice.name),
        ).map((choice) => choice.name);
    update({ selectedUnitTypes: selected });
  }

  function setPrice(name: string, price: string) {
    update({ unitTypePrices: { ...draft.unitTypePrices, [name]: price } });
  }

  return (
    <WizardShell
      step="unit-types"
      title="Quels types de lots gérez-vous ?"
      subtitle="Sélectionnez les types de lots présents dans votre copropriété."
      backPath={previousStepPath('unit-types')}
    >
      <div className="flex flex-col gap-3">
        {UNIT_TYPE_CHOICES.map((choice) => (
          <SelectableCard
            key={choice.name}
            type="checkbox"
            name="unit-types"
            value={choice.name}
            checked={draft.selectedUnitTypes.includes(choice.name)}
            onSelect={toggleType}
            title={choice.name}
            description={UNIT_TYPE_DESCRIPTIONS[choice.name]}
          />
        ))}

        {draft.selectedUnitTypes.length === 0 && (
          <Alert variant="warning" message="Sélectionnez au moins un type de lot pour continuer." />
        )}

        {/* La section des montants n'a de sens qu'au forfait : en tantièmes, ce
            sont le budget prévisionnel et les tantièmes de chaque lot qui servent
            au calcul, jamais un prix par type. */}
        {isFlatRate && draft.selectedUnitTypes.length > 0 && (
          <section className="mt-2 flex flex-col gap-3 rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
            <div>
              <h2 className="text-sm font-medium text-gray-900 dark:text-white/90">Montant par type de lot</h2>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Indiquez le montant associé à chaque type de lot.
              </p>
            </div>
            {draft.selectedUnitTypes.map((name) => (
              <Input
                key={name}
                label={name}
                type="number"
                min={0}
                step="0.01"
                name={`price-${name}`}
                placeholder="0"
                value={draft.unitTypePrices[name] ?? ''}
                onChange={(event) => setPrice(name, event.target.value)}
              />
            ))}
          </section>
        )}

        {!isFlatRate && (
          <section className="mt-2 flex flex-col gap-3 rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
            <div>
              <h2 className="text-sm font-medium text-gray-900 dark:text-white/90">Budget prévisionnel</h2>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                En tantièmes, les charges se répartissent à partir de ce budget. Vous pourrez le renseigner
                plus tard, ainsi que les tantièmes de chaque lot.
              </p>
            </div>
            {/* Le titre de l'encadré dit déjà « Budget prévisionnel » : l'étiquette
                du champ précise l'unité de temps plutôt que de répéter le titre -
                c'est aussi le libellé de l'écran de configuration du syndic. */}
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
          </section>
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
