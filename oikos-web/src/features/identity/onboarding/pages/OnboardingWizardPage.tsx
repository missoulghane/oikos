import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { OnboardingProvider } from '@/features/identity/onboarding/state/OnboardingContext';
import { AccountStepPage } from '@/features/identity/onboarding/pages/AccountStepPage';
import { PropertyStepPage } from '@/features/identity/onboarding/pages/PropertyStepPage';
import { DuesModeStepPage } from '@/features/identity/onboarding/pages/DuesModeStepPage';
import { UnitTypesStepPage } from '@/features/identity/onboarding/pages/UnitTypesStepPage';
import { BuildingsStepPage } from '@/features/identity/onboarding/pages/BuildingsStepPage';
import { BankAccountsStepPage } from '@/features/identity/onboarding/pages/BankAccountsStepPage';
import { SummaryStepPage } from '@/features/identity/onboarding/pages/SummaryStepPage';
import { OnboardingDonePage } from '@/features/identity/onboarding/pages/OnboardingDonePage';

/**
 * Wizard d'inscription du syndic bénévole. Le provider enveloppe toutes les
 * étapes : le brouillon doit survivre à la navigation d'une étape à l'autre,
 * qui est un vrai changement de route (URL par étape, donc lien profond,
 * bouton « Modifier » du récapitulatif et retour navigateur gratuits).
 */
export function OnboardingWizardPage() {
  return (
    <OnboardingProvider>
      <AuthLayout wide>
        <Routes>
          <Route index element={<Navigate to="account" replace />} />
          <Route path="account" element={<AccountStepPage />} />
          <Route path="property" element={<PropertyStepPage />} />
          <Route path="dues-mode" element={<DuesModeStepPage />} />
          <Route path="unit-types" element={<UnitTypesStepPage />} />
          <Route path="buildings" element={<BuildingsStepPage />} />
          <Route path="bank-accounts" element={<BankAccountsStepPage />} />
          <Route path="summary" element={<SummaryStepPage />} />
          <Route path="done" element={<OnboardingDonePage />} />
          <Route path="*" element={<Navigate to="account" replace />} />
        </Routes>
      </AuthLayout>
    </OnboardingProvider>
  );
}
