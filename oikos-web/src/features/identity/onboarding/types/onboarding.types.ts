import type { DuesCalculationMode } from '@/features/property-mngt/properties/types/property.types';
import type { BoardSeat } from '@/features/identity/onboarding/constants/boardSeats';

/** Réponse de POST /users/register-property-board-admin (fin de l'étape 1). */
export interface RegisteredBoardAdmin {
  userId: string;
  onboardingToken: string;
  expiresInSeconds: number;
}

export interface CreateBoardAdminPropertyPayload {
  propertyName: string;
  propertyAddress: string;
  propertyCity: string;
  boardRole: BoardSeat;
}

/** Réponse de POST /users/me/board-admin-property (fin de l'étape 2). */
export interface CreatedBoardAdminProperty {
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
  floorCount?: number;
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
