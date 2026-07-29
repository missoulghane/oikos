import type { InstallmentStatus } from '@/features/property-mngt/installments/types/installment.types';

export const INSTALLMENT_STATUS_LABELS: Record<InstallmentStatus, string> = {
  NOT_PAID: 'Non payée',
  OVERDUE: 'En retard',
};

export const INSTALLMENT_STATUS_BADGE_COLORS: Record<InstallmentStatus, 'light' | 'error'> = {
  NOT_PAID: 'light',
  OVERDUE: 'error',
};
