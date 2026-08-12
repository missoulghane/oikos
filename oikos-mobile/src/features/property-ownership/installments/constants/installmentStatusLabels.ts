import type { InstallmentStatus } from '@/features/property-ownership/installments/types/installment.types';

export const INSTALLMENT_STATUS_LABELS: Record<InstallmentStatus, string> = {
  NOT_SETTLED: 'Non soldée',
  PARTIALLY_SETTLED: 'Partiellement soldée',
  SETTLED: 'Soldée',
};

export const INSTALLMENT_STATUS_BADGE_COLORS: Record<InstallmentStatus, 'light' | 'warning' | 'success'> = {
  NOT_SETTLED: 'light',
  PARTIALLY_SETTLED: 'warning',
  SETTLED: 'success',
};
