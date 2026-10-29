import { useState } from 'react';
import { StyleSheet, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'Pricing'>;

/**
 * Ce que coûte un lot, selon le mode choisi juste avant : un montant par type
 * sélectionné en forfait, le budget prévisionnel en tantièmes - jamais les deux,
 * puisque chaque mode n'en lit qu'un. Même découpage que oikos-web.
 */
export function PricingStepScreen({ navigation }: Props) {
  const { draft, update } = useOnboarding();
  const isFlatRate = draft.duesCalculationMode === 'FLAT_RATE';
  // N'affiche les montants manquants qu'après une tentative de « Continuer » -
  // pas dès l'arrivée sur l'étape, où aucun champ n'a encore pu être rempli.
  const [attemptedContinue, setAttemptedContinue] = useState(false);

  function isPriceMissing(name: string) {
    return (draft.unitTypePrices[name] ?? '') === '';
  }

  function handleContinue() {
    if (isFlatRate && draft.selectedUnitTypes.some(isPriceMissing)) {
      setAttemptedContinue(true);
      return;
    }
    navigation.navigate('Buildings');
  }

  return (
    <AuthLayout scrollable>
      <WizardShell
        step="Pricing"
        stepLabel={isFlatRate ? undefined : 'Budget prévisionnel'}
        title={isFlatRate ? 'Quel montant pour chaque type de lot ?' : 'Quel est votre budget prévisionnel ?'}
        subtitle={
          isFlatRate
            ? 'Indiquez le montant à payer pour chaque type de lot.'
            : 'En tantièmes, les charges se répartissent à partir de ce budget. Vous pourrez le renseigner plus tard, ainsi que les tantièmes de chaque lot.'
        }
        onBack={() => navigation.goBack()}
      >
        <View style={styles.container}>
          {isFlatRate ? (
            draft.selectedUnitTypes.map((name) => (
              <Input
                key={name}
                label={`Montant ${name} (MAD)`}
                accessibilityLabel={`Montant ${name}`}
                keyboardType="decimal-pad"
                placeholder="0"
                value={draft.unitTypePrices[name] ?? ''}
                onChangeText={(text) => update({ unitTypePrices: { ...draft.unitTypePrices, [name]: text } })}
                errorMessage={attemptedContinue && isPriceMissing(name) ? 'Le montant est requis' : undefined}
              />
            ))
          ) : (
            <Input
              label="Budget prévisionnel annuel (optionnel)"
              keyboardType="decimal-pad"
              placeholder="0"
              value={draft.projectedBudget}
              onChangeText={(text) => update({ projectedBudget: text })}
            />
          )}
          <Button onPress={handleContinue}>Continuer</Button>
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
