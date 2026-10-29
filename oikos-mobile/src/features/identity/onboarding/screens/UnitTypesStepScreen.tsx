import { StyleSheet, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { SelectableCard } from '@/features/identity/onboarding/components/SelectableCard';
import { CatalogLoadState } from '@/shared/components/CatalogLoadState/CatalogLoadState';
import { useUnitTypeChoices } from '@/shared/hooks/useUnitTypeChoices';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'UnitTypes'>;

/**
 * Le choix des types seul : leurs montants viennent deux étapes plus loin, une
 * fois le mode de gestion connu - en tantièmes, il n'y en a pas.
 */
export function UnitTypesStepScreen({ navigation }: Props) {
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
    <AuthLayout scrollable>
      <WizardShell
        step="UnitTypes"
        title="Quels types de lots gérez-vous ?"
        subtitle="Sélectionnez les types de lots présents dans votre copropriété."
        onBack={() => navigation.goBack()}
      >
        <View style={styles.container}>
          {!unitTypeChoices.data && (
            <CatalogLoadState
              query={unitTypeChoices}
              loadingLabel="Chargement des types de lots…"
              errorMessage="Impossible de charger les types de lots. Vérifiez votre connexion, puis réessayez."
            />
          )}
          {choices.map((choice) => (
            <SelectableCard
              key={choice.name}
              type="checkbox"
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
