import { StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Button } from '@/shared/components/Button/Button';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { SelectableCard } from '@/features/identity/onboarding/components/SelectableCard';
import { colors } from '@/shared/theme/colors';
import type { DuesCalculationMode } from '@/features/identity/onboarding/state/onboardingDraft';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'DuesMode'>;

export function DuesModeStepScreen({ navigation }: Props) {
  const { draft, update } = useOnboarding();

  return (
    <AuthLayout scrollable>
      <WizardShell
        step="DuesMode"
        title="Comment souhaitez-vous gérer vos charges ?"
        subtitle="Choisissez le mode de gestion utilisé par votre copropriété."
        onBack={() => navigation.goBack()}
      >
        <View style={styles.container}>
          <SelectableCard
            value="FLAT_RATE"
            checked={draft.duesCalculationMode === 'FLAT_RATE'}
            onSelect={(value) => update({ duesCalculationMode: value as DuesCalculationMode })}
            title="Forfait"
            description="Un montant fixe est associé à chaque type de lot."
          />
          <SelectableCard
            value="SHARES"
            checked={draft.duesCalculationMode === 'SHARES'}
            onSelect={(value) => update({ duesCalculationMode: value as DuesCalculationMode })}
            title="Tantièmes"
            description="Les charges sont réparties selon les tantièmes de chaque lot."
          />
          <Text style={styles.hint}>Vous pourrez modifier ce choix plus tard dans les paramètres de votre copropriété.</Text>
          <Button onPress={() => navigation.navigate('UnitTypes')}>Continuer</Button>
        </View>
      </WizardShell>
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 12,
  },
  hint: {
    fontSize: 14,
    color: colors.gray[500],
  },
});
