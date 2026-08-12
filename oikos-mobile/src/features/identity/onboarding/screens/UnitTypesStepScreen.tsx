import { StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { Alert } from '@/shared/components/Alert/Alert';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { SelectableCard } from '@/features/identity/onboarding/components/SelectableCard';
import { UNIT_TYPE_CHOICES } from '@/features/identity/onboarding/state/onboardingDraft';
import { colors } from '@/shared/theme/colors';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'UnitTypes'>;

const UNIT_TYPE_DESCRIPTIONS: Record<string, string> = {
  Appartement: 'Logements de la copropriété.',
  Box: 'Garages et emplacements de stationnement.',
  Bureau: 'Locaux professionnels.',
};

export function UnitTypesStepScreen({ navigation }: Props) {
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
    <AuthLayout scrollable>
      <WizardShell
        step="UnitTypes"
        title="Quels types de lots gérez-vous ?"
        subtitle="Sélectionnez les types de lots présents dans votre copropriété."
        onBack={() => navigation.goBack()}
      >
        <View style={styles.container}>
          {UNIT_TYPE_CHOICES.map((choice) => (
            <SelectableCard
              key={choice.name}
              type="checkbox"
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
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Montant par type de lot</Text>
              <Text style={styles.sectionSubtitle}>Indiquez le montant associé à chaque type de lot.</Text>
              {draft.selectedUnitTypes.map((name) => (
                <Input
                  key={name}
                  label={name}
                  keyboardType="decimal-pad"
                  placeholder="0"
                  value={draft.unitTypePrices[name] ?? ''}
                  onChangeText={(text) => setPrice(name, text)}
                />
              ))}
            </View>
          )}

          {!isFlatRate && (
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Budget prévisionnel</Text>
              <Text style={styles.sectionSubtitle}>
                En tantièmes, les charges se répartissent à partir de ce budget. Vous pourrez le renseigner plus
                tard, ainsi que les tantièmes de chaque lot.
              </Text>
              <Input
                label="Budget prévisionnel (optionnel)"
                keyboardType="decimal-pad"
                placeholder="0"
                value={draft.projectedBudget}
                onChangeText={(text) => update({ projectedBudget: text })}
              />
            </View>
          )}

          <Button disabled={draft.selectedUnitTypes.length === 0} onPress={() => navigation.navigate('Buildings')}>
            Continuer
          </Button>
        </View>
      </WizardShell>
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 12,
  },
  section: {
    marginTop: 8,
    gap: 12,
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
  sectionSubtitle: {
    fontSize: 14,
    color: colors.gray[500],
    marginTop: -8,
  },
});
