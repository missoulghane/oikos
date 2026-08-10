import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { useRecordOwnerPayment } from '@/features/property-mngt/installments/hooks/useRecordOwnerPayment';
import { PAYMENT_MODE_LABELS } from '@/features/property-mngt/installments/constants/paymentModeLabels';
import {
  recordOwnerPaymentSchema,
  type RecordOwnerPaymentFormValues,
} from '@/features/property-mngt/installments/schemas/recordOwnerPaymentSchema';

interface RecordOwnerPaymentFormProps {
  propertyId: string;
  unitId: string;
  onSuccess: () => void;
}

export function RecordOwnerPaymentForm({ propertyId, unitId, onSuccess }: RecordOwnerPaymentFormProps) {
  const ledgerAccounts = useLedgerAccounts(propertyId);
  const treasuryAccounts = (ledgerAccounts.data ?? []).filter(
    (account) => account.role === 'BANK' || account.role === 'CASH',
  );
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RecordOwnerPaymentFormValues>({ resolver: zodResolver(recordOwnerPaymentSchema) });
  const { mutate, isPending, error } = useRecordOwnerPayment(propertyId, unitId);

  function onSubmit(values: RecordOwnerPaymentFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:flex-wrap">
        <Select label="Moyen de paiement" {...register('mode')} errorMessage={errors.mode?.message}>
          <option value="">Sélectionner…</option>
          {Object.entries(PAYMENT_MODE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </Select>
        <Select
          label="Compte impacté"
          {...register('treasuryAccountId')}
          errorMessage={errors.treasuryAccountId?.message}
        >
          <option value="">Sélectionner…</option>
          {treasuryAccounts.map((account) => (
            <option key={account.id} value={account.id}>
              {account.accountNumber} — {account.label}
            </option>
          ))}
        </Select>
        <Input label="Date" type="date" {...register('valueDate')} errorMessage={errors.valueDate?.message} />
        <Input
          label="Montant"
          type="number"
          min={0}
          step="any"
          {...register('amount', { valueAsNumber: true })}
          errorMessage={errors.amount?.message}
        />
        <Button type="submit" isLoading={isPending}>
          Enregistrer le paiement
        </Button>
      </div>
    </form>
  );
}
