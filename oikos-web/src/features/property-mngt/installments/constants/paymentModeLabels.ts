import type { PaymentMode } from '@/features/property-mngt/installments/types/payment.types';

export const PAYMENT_MODE_LABELS: Record<PaymentMode, string> = {
  BANK_TRANSFER: 'Virement',
  CASH: 'Espèces',
  CHECK: 'Chèque',
  DIRECT_DEBIT: 'Prélèvement',
};
