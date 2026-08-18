import type { Paged } from '@/shared/types/pagination.types';

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

export type PagedInstallments = Paged<Installment>;

export const INSTALLMENT_SORT_FIELDS = ['DUE_DATE', 'AMOUNT'] as const;
export type InstallmentSortField = (typeof INSTALLMENT_SORT_FIELDS)[number];

export type SortDirection = 'ASC' | 'DESC';

export interface InstallmentListFilters {
  status?: InstallmentStatus[];
  dueDateFrom?: string;
  dueDateTo?: string;
  installmentCallId?: string;
  /**
   * Drops the installments not settled and falling due after today. Applied
   * server-side because the list is paginated: filtering the page client-side
   * would leave a page of 20 showing 14 rows and a wrong total.
   */
  excludeNotYetDue?: boolean;
  /**
   * Free text matched against the lot number, and the owner's name or phone -
   * the property module resolves it, so it means exactly what it means on the
   * lots list.
   */
  search?: string;
  sortBy: InstallmentSortField;
  sortDirection: SortDirection;
}

/**
 * The two figures of the dashboard's "à collecter": installments unpaid
 * (nothing received on them) and already due, and what they add up to. The set
 * is exactly what the tracking list shows under "Non soldée" with "à échoir"
 * off, so the badge and the page it opens agree.
 */
export interface InstallmentCollectionSummary {
  count: number;
  amount: number;
}
