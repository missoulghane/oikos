import type { MovementType } from '@/features/property-mngt/accounting/types/accounting.types';

export const MOVEMENT_TYPE_LABELS: Record<MovementType, string> = {
  PAYMENT: 'Paiement',
  INITIAL_BALANCE: 'Solde initial',
  REFUND: 'Remboursement',
  CREDIT_NOTE: 'Avoir',
  POSITIVE_ADJUSTMENT: 'Régularisation positive',
  INSTALLMENT: 'Appel de cotisation',
  PENALTY: 'Pénalité',
  REMINDER_FEE: 'Frais de relance',
  NEGATIVE_ADJUSTMENT: 'Régularisation négative',
};
