import type {
  UnitAccountMovementDirection,
  UnitAccountMovementType,
} from '@/features/property-mngt/accounting/types/accounting.types';

export const UNIT_ACCOUNT_MOVEMENT_TYPE_LABELS: Record<UnitAccountMovementType, string> = {
  FUND_CALL: 'Appel de fonds',
  PAYMENT: 'Paiement',
  REGULARIZATION: 'Régularisation',
};

export const UNIT_ACCOUNT_MOVEMENT_DIRECTION_LABELS: Record<UnitAccountMovementDirection, string> = {
  DEBIT: 'Débit',
  CREDIT: 'Crédit',
};

export const UNIT_ACCOUNT_MOVEMENT_DIRECTION_BADGE_COLORS: Record<UnitAccountMovementDirection, 'error' | 'success'> = {
  DEBIT: 'error',
  CREDIT: 'success',
};
