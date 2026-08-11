import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { Stepper } from '@/shared/components/Stepper/Stepper';
import { ChevronDownIcon } from '@/shared/icons';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { nextStepPath, previousStepPath } from '@/features/identity/onboarding/constants/steps';
import {
  buildingSummary,
  resizeBuildings,
  unitCountOf,
  type OnboardingBuilding,
} from '@/features/identity/onboarding/state/onboardingDraft';

export function BuildingsStepPage() {
  const navigate = useNavigate();
  const { draft, update } = useOnboarding();
  const [openBuildingIndex, setOpenBuildingIndex] = useState(0);
  const hasSeveralBuildings = draft.buildings.length > 1;

  function setBuildingCount(count: number) {
    update({ buildings: resizeBuildings(draft.buildings, count) });
    setOpenBuildingIndex((previous) => Math.min(previous, count - 1));
  }

  function patchBuilding(index: number, patch: Partial<OnboardingBuilding>) {
    update({
      buildings: draft.buildings.map((building, position) =>
        position === index ? { ...building, ...patch } : building,
      ),
    });
  }

  function setUnitCount(index: number, unitTypeName: string, count: number) {
    const building = draft.buildings[index];
    patchBuilding(index, { unitCounts: { ...building.unitCounts, [unitTypeName]: count } });
  }

  return (
    <WizardShell
      step="buildings"
      title="Structure de votre copropriété"
      subtitle="Indiquez combien de bâtiments composent votre copropriété et combien de lots ils contiennent."
      backPath={previousStepPath('buildings')}
    >
      <div className="flex flex-col gap-4">
        <div className="rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
          <Stepper label="Combien de bâtiments ?" value={draft.buildings.length} onChange={setBuildingCount} min={1} max={50} />
        </div>

        {!hasSeveralBuildings ? (
          <section className="flex flex-col gap-4 rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
            <h2 className="text-sm font-medium text-gray-900 dark:text-white/90">Votre bâtiment</h2>
            <BuildingFields
              building={draft.buildings[0]}
              index={0}
              selectedUnitTypes={draft.selectedUnitTypes}
              onNameChange={(name) => patchBuilding(0, { name })}
              onUnitCountChange={(unitTypeName, count) => setUnitCount(0, unitTypeName, count)}
            />
          </section>
        ) : (
          <div className="flex flex-col gap-2">
            {draft.buildings.map((building, index) => {
              const isOpen = openBuildingIndex === index;
              const summary = buildingSummary(building, draft.selectedUnitTypes);
              return (
                <section key={index} className="rounded-2xl border border-gray-200 dark:border-gray-800">
                  <button
                    type="button"
                    onClick={() => setOpenBuildingIndex(isOpen ? -1 : index)}
                    aria-expanded={isOpen}
                    className="flex w-full items-center justify-between gap-3 p-4 text-left"
                  >
                    <span className="min-w-0">
                      <span className="block truncate text-sm font-medium text-gray-900 dark:text-white/90">
                        {building.name || `Bâtiment ${index + 1}`}
                      </span>
                      <span className="block truncate text-sm text-gray-500 dark:text-gray-400">
                        {summary || 'Aucun lot pour le moment'}
                      </span>
                    </span>
                    <ChevronDownIcon
                      className={`size-5 shrink-0 text-gray-500 transition-transform duration-200 dark:text-gray-400 ${
                        isOpen ? 'rotate-180' : ''
                      }`}
                    />
                  </button>
                  {isOpen && (
                    <div className="flex flex-col gap-4 border-t border-gray-200 p-4 dark:border-gray-800">
                      <BuildingFields
                        building={building}
                        index={index}
                        selectedUnitTypes={draft.selectedUnitTypes}
                        onNameChange={(name) => patchBuilding(index, { name })}
                        onUnitCountChange={(unitTypeName, count) => setUnitCount(index, unitTypeName, count)}
                      />
                    </div>
                  )}
                </section>
              );
            })}
          </div>
        )}

        <Button type="button" onClick={() => navigate(nextStepPath('buildings')!)}>
          Continuer
        </Button>
      </div>
    </WizardShell>
  );
}

interface BuildingFieldsProps {
  building: OnboardingBuilding;
  index: number;
  selectedUnitTypes: string[];
  onNameChange: (name: string) => void;
  onUnitCountChange: (unitTypeName: string, count: number) => void;
}

function BuildingFields({ building, index, selectedUnitTypes, onNameChange, onUnitCountChange }: BuildingFieldsProps) {
  return (
    <>
      <Input
        label="Nom du bâtiment (optionnel)"
        name={`building-name-${index}`}
        placeholder="Bâtiment principal"
        value={building.name}
        onChange={(event) => onNameChange(event.target.value)}
      />
      <div className="flex flex-col gap-3">
        <h3 className="text-sm font-medium text-gray-900 dark:text-white/90">Nombre de lots</h3>
        {/* Exactement les types cochés à l'étape précédente : un type peut être à
            0 ici, c'est simplement qu'il n'existe pas dans ce bâtiment-là. */}
        {selectedUnitTypes.map((unitTypeName) => (
          <Stepper
            key={unitTypeName}
            label={unitTypeName}
            value={unitCountOf(building, unitTypeName)}
            onChange={(count) => onUnitCountChange(unitTypeName, count)}
            min={0}
          />
        ))}
      </div>
    </>
  );
}
