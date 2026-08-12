// Mirrors the subset of property-mngt/installments/types/payment.types.ts
// (oikos-web) needed for owner-facing read-only screens - property-mngt
// doesn't exist on mobile (out of scope, see PLAN.md).
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
