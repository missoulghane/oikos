import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { useRecordSupplierPayment } from '@/features/property-mngt/accounting/hooks/useRecordSupplierPayment';
import {
  recordSupplierPaymentSchema,
  type RecordSupplierPaymentFormValues,
} from '@/features/property-mngt/accounting/schemas/recordSupplierPaymentSchema';
import type { Expense } from '@/features/property-mngt/accounting/types/accounting.types';

interface RecordSupplierPaymentFormProps {
  propertyId: string;
  // Set when reached from a treasury account's operations page (accounting overview
  // "click an account" flow) - the account is then fixed rather than user-selected,
  // mirroring the "Nouvelle dépense" flow but skipping that one step.
  fixedTreasuryAccountId?: string;
  onSuccess: (result: Expense) => void;
}

export function RecordSupplierPaymentForm({
  propertyId,
  fixedTreasuryAccountId,
  onSuccess,
}: RecordSupplierPaymentFormProps) {
  const ledgerAccounts = useLedgerAccounts(propertyId);
  const chargeAccounts = (ledgerAccounts.data ?? []).filter((account) => account.nature === 'EXPENSE');
  const treasuryAccounts = (ledgerAccounts.data ?? []).filter(
    (account) => account.role === 'BANK' || account.role === 'CASH',
  );
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RecordSupplierPaymentFormValues>({
    resolver: zodResolver(recordSupplierPaymentSchema),
    defaultValues: fixedTreasuryAccountId ? { treasuryAccountId: fixedTreasuryAccountId } : undefined,
  });
  const { mutate, isPending, isSuccess, error } = useRecordSupplierPayment(propertyId);

  function onSubmit(values: RecordSupplierPaymentFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      {isSuccess && <Alert variant="success" message="Règlement enregistré." />}
      <Select label="Compte de charge" {...register('ledgerAccountId')} errorMessage={errors.ledgerAccountId?.message}>
        <option value="">Sélectionner…</option>
        {chargeAccounts.map((account) => (
          <option key={account.id} value={account.id}>
            {account.accountNumber} — {account.label}
          </option>
        ))}
      </Select>
      {fixedTreasuryAccountId ? (
        <input type="hidden" {...register('treasuryAccountId')} />
      ) : (
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
      )}
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
      <Input
        label="Référence"
        {...register('externalReference')}
        errorMessage={errors.externalReference?.message}
      />
      <Button type="submit" isLoading={isPending}>
        Enregistrer le règlement
      </Button>
    </form>
  );
}
