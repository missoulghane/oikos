import type {
  AccountNature,
  AccountRole,
  EntryDirection,
  ExerciseStatus,
  JournalCode,
  JournalEntryStatus,
  PeriodStatus,
} from '@/features/property-mngt/accounting/types/accounting.types';

export const JOURNAL_CODE_LABELS: Record<JournalCode, string> = {
  VT: 'Ventes / Appels de fonds',
  BQ: 'Banque',
  CA: 'Caisse',
  AC: 'Achats',
  OD: 'Opérations diverses',
  AN: 'À-nouveaux',
};

export const ENTRY_DIRECTION_LABELS: Record<EntryDirection, string> = {
  DEBIT: 'Débit',
  CREDIT: 'Crédit',
};

export const ACCOUNT_NATURE_LABELS: Record<AccountNature, string> = {
  BALANCE_ASSET: 'Actif',
  BALANCE_LIABILITY: 'Passif',
  EXPENSE: 'Charge',
  INCOME: 'Produit',
};

export const ACCOUNT_ROLE_LABELS: Record<AccountRole, string> = {
  UNIT_RECEIVABLE: 'Créance copropriétaire',
  UNIT_ADVANCE: 'Avance copropriétaire',
  DUES_INCOME: 'Cotisations',
  BANK: 'Banque',
  CASH: 'Caisse',
  SUPPLIER: 'Fournisseurs',
  STAFF_PAYABLE: 'Personnel',
};

export const EXERCISE_STATUS_LABELS: Record<ExerciseStatus, string> = {
  OPEN: 'Ouvert',
  CLOSED: 'Clôturé',
};

export const EXERCISE_STATUS_BADGE_COLORS: Record<ExerciseStatus, 'success' | 'light'> = {
  OPEN: 'success',
  CLOSED: 'light',
};

export const PERIOD_STATUS_LABELS: Record<PeriodStatus, string> = {
  OPEN: 'Ouverte',
  CLOSED: 'Fermée',
};

export const JOURNAL_ENTRY_STATUS_LABELS: Record<JournalEntryStatus, string> = {
  DRAFT: 'Brouillon',
  POSTED: 'Validée',
  REVERSED: 'Extournée',
};

export const JOURNAL_ENTRY_STATUS_BADGE_COLORS: Record<JournalEntryStatus, 'light' | 'success' | 'warning'> = {
  DRAFT: 'warning',
  POSTED: 'success',
  REVERSED: 'light',
};

// Balance above which a treasury account (caisse/banque) reads as "healthy" (green) rather than
// merely non-negative (blue) on the accounting overview cards. Tune this single value to change
// the threshold app-wide.
export const TREASURY_BALANCE_HEALTHY_THRESHOLD = 1000;

export function getTreasuryBalanceColorClass(balance: number): string {
  if (balance < 0) {
    return 'text-error-600';
  }
  if (balance > TREASURY_BALANCE_HEALTHY_THRESHOLD) {
    return 'text-success-600';
  }
  return 'text-brand-600';
}
