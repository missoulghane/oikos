import type { PropsWithChildren, ReactNode } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { useConfigureProperty } from '@/features/identity/onboarding/hooks/useConfigureProperty';
import { colors } from '@/shared/theme/colors';
import {
  buildingSummary,
  formatAddress,
  totalUnitCount,
  unitCountOf,
} from '@/features/identity/onboarding/state/onboardingDraft';
import type { ConfigurePropertyPayload } from '@/features/identity/onboarding/types/onboarding.types';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'Summary'>;

export function SummaryStepScreen({ navigation }: Props) {
  const { draft } = useOnboarding();
  const configure = useConfigureProperty();
  const isFlatRate = draft.duesCalculationMode === 'FLAT_RATE';

  function toPayload(): ConfigurePropertyPayload {
    const projectedBudget = Number.parseFloat(draft.projectedBudget);
    return {
      duesCalculationMode: draft.duesCalculationMode,
      projectedBudget: !isFlatRate && Number.isFinite(projectedBudget) ? projectedBudget : undefined,
      unitTypes: draft.selectedUnitTypes.map((name) => {
        const price = Number.parseFloat(draft.unitTypePrices[name] ?? '');
        return { name, price: isFlatRate && Number.isFinite(price) ? price : undefined };
      }),
      buildings: draft.buildings.map((building) => ({
        name: building.name.trim() || undefined,
        floorCount: building.floorCount,
        unitTypes: draft.selectedUnitTypes.map((unitTypeName) => ({
          unitTypeName,
          count: unitCountOf(building, unitTypeName),
        })),
      })),
      bankAccounts: draft.bankAccounts
        .filter((account) => account.label.trim() !== '')
        .map((account) => ({
          label: account.label.trim(),
          bankAccountNumber: account.bankAccountNumber.trim() || undefined,
        })),
    };
  }

  function finalize() {
    if (!draft.registration) {
      return;
    }
    configure.mutate(
      {
        propertyId: draft.registration.propertyId,
        payload: toPayload(),
        onboardingToken: draft.registration.onboardingToken,
      },
      { onSuccess: () => navigation.navigate('Done') },
    );
  }

  if (!draft.registration) {
    return (
      <AuthLayout scrollable>
        <WizardShell step="Summary" title="Vérifiez votre configuration" subtitle="Votre compte n'a pas encore été créé.">
          <Alert message="Reprenez à l'étape « Votre copropriété » pour créer votre compte avant de finaliser." />
          <Pressable onPress={() => navigation.navigate('Property')}>
            <Text style={styles.editLink}>Revenir à cette étape</Text>
          </Pressable>
        </WizardShell>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout scrollable>
      <WizardShell
        step="Summary"
        title="Vérifiez votre configuration"
        subtitle="Tout est prêt. Vérifiez les informations avant de finaliser votre copropriété."
        onBack={() => navigation.goBack()}
      >
        <View style={styles.container}>
          {configure.isError && <Alert message={getErrorMessage(configure.error)} />}

          <RecapSection title="Votre copropriété" onEdit={() => navigation.navigate('Property')}>
            <RecapLine label="Nom" value={draft.property.name} />
            <RecapLine label="Adresse" value={formatAddress(draft.property)} />
          </RecapSection>

          <RecapSection title="Mode de gestion" onEdit={() => navigation.navigate('DuesMode')}>
            <Text style={styles.recapText}>{isFlatRate ? 'Forfait' : 'Tantièmes'}</Text>
            {!isFlatRate && draft.projectedBudget && <RecapLine label="Budget prévisionnel" value={draft.projectedBudget} />}
          </RecapSection>

          <RecapSection title="Types de lots" onEdit={() => navigation.navigate('UnitTypes')}>
            {draft.selectedUnitTypes.map((name) => (
              <Text key={name} style={styles.recapText}>
                {name}
                {isFlatRate && draft.unitTypePrices[name] ? ` — ${draft.unitTypePrices[name]} MAD` : ''}
              </Text>
            ))}
          </RecapSection>

          <RecapSection title="Bâtiments" onEdit={() => navigation.navigate('Buildings')}>
            <Text style={styles.recapMuted}>
              {draft.buildings.length} bâtiment{draft.buildings.length > 1 ? 's' : ''} · {totalUnitCount(draft)} lot
              {totalUnitCount(draft) > 1 ? 's' : ''}
            </Text>
            {draft.buildings.map((building, index) => (
              <View key={index} style={styles.buildingRecap}>
                <Text style={styles.recapText}>{building.name || `Bâtiment ${index + 1}`}</Text>
                <Text style={styles.recapMuted}>{buildingSummary(building, draft.selectedUnitTypes) || 'Aucun lot'}</Text>
              </View>
            ))}
          </RecapSection>

          <RecapSection title="Comptes bancaires" onEdit={() => navigation.navigate('BankAccounts')}>
            {draft.bankAccounts.length === 0 ? (
              <Text style={styles.recapMuted}>Aucun compte déclaré pour le moment.</Text>
            ) : (
              draft.bankAccounts.map((account, index) => (
                <Text key={index} style={styles.recapText}>
                  {account.label}
                  {account.bankAccountNumber ? ` — ${account.bankAccountNumber}` : ''}
                </Text>
              ))
            )}
          </RecapSection>

          <Button isLoading={configure.isPending} onPress={finalize}>
            Finaliser ma copropriété
          </Button>
          {/* La copropriété existe depuis l'étape 2 : ce bouton la structure, il ne
              la crée pas - d'où « Finaliser » plutôt que « Créer ». */}
          <Text style={styles.footnote}>
            Votre compte est déjà créé : vous pourrez reprendre cette configuration plus tard si besoin.
          </Text>
        </View>
      </WizardShell>
    </AuthLayout>
  );
}

function RecapSection({ title, onEdit, children }: PropsWithChildren<{ title: string; onEdit: () => void }>) {
  return (
    <View style={styles.section}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>{title}</Text>
        <Pressable onPress={onEdit}>
          <Text style={styles.editLink}>Modifier</Text>
        </Pressable>
      </View>
      {children}
    </View>
  );
}

function RecapLine({ label, value }: { label: string; value: ReactNode }) {
  return (
    <Text style={styles.recapText}>
      <Text style={styles.recapMuted}>{label} : </Text>
      {value}
    </Text>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 16,
  },
  section: {
    gap: 4,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.gray[200],
    padding: 16,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 4,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.gray[900],
  },
  editLink: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.brand[500],
  },
  recapText: {
    fontSize: 14,
    color: colors.gray[900],
  },
  recapMuted: {
    fontSize: 14,
    color: colors.gray[500],
  },
  buildingRecap: {
    marginBottom: 4,
  },
  footnote: {
    textAlign: 'center',
    fontSize: 14,
    color: colors.gray[500],
  },
});
