import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useRecordExpense } from '@/features/property-mngt/accounting/hooks/useRecordExpense';
import {
  recordExpenseSchema,
  type RecordExpenseFormValues,
} from '@/features/property-mngt/accounting/schemas/recordExpenseSchema';
import type { FinancialAccount } from '@/features/property-mngt/accounting/types/accounting.types';

interface RecordExpenseFormProps {
  propertyId: string;
  accounts: FinancialAccount[];
}

export function RecordExpenseForm({ propertyId, accounts }: RecordExpenseFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<RecordExpenseFormValues>({ resolver: zodResolver(recordExpenseSchema) });
  const { mutate, isPending, error } = useRecordExpense(propertyId);

  function onSubmit(values: RecordExpenseFormValues) {
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
        <Input label="Date" type="date" {...register('date')} errorMessage={errors.date?.message} />
        <Input label="Catégorie" {...register('category')} errorMessage={errors.category?.message} />
        <Input label="Fournisseur" {...register('provider')} errorMessage={errors.provider?.message} />
        <Input
          label="Montant"
          type="number"
          min={0}
          step="any"
          {...register('amount', { valueAsNumber: true })}
          errorMessage={errors.amount?.message}
        />
        <Input label="Description" {...register('description')} errorMessage={errors.description?.message} />
        <Input
          label="Référence justificatif"
          {...register('receiptReference')}
          errorMessage={errors.receiptReference?.message}
        />
        <Button type="submit" isLoading={isPending}>
          Enregistrer la dépense
        </Button>
      </div>
    </form>
  );
}
