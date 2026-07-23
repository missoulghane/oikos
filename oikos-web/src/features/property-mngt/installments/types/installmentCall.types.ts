import type { Paged } from '@/shared/types/pagination.types';
import type { Installment } from '@/features/property-mngt/installments/types/installment.types';

export interface InstallmentCallSummary {
  id: string;
  propertyId: string;
  period: string;
  dueDate: string;
  unitCount: number;
  totalAmount: number;
}

export type PagedInstallmentCalls = Paged<InstallmentCallSummary>;

export interface InstallmentCallDetail {
  id: string;
  propertyId: string;
  period: string;
  dueDate: string;
  installments: Installment[];
}

export interface GenerateInstallmentCallPayload {
  period: string;
  dueDate: string;
}

export interface GenerateInstallmentCallResult {
  id: string;
  propertyId: string;
  period: string;
  dueDate: string;
  chargedUnitIds: string[];
  skippedUnitIds: string[];
}
