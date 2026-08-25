export type PaymentMode = 'BANK_TRANSFER' | 'CASH' | 'CHECK' | 'DIRECT_DEBIT';

export interface Payment {
  id: string;
  propertyId: string;
  unitId: string;
  mode: PaymentMode;
  valueDate: string;
  amount: number;
  journalEntryId: string;
}

export interface InstallmentAllocation {
  installmentId: string;
  amount: number;
}

export interface RecordOwnerPaymentResult {
  payment: Payment;
  allocations: InstallmentAllocation[];
  advanceAmount: number;
}

export interface RecordOwnerPaymentPayload {
  mode: PaymentMode;
  treasuryAccountId: string;
  valueDate: string;
  amount: number;
}
