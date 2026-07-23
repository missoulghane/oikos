import type { Paged } from '@/shared/types/pagination.types';

export type AccountType = 'UNIT' | 'PROPERTY';

export interface Account {
  id: string;
  holderId: string;
  accountType: AccountType;
  balance: number;
}

export type MovementType =
  | 'PAYMENT'
  | 'INITIAL_BALANCE'
  | 'REFUND'
  | 'CREDIT_NOTE'
  | 'POSITIVE_ADJUSTMENT'
  | 'INSTALLMENT'
  | 'PENALTY'
  | 'REMINDER_FEE'
  | 'NEGATIVE_ADJUSTMENT';

export type MovementDirection = 'DEBIT' | 'CREDIT';

export interface Movement {
  id: string;
  accountId: string;
  occurredOn: string;
  type: MovementType;
  direction: MovementDirection;
  amount: number;
  label: string;
  businessReference: string | null;
}

export type PagedMovements = Paged<Movement>;
