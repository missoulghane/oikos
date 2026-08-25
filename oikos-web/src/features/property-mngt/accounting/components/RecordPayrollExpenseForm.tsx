import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { useRecordPayrollExpense } from '@/features/property-mngt/accounting/hooks/useRecordPayrollExpense';
import {
  recordPayrollExpenseSchema,
  type RecordPayrollExpenseFormValues,
} from '@/features/property-mngt/accounting/schemas/recordPayrollExpenseSchema';
import { RequiredFieldsHint } from '@/shared/components/RequiredFieldsHint/RequiredFieldsHint';

export function RecordPayrollExpenseForm({ propertyId }: { propertyId: string }) {
  const navigate = useNavigate();
  const ledgerAccounts = useLedgerAccounts(propertyId);
  const chargeAccounts = (ledgerAccounts.data ?? []).filter((account) => account.nature === 'EXPENSE');
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RecordPayrollExpenseFormValues>({ resolver: zodResolver(recordPayrollExpenseSchema) });
  const { mutate, isPending, error } = useRecordPayrollExpense(propertyId);

  function onSubmit(values: RecordPayrollExpenseFormValues) {
    mutate(values, {
      onSuccess: (result) =>
        navigate(`/property-mngt/properties/${propertyId}/accounting/journal/${result.journalEntryId}`),
    });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <RequiredFieldsHint />
      <Select label="Compte de charge" required {...register('ledgerAccountId')} errorMessage={errors.ledgerAccountId?.message}>
        <option value="">Sélectionner…</option>
        {chargeAccounts.map((account) => (
          <option key={account.id} value={account.id}>
            {account.accountNumber} — {account.label}
          </option>
        ))}
      </Select>
      <Input label="Date" required type="date" {...register('date')} errorMessage={errors.date?.message} />
      <Input
        label="Montant"
        required
        type="number"
        min={0}
        step="any"
        {...register('amount', { valueAsNumber: true })}
        errorMessage={errors.amount?.message}
      />
      <Input label="Description" {...register('description')} errorMessage={errors.description?.message} />
      <Button type="submit" isLoading={isPending}>
        Enregistrer la charge
      </Button>
    </form>
  );
}
