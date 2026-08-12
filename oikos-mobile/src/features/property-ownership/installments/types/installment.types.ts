// Mirrors the subset of property-mngt/installments/types/installment.types.ts
// (oikos-web) needed for owner-facing read-only screens - property-mngt
// doesn't exist on mobile (out of scope, see PLAN.md).
export type InstallmentStatus = 'NOT_SETTLED' | 'PARTIALLY_SETTLED' | 'SETTLED';

export interface Installment {
  id: string;
  unitId: string;
  dueDate: string;
  amount: number;
  outstandingAmount: number;
  status: InstallmentStatus;
  /** "YYYY-MM" of the installment call this echeance was raised from - null when raised manually (no call). */
  period: string | null;
}
