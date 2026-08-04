import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useRecordExceptionalDeposit } from '@/features/property-mngt/accounting/hooks/useRecordExceptionalDeposit';
import {
  recordExceptionalDepositSchema,
  type RecordExceptionalDepositFormValues,
} from '@/features/property-mngt/accounting/schemas/recordExceptionalDepositSchema';
import type { FinancialAccount } from '@/features/property-mngt/accounting/types/accounting.types';

interface RecordExceptionalDepositFormProps {
  propertyId: string;
  accounts: FinancialAccount[];
}

export function RecordExceptionalDepositForm({ propertyId, accounts }: RecordExceptionalDepositFormProps) {
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RecordExceptionalDepositFormValues>({ resolver: zodResolver(recordExceptionalDepositSchema) });
  const { mutate, isPending, error } = useRecordExceptionalDeposit(propertyId);

  function onSubmit(values: RecordExceptionalDepositFormValues) {
    mutate(values, { onSuccess: () => navigate(`/property-mngt/properties/${propertyId}/accounting/financial-accounts`) });
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
          Enregistrer le dépôt
        </Button>
      </div>
    </form>
  );
}
