import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { useRecordBankCharge } from '@/features/property-mngt/accounting/hooks/useRecordBankCharge';
import {
  recordBankChargeSchema,
  type RecordBankChargeFormValues,
} from '@/features/property-mngt/accounting/schemas/recordBankChargeSchema';

export function RecordBankChargeForm({ propertyId }: { propertyId: string }) {
  const navigate = useNavigate();
  const ledgerAccounts = useLedgerAccounts(propertyId);
  const chargeAccounts = (ledgerAccounts.data ?? []).filter((account) => account.nature === 'EXPENSE');
  const bankAccounts = (ledgerAccounts.data ?? []).filter((account) => account.role === 'BANK');
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RecordBankChargeFormValues>({ resolver: zodResolver(recordBankChargeSchema) });
  const { mutate, isPending, error } = useRecordBankCharge(propertyId);

  function onSubmit(values: RecordBankChargeFormValues) {
    mutate(values, {
      onSuccess: (result) =>
        navigate(`/property-mngt/properties/${propertyId}/accounting/journal/${result.journalEntryId}`),
    });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Select label="Compte de charge" {...register('ledgerAccountId')} errorMessage={errors.ledgerAccountId?.message}>
        <option value="">Sélectionner…</option>
        {chargeAccounts.map((account) => (
          <option key={account.id} value={account.id}>
            {account.accountNumber} — {account.label}
          </option>
        ))}
      </Select>
      <Select label="Compte bancaire" {...register('bankAccountId')} errorMessage={errors.bankAccountId?.message}>
        <option value="">Sélectionner…</option>
        {bankAccounts.map((account) => (
          <option key={account.id} value={account.id}>
            {account.accountNumber} — {account.label}
          </option>
        ))}
      </Select>
      <Input label="Date" type="date" {...register('pieceDate')} errorMessage={errors.pieceDate?.message} />
      <Input
        label="Montant"
        type="number"
        min={0}
        step="any"
        {...register('amount', { valueAsNumber: true })}
        errorMessage={errors.amount?.message}
      />
      <Input label="Description" {...register('description')} errorMessage={errors.description?.message} />
      <Button type="submit" isLoading={isPending}>
        Enregistrer les frais
      </Button>
    </form>
  );
}
