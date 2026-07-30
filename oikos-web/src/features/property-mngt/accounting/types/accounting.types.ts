import type { Paged } from '@/shared/types/pagination.types';

export type FinancialAccountType = 'CASH' | 'BANK' | 'MOBILE_MONEY';
export type FinancialAccountStatus = 'ACTIVE' | 'CLOSED';
export type ExerciseStatus = 'OPEN' | 'CLOSED';

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

export interface TreasurySummary {
  cashBalance: number;
  bankBalance: number;
  totalBalance: number;
}

export interface FinancialAccount {
  id: string;
  name: string;
  type: FinancialAccountType;
  currency: string;
  balance: number;
  status: FinancialAccountStatus;
}

export interface Expense {
  id: string;
  financialAccountId: string;
  date: string;
  category: string;
  provider: string;
  amount: number;
  description: string | null;
  receiptReference: string | null;
}

export type PagedExpenses = Paged<Expense>;

export interface OpenExercisePayload {
  label: string;
  startDate: string;
  endDate: string;
  comment?: string;
}

export interface CreateFinancialAccountPayload {
  name: string;
  type: FinancialAccountType;
  currency: string;
}

export interface TransferBetweenFinancialAccountsPayload {
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  date: string;
  label: string;
}

export interface RecordExceptionalDepositPayload {
  financialAccountId: string;
  amount: number;
  date: string;
  label: string;
}

export interface RecordExpensePayload {
  financialAccountId: string;
  date: string;
  category: string;
  provider: string;
  amount: number;
  description?: string;
  receiptReference?: string;
}

export type UnitAccountMovementType = 'FUND_CALL' | 'PAYMENT' | 'REGULARIZATION';
export type UnitAccountMovementDirection = 'DEBIT' | 'CREDIT';

export interface UnitAccount {
  id: string;
  unitId: string;
  balance: number;
  lastUpdatedDate: string;
}

export interface UnitAccountMovement {
  id: string;
  date: string;
  type: UnitAccountMovementType;
  direction: UnitAccountMovementDirection;
  amount: number;
  label: string;
  businessReference: string | null;
  reason: string | null;
}

export type PagedUnitAccountMovements = Paged<UnitAccountMovement>;

export interface UnitAccountSummary {
  unitId: string;
  totalDue: number;
  totalPaid: number;
  availableAdvance: number;
  currentBalance: number;
}

export interface RecordOwnerPaymentPayload {
  financialAccountId: string;
  amount: number;
  date: string;
  label: string;
}

export interface RecordUnitAccountRegularizationPayload {
  amount: number;
  direction: UnitAccountMovementDirection;
  label: string;
  reason: string;
}

export type FinancialEntryType =
  | 'OWNER_PAYMENT'
  | 'EXCEPTIONAL_DEPOSIT'
  | 'REFUND'
  | 'OTHER_INCOME'
  | 'PROVIDER_PAYMENT'
  | 'PURCHASE'
  | 'BANK_FEE'
  | 'OTHER_EXPENSE'
  | 'TRANSFER_OUT'
  | 'TRANSFER_IN';

export type FinancialEntryDirection = 'IN' | 'OUT';

export interface FinancialJournalEntry {
  id: string;
  financialAccountId: string;
  date: string;
  type: FinancialEntryType;
  direction: FinancialEntryDirection;
  amount: number;
  label: string;
  businessReference: string | null;
}

export type PagedFinancialJournalEntries = Paged<FinancialJournalEntry>;

export interface JournalFilters {
  financialAccountId?: string;
  type?: FinancialEntryType;
  dateFrom?: string;
  dateTo?: string;
}

export interface LettrageMovement {
  id: string;
  date: string;
  type: UnitAccountMovementType;
  direction: UnitAccountMovementDirection;
  amount: number;
  label: string;
  remainingAmount: number;
}

export interface LettrageProposalLine {
  debitMovementId: string;
  creditMovementId: string;
  amount: number;
}

export interface LettrageProposal {
  unitId: string;
  unsettledDebits: LettrageMovement[];
  unallocatedCredits: LettrageMovement[];
  proposedLines: LettrageProposalLine[];
  totalProposedAmount: number;
  totalUnmatchedDebit: number;
  totalUnmatchedCredit: number;
}

export interface PendingLettrage {
  unitId: string;
  proposedAmount: number;
  remainingUnmatchedDebitAfter: number;
  remainingUnallocatedCreditAfter: number;
}

export interface ValidateBulkLettragePayload {
  unitIds?: string[];
}
