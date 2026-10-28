import { StyleSheet, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { SelectableCard } from '@/features/identity/onboarding/components/SelectableCard';
import { UNIT_TYPE_CHOICES } from '@/features/identity/onboarding/state/onboardingDraft';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'UnitTypes'>;

const UNIT_TYPE_DESCRIPTIONS: Record<string, string> = {
  Appartement: 'Logements de la copropriété.',
  Box: 'Garages et emplacements de stationnement.',
  Bureau: 'Locaux professionnels.',
};

/**
 * Le choix des types seul : leurs montants viennent deux étapes plus loin, une
 * fois le mode de gestion connu - en tantièmes, il n'y en a pas.
 */
export function UnitTypesStepScreen({ navigation }: Props) {
  const { draft, update } = useOnboarding();

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

          <Button disabled={draft.selectedUnitTypes.length === 0} onPress={() => navigation.navigate('DuesMode')}>
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
});
