import { Link } from 'react-router-dom';
import type { Payment } from '@/features/property-mngt/installments/types/payment.types';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';

interface PaymentRowProps {
  payment: Payment;
  propertyId: string;
}

export function PaymentRow({ payment, propertyId }: PaymentRowProps) {
  return (
    <li className="flex flex-col gap-1 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p className="text-sm font-medium text-gray-900 dark:text-white/90">
          {payment.amount.toLocaleString('fr-FR')} MAD
        </p>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          {new Date(payment.valueDate).toLocaleDateString('fr-FR')} · {PAYMENT_MODE_LABELS[payment.mode]}
        </p>
      </div>
      <Link
        to={`/property-mngt/properties/${propertyId}/accounting/journal/${payment.journalEntryId}`}
        className="shrink-0 text-sm font-medium text-brand-500 dark:text-brand-400 hover:underline"
      >
        Voir l'écriture
      </Link>
    </li>
  );
}
