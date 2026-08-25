import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { useOnboarding } from '@/features/identity/onboarding/state/OnboardingContext';
import { WizardShell } from '@/features/identity/onboarding/components/WizardShell';
import { nextStepPath, previousStepPath } from '@/features/identity/onboarding/constants/steps';
import type { OnboardingBankAccount } from '@/features/identity/onboarding/state/onboardingDraft';

export function BankAccountsStepPage() {
  const navigate = useNavigate();
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
    navigate(nextStepPath('bank-accounts')!);
  }

  return (
    <WizardShell
      step="bank-accounts"
      title="Vos comptes bancaires"
      subtitle="Déclarez le ou les comptes de votre copropriété. Vous pourrez le faire plus tard si vous préférez."
      backPath={previousStepPath('bank-accounts')}
    >
      <div className="flex flex-col gap-4">
        {draft.bankAccounts.length === 0 ? (
          <EmptyState title="Aucun compte bancaire">
            Ajoutez un compte maintenant, ou passez cette étape et faites-le depuis la comptabilité.
          </EmptyState>
        ) : (
          draft.bankAccounts.map((account, index) => (
            <section key={index} className="flex flex-col gap-3 rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
              <Input
                label="Nom de la banque"
                name={`bank-label-${index}`}
                placeholder="Attijariwafa Bank"
                value={account.label}
                onChange={(event) => patchAccount(index, { label: event.target.value })}
              />
              <Input
                label="Numéro de compte"
                name={`bank-number-${index}`}
                placeholder="RIB ou IBAN"
                value={account.bankAccountNumber}
                onChange={(event) => patchAccount(index, { bankAccountNumber: event.target.value })}
              />
              <button
                type="button"
                onClick={() => removeAccount(index)}
                className="self-start text-sm font-medium text-error-500 underline dark:text-error-400"
              >
                Retirer ce compte
              </button>
            </section>
          ))
        )}

        <Button type="button" variant="secondary" onClick={addAccount} className="self-start">
          Ajouter un compte bancaire
        </Button>

        <div className="flex flex-col gap-2">
          <Button type="button" onClick={goNext}>
            Continuer
          </Button>
          <button
            type="button"
            onClick={() => {
              update({ bankAccounts: [] });
              navigate(nextStepPath('bank-accounts')!);
            }}
            className="text-sm font-medium text-gray-600 underline dark:text-gray-400"
          >
            Passer cette étape
          </button>
        </div>
      </div>
    </WizardShell>
  );
}
