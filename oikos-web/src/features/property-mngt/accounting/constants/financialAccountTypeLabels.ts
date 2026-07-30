import type { FinancialAccountType } from '@/features/property-mngt/accounting/types/accounting.types';

export const FINANCIAL_ACCOUNT_TYPE_LABELS: Record<FinancialAccountType, string> = {
  CASH: 'Caisse',
  BANK: 'Banque',
  MOBILE_MONEY: 'Mobile money',
};
