import { useState } from 'react';
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
  // N'affiche les prix manquants qu'après une tentative de « Continuer » - pas
  // dès l'arrivée sur l'étape, où aucun champ n'a encore pu être rempli.
  const [attemptedContinue, setAttemptedContinue] = useState(false);

  function isPriceMissing(name: string) {
    return (draft.unitTypePrices[name] ?? '') === '';
  }

  const hasMissingPrice = isFlatRate && draft.selectedUnitTypes.some(isPriceMissing);

  function handleContinue() {
    if (hasMissingPrice) {
      setAttemptedContinue(true);
      return;
    }
    navigate(nextStepPath('unit-types')!);
  }

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
      subtitle={
        isFlatRate
          ? 'Sélectionnez les types de lots présents dans votre copropriété et indiquez le montant de chacun.'
          : 'Sélectionnez les types de lots présents dans votre copropriété.'
      }
      backPath={previousStepPath('unit-types')}
    >
      <div className="flex flex-col gap-3">
        {UNIT_TYPE_CHOICES.map((choice) => {
          const isSelected = draft.selectedUnitTypes.includes(choice.name);
          return (
            // Le montant se saisit sur la ligne même du type qu'il chiffre, à côté
            // de sa case : le geste « je coche, je chiffre » se fait sur place, sans
            // aller le rechercher dans une liste séparée plus bas. Hors du <label>
            // de la carte, sinon cliquer dans le champ décocherait le type.
            <div key={choice.name} className="flex items-start gap-3">
              <div className="min-w-0 flex-1">
                <SelectableCard
                  type="checkbox"
                  name="unit-types"
                  value={choice.name}
                  checked={isSelected}
                  onSelect={toggleType}
                  title={choice.name}
                  description={UNIT_TYPE_DESCRIPTIONS[choice.name]}
                />
              </div>
              {/* En tantièmes, ce sont le budget prévisionnel et les tantièmes de
                  chaque lot qui servent au calcul, jamais un prix par type. */}
              {isFlatRate && isSelected && (
                <div className="w-32 shrink-0">
                  <Input
                    label="Montant (MAD)"
                    aria-label={`Montant ${choice.name}`}
                    type="number"
                    min={0}
                    step="0.01"
                    id={`price-${choice.name}`}
                    name={`price-${choice.name}`}
                    placeholder="0"
                    value={draft.unitTypePrices[choice.name] ?? ''}
                    onChange={(event) => setPrice(choice.name, event.target.value)}
                    errorMessage={attemptedContinue && isPriceMissing(choice.name) ? 'Requis' : undefined}
                  />
                </div>
              )}
            </div>
          );
        })}

        {draft.selectedUnitTypes.length === 0 && (
          <Alert variant="warning" message="Sélectionnez au moins un type de lot pour continuer." />
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

        <Button type="button" disabled={draft.selectedUnitTypes.length === 0} onClick={handleContinue}>
          Continuer
        </Button>
      </div>
    </WizardShell>
  );
}
