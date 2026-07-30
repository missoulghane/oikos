import type { Paged } from '@/shared/types/pagination.types';

export type InstallmentStatus = 'NOT_SETTLED' | 'PARTIALLY_SETTLED' | 'SETTLED';

export interface Installment {
  id: string;
  unitId: string;
  dueDate: string;
  amount: number;
  outstandingAmount: number;
  status: InstallmentStatus;
}

export type PagedInstallments = Paged<Installment>;

export const INSTALLMENT_SORT_FIELDS = ['DUE_DATE', 'AMOUNT'] as const;
export type InstallmentSortField = (typeof INSTALLMENT_SORT_FIELDS)[number];

export type SortDirection = 'ASC' | 'DESC';

export interface InstallmentListFilters {
  status?: InstallmentStatus[];
  dueDateFrom?: string;
  dueDateTo?: string;
  sortBy: InstallmentSortField;
  sortDirection: SortDirection;
}
