import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useTransferBetweenFinancialAccounts } from '@/features/property-mngt/accounting/hooks/useTransferBetweenFinancialAccounts';
import { transferSchema, type TransferFormValues } from '@/features/property-mngt/accounting/schemas/transferSchema';
import type { FinancialAccount } from '@/features/property-mngt/accounting/types/accounting.types';

interface TransferFormProps {
  propertyId: string;
  accounts: FinancialAccount[];
}

export function TransferForm({ propertyId, accounts }: TransferFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TransferFormValues>({ resolver: zodResolver(transferSchema) });
  const { mutate, isPending, error } = useTransferBetweenFinancialAccounts(propertyId);

  function onSubmit(values: TransferFormValues) {
    mutate(values, { onSuccess: () => reset() });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:flex-wrap">
        <Select label="Compte source" {...register('fromAccountId')} errorMessage={errors.fromAccountId?.message}>
          <option value="">Sélectionner…</option>
          {accounts.map((account) => (
            <option key={account.id} value={account.id}>
              {account.name}
            </option>
          ))}
        </Select>
        <Select label="Compte destination" {...register('toAccountId')} errorMessage={errors.toAccountId?.message}>
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
          Effectuer le virement
        </Button>
      </div>
    </form>
  );
}
