import type { DuesCalculationMode } from '@/features/identity/onboarding/state/onboardingDraft';

export interface CaptureOnboardingLeadPayload {
  email: string;
  firstName?: string;
  lastName?: string;
}

/** Réponse de POST /users/register-property-board-admin (fin de l'étape 2). */
export interface RegisteredBoardAdmin {
  userId: string;
  propertyId: string;
  onboardingToken: string;
  expiresInSeconds: number;
}

export interface ConfiguredUnitType {
  name: string;
  price?: number;
}

export interface ConfiguredBuilding {
  name?: string;
  unitTypes: { unitTypeName: string; count: number }[];
}

export interface ConfiguredBankAccount {
  label: string;
  bankAccountNumber?: string;
}

export interface ConfigurePropertyPayload {
  duesCalculationMode: DuesCalculationMode;
  projectedBudget?: number;
  unitTypes: ConfiguredUnitType[];
  buildings: ConfiguredBuilding[];
  bankAccounts: ConfiguredBankAccount[];
}
