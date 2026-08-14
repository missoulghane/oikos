import type { InstallmentStatus } from '@/features/property-mngt/installments/types/installment.types';

/**
 * What GET /users/me/installments actually returns (OwnedInstallmentResponse
 * server-side) - deliberately NOT the manager-side `Installment`, which also
 * declares `period`. That field is dropped by the owner endpoint's mapping, so
 * typing this payload as `Installment` made TypeScript promise a `period` that
 * is always undefined at runtime. `period` is only available on the
 * single-installment detail endpoint (see useInstallment).
 */
export interface OwnedInstallment {
  id: string;
  unitId: string;
  dueDate: string;
  amount: number;
  outstandingAmount: number;
  status: InstallmentStatus;
}
