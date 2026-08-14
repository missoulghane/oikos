// Mirrors the subset of property-mngt/installments/types/installment.types.ts
// (oikos-web) needed for owner-facing read-only screens.
export type InstallmentStatus = 'NOT_SETTLED' | 'PARTIALLY_SETTLED' | 'SETTLED';

/**
 * What GET /users/me/installments actually returns. It deliberately has no
 * `period`: the owner endpoint's mapping (OwnedInstallmentResponse) drops it, so
 * declaring it here promised a field that was always undefined at runtime.
 * `period` only exists on the single-installment endpoint - see Installment below.
 */
export interface OwnedInstallment {
  id: string;
  unitId: string;
  dueDate: string;
  amount: number;
  outstandingAmount: number;
  status: InstallmentStatus;
}

/** What GET /installments/{id} returns - same fields plus the originating call's period. */
export interface Installment extends OwnedInstallment {
  /** "YYYY-MM" of the installment call this echeance was raised from - null when raised manually (no call). */
  period: string | null;
}
