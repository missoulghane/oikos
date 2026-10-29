import type { Paged } from '@/shared/types/pagination.types';

/** Mirrors InstallmentCallStatus (oikos-api). A generated call is POSTED; CANCELLED once reversed. */
export type InstallmentCallStatus = 'DRAFT' | 'ISSUED' | 'POSTED' | 'CANCELLED';

export interface InstallmentCallSummary {
  id: string;
  propertyId: string;
  period: string;
  dueDate: string;
  status: InstallmentCallStatus;
  unitCount: number;
  totalAmount: number;
}

export type PagedInstallmentCalls = Paged<InstallmentCallSummary>;

export interface GenerateInstallmentCallPayload {
  period: string;
  dueDate: string;
}

export interface GenerateInstallmentCallResult {
  id: string;
  propertyId: string;
  period: string;
  dueDate: string;
  status: InstallmentCallStatus;
  chargedUnitIds: string[];
  skippedUnitIds: string[];
}
