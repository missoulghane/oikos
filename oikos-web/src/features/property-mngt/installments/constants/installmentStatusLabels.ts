import type { InstallmentStatus } from '@/features/property-mngt/installments/types/installment.types';

export const INSTALLMENT_STATUS_LABELS: Record<InstallmentStatus, string> = {
  NOT_PAID: 'Non payée',
  OVERDUE: 'En retard',
};

export const INSTALLMENT_STATUS_CLASSES: Record<InstallmentStatus, string> = {
  NOT_PAID: 'bg-slate-100 text-slate-600',
  OVERDUE: 'bg-red-100 text-red-700',
};
