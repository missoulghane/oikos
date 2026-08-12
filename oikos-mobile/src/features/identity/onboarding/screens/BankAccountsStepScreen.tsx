import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { colors } from '@/shared/theme/colors';
import type { OnboardingBankAccount } from '@/features/identity/onboarding/state/onboardingDraft';
import type { OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

type Props = NativeStackScreenProps<OnboardingStackParamList, 'BankAccounts'>;

export function BankAccountsStepScreen({ navigation }: Props) {
  const { draft, update } = useOnboarding();

  function patchAccount(index: number, patch: Partial<OnboardingBankAccount>) {
    update({
      bankAccounts: draft.bankAccounts.map((account, position) =>
        position === index ? { ...account, ...patch } : account,
      ),
    });
  }

  function addAccount() {
    update({ bankAccounts: [...draft.bankAccounts, { label: '', bankAccountNumber: '' }] });
  }

  function removeAccount(index: number) {
    update({ bankAccounts: draft.bankAccounts.filter((_, position) => position !== index) });
  }

  // Un compte sans intitulé ne peut pas être créé côté API (label obligatoire) :
  // on l'ignore plutôt que de bloquer une étape explicitement facultative.
  const usableAccounts = draft.bankAccounts.filter((account) => account.label.trim() !== '');

  function goNext() {
    update({ bankAccounts: usableAccounts });
    navigation.navigate('Summary');
  }

  return (
    <AuthLayout scrollable>
      <WizardShell
        step="BankAccounts"
        title="Vos comptes bancaires"
        subtitle="Déclarez le ou les comptes de votre copropriété. Vous pourrez le faire plus tard si vous préférez."
        onBack={() => navigation.goBack()}
      >
        <View style={styles.container}>
          {draft.bankAccounts.length === 0 ? (
            <EmptyState title="Aucun compte bancaire">
              Ajoutez un compte maintenant, ou passez cette étape et faites-le depuis la comptabilité.
            </EmptyState>
          ) : (
            draft.bankAccounts.map((account, index) => (
              <View key={index} style={styles.accountCard}>
                <Input
                  label="Nom de la banque"
                  placeholder="Attijariwafa Bank"
                  value={account.label}
                  onChangeText={(text) => patchAccount(index, { label: text })}
                />
                <Input
                  label="Numéro de compte (optionnel)"
                  placeholder="RIB ou IBAN"
                  value={account.bankAccountNumber}
                  onChangeText={(text) => patchAccount(index, { bankAccountNumber: text })}
                />
                <Pressable onPress={() => removeAccount(index)}>
                  <Text style={styles.removeLink}>Retirer ce compte</Text>
                </Pressable>
              </View>
            ))
          )}

          <Button variant="secondary" onPress={addAccount} style={styles.addButton}>
            Ajouter un compte bancaire
          </Button>

          <View style={styles.actions}>
            <Button onPress={goNext}>Continuer</Button>
            <Pressable
              onPress={() => {
                update({ bankAccounts: [] });
                navigation.navigate('Summary');
              }}
            >
              <Text style={styles.skipLink}>Passer cette étape</Text>
            </Pressable>
          </View>
        </View>
      </WizardShell>
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 16,
  },
  accountCard: {
    gap: 12,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.gray[200],
    padding: 16,
  },
  removeLink: {
    alignSelf: 'flex-start',
    fontSize: 14,
    fontWeight: '500',
    color: colors.error[500],
  },
  addButton: {
    alignSelf: 'flex-start',
  },
  actions: {
    gap: 8,
  },
  skipLink: {
    textAlign: 'center',
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[600],
  },
});
