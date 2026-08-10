import type { Paged } from '@/shared/types/pagination.types';

export type AccountNature = 'BALANCE_ASSET' | 'BALANCE_LIABILITY' | 'EXPENSE' | 'INCOME';
export type EntryDirection = 'DEBIT' | 'CREDIT';
export type AccountRole =
  | 'UNIT_RECEIVABLE'
  | 'UNIT_ADVANCE'
  | 'DUES_INCOME'
  | 'BANK'
  | 'CASH'
  | 'SUPPLIER'
  | 'STAFF_PAYABLE';
export type JournalCode = 'VT' | 'BQ' | 'CA' | 'AC' | 'OD' | 'AN';
export type ExerciseStatus = 'OPEN' | 'CLOSED';
export type PeriodStatus = 'OPEN' | 'CLOSED';
export type JournalEntryStatus = 'DRAFT' | 'POSTED' | 'REVERSED';

export interface LedgerAccount {
  id: string;
  propertyId: string | null;
  unitId: string | null;
  accountNumber: string;
  label: string;
  accountClass: number;
  nature: AccountNature;
  normalSide: EntryDirection;
  collective: boolean;
  role: AccountRole | null;
  active: boolean;
  balance: number;
}

export interface AccountingExercise {
  id: string;
  propertyId: string;
  label: string;
  startDate: string;
  endDate: string;
  status: ExerciseStatus;
  closedAt: string | null;
  closedByUserId: string | null;
  comment: string | null;
}

export interface Period {
  id: string;
  exerciseId: string;
  yearMonth: string;
  status: PeriodStatus;
  closedAt: string | null;
  closedByUserId: string | null;
}

export interface JournalEntryLine {
  id: string;
  ledgerAccountId: string;
  auxiliaryUnitId: string | null;
  auxiliaryPartyId: string | null;
  direction: EntryDirection;
  amount: number;
  label: string;
}

export interface JournalEntry {
  id: string;
  propertyId: string;
  exerciseId: string;
  periodId: string;
  journalCode: JournalCode;
  treasuryAccountId: string | null;
  pieceDate: string;
  pieceNumber: number | null;
  externalReference: string | null;
  status: JournalEntryStatus;
  originalEntryId: string | null;
  createdByUserId: string;
  lines: JournalEntryLine[];
}

export type PagedJournalEntries = Paged<JournalEntry>;

export interface Expense {
  id: string;
  propertyId: string;
  date: string;
  ledgerAccountId: string;
  amount: number;
  description: string | null;
  receiptReference: string | null;
  journalEntryId: string;
}

export interface JournalEntryReference {
  journalEntryId: string;
}

export interface OpenExercisePayload {
  label: string;
  startDate: string;
  endDate: string;
  comment?: string;
}

export interface RecordSupplierPaymentPayload {
  ledgerAccountId: string;
  treasuryAccountId: string;
  pieceDate: string;
  amount: number;
  description?: string;
  externalReference?: string;
}

export interface RecordPayrollExpensePayload {
  date: string;
  ledgerAccountId: string;
  amount: number;
  description?: string;
}

export interface RecordBankChargePayload {
  pieceDate: string;
  ledgerAccountId: string;
  bankAccountId: string;
  amount: number;
  description?: string;
}

export interface AddBankAccountPayload {
  label: string;
}

export interface JournalEntryListFilters {
  pieceDateFrom?: string;
  pieceDateTo?: string;
  search?: string;
  status?: JournalEntryStatus;
}

export interface RecordTreasuryTransferPayload {
  sourceAccountId: string;
  destinationAccountId: string;
  pieceDate: string;
  amount: number;
  description?: string;
}
