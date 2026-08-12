import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { Stepper } from '@/shared/components/Stepper/Stepper';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { colors } from '@/shared/theme/colors';
import {
  buildingSummary,
  resizeBuildings,
  unitCountOf,
  type OnboardingBuilding,
} from '@/features/identity/onboarding/state/onboardingDraft';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'Buildings'>;

export function BuildingsStepScreen({ navigation }: Props) {
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
    <AuthLayout scrollable>
      <WizardShell
        step="Buildings"
        title="Structure de votre copropriété"
        subtitle="Indiquez combien de bâtiments composent votre copropriété et combien de lots ils contiennent."
        onBack={() => navigation.goBack()}
      >
        <View style={styles.container}>
          <View style={styles.section}>
            <Stepper label="Combien de bâtiments ?" value={draft.buildings.length} onChange={setBuildingCount} min={1} max={50} />
          </View>

          {!hasSeveralBuildings ? (
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Votre bâtiment</Text>
              <BuildingFields
                building={draft.buildings[0]}
                selectedUnitTypes={draft.selectedUnitTypes}
                onNameChange={(name) => patchBuilding(0, { name })}
                onUnitCountChange={(unitTypeName, count) => setUnitCount(0, unitTypeName, count)}
              />
            </View>
          ) : (
            <View style={styles.accordion}>
              {draft.buildings.map((building, index) => {
                const isOpen = openBuildingIndex === index;
                const summary = buildingSummary(building, draft.selectedUnitTypes);
                return (
                  <View key={index} style={styles.accordionItem}>
                    <Pressable
                      accessibilityRole="button"
                      accessibilityState={{ expanded: isOpen }}
                      onPress={() => setOpenBuildingIndex(isOpen ? -1 : index)}
                      style={styles.accordionHeader}
                    >
                      <View style={styles.accordionHeaderText}>
                        <Text style={styles.accordionTitle} numberOfLines={1}>
                          {building.name || `Bâtiment ${index + 1}`}
                        </Text>
                        <Text style={styles.accordionSubtitle} numberOfLines={1}>
                          {summary || 'Aucun lot pour le moment'}
                        </Text>
                      </View>
                      <Text style={styles.chevron}>{isOpen ? '▾' : '▸'}</Text>
                    </Pressable>
                    {isOpen && (
                      <View style={styles.accordionBody}>
                        <BuildingFields
                          building={building}
                          selectedUnitTypes={draft.selectedUnitTypes}
                          onNameChange={(name) => patchBuilding(index, { name })}
                          onUnitCountChange={(unitTypeName, count) => setUnitCount(index, unitTypeName, count)}
                        />
                      </View>
                    )}
                  </View>
                );
              })}
            </View>
          )}

          <Button onPress={() => navigation.navigate('BankAccounts')}>Continuer</Button>
        </View>
      </WizardShell>
    </AuthLayout>
  );
}

interface BuildingFieldsProps {
  building: OnboardingBuilding;
  selectedUnitTypes: string[];
  onNameChange: (name: string) => void;
  onUnitCountChange: (unitTypeName: string, count: number) => void;
}

function BuildingFields({ building, selectedUnitTypes, onNameChange, onUnitCountChange }: BuildingFieldsProps) {
  return (
    <>
      <Input label="Nom du bâtiment (optionnel)" placeholder="Bâtiment principal" value={building.name} onChangeText={onNameChange} />
      <View style={styles.unitCounts}>
        <Text style={styles.sectionTitle}>Nombre de lots</Text>
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
      </View>
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 16,
  },
  section: {
    gap: 16,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.gray[200],
    padding: 16,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[900],
  },
  accordion: {
    gap: 8,
  },
  accordionItem: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.gray[200],
  },
  accordionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
    padding: 16,
  },
  accordionHeaderText: {
    flex: 1,
    gap: 2,
  },
  accordionTitle: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[900],
  },
  accordionSubtitle: {
    fontSize: 14,
    color: colors.gray[500],
  },
  chevron: {
    fontSize: 16,
    color: colors.gray[500],
  },
  accordionBody: {
    gap: 16,
    borderTopWidth: 1,
    borderTopColor: colors.gray[200],
    padding: 16,
  },
  unitCounts: {
    gap: 12,
  },
});
