import type {
  FinancialEntryDirection,
  FinancialEntryType,
} from '@/features/property-mngt/accounting/types/accounting.types';

export const FINANCIAL_ENTRY_TYPE_LABELS: Record<FinancialEntryType, string> = {
  OWNER_PAYMENT: 'Paiement propriétaire',
  EXCEPTIONAL_DEPOSIT: 'Dépôt exceptionnel',
  REFUND: 'Remboursement',
  OTHER_INCOME: 'Autre recette',
  PROVIDER_PAYMENT: 'Paiement fournisseur',
  PURCHASE: 'Achat',
  BANK_FEE: 'Frais bancaires',
  OTHER_EXPENSE: 'Autre dépense',
  TRANSFER_OUT: 'Virement sortant',
  TRANSFER_IN: 'Virement entrant',
};

export const FINANCIAL_ENTRY_DIRECTION_LABELS: Record<FinancialEntryDirection, string> = {
  IN: 'Entrée',
  OUT: 'Sortie',
};

export const FINANCIAL_ENTRY_DIRECTION_BADGE_COLORS: Record<FinancialEntryDirection, 'success' | 'error'> = {
  IN: 'success',
  OUT: 'error',
};
