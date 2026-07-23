import type { InstallmentStatus } from '@/features/property-mngt/installments/types/installment.types';

export const INSTALLMENT_STATUS_LABELS: Record<InstallmentStatus, string> = {
  NOT_PAID: 'Non payée',
  PARTIALLY_PAID: 'Partiellement payée',
  PAID: 'Payée',
  OVERDUE: 'En retard',
};

export const INSTALLMENT_STATUS_CLASSES: Record<InstallmentStatus, string> = {
  NOT_PAID: 'bg-slate-100 text-slate-600',
  PARTIALLY_PAID: 'bg-amber-100 text-amber-700',
  PAID: 'bg-green-100 text-green-700',
  OVERDUE: 'bg-red-100 text-red-700',
};
