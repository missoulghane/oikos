import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useRecordOwnerPayment } from '@/features/property-mngt/accounting/hooks/useRecordOwnerPayment';
import {
  recordOwnerPaymentSchema,
  type RecordOwnerPaymentFormValues,
} from '@/features/property-mngt/accounting/schemas/recordOwnerPaymentSchema';
import type { FinancialAccount } from '@/features/property-mngt/accounting/types/accounting.types';

interface RecordPaymentFormProps {
  propertyId: string;
  unitId: string;
  accounts: FinancialAccount[];
}

export function RecordPaymentForm({ propertyId, unitId, accounts }: RecordPaymentFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<RecordOwnerPaymentFormValues>({ resolver: zodResolver(recordOwnerPaymentSchema) });
  const { mutate, isPending, error } = useRecordOwnerPayment(propertyId, unitId);

  function onSubmit(values: RecordOwnerPaymentFormValues) {
    mutate(values, { onSuccess: () => reset() });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:flex-wrap">
        <Select
          label="Compte"
          {...register('financialAccountId')}
          errorMessage={errors.financialAccountId?.message}
        >
          <option value="">Sélectionner…</option>
          {accounts.map((account) => (
            <option key={account.id} value={account.id}>
              {account.name}
            </option>
          ))}
        </Select>
        <Input
          label="Montant"
          type="number"
          min={0}
          step="any"
          {...register('amount', { valueAsNumber: true })}
          errorMessage={errors.amount?.message}
        />
        <Input label="Date" type="date" {...register('date')} errorMessage={errors.date?.message} />
        <Input label="Libellé" {...register('label')} errorMessage={errors.label?.message} />
        <Button type="submit" isLoading={isPending}>
          Enregistrer le paiement
        </Button>
      </div>
    </form>
  );
}
