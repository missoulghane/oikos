import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useLedgerAccounts } from '@/features/property-mngt/accounting/hooks/useLedgerAccounts';
import { useRecordTreasuryTransfer } from '@/features/property-mngt/accounting/hooks/useRecordTreasuryTransfer';
import {
  recordTreasuryTransferSchema,
  type RecordTreasuryTransferFormValues,
} from '@/features/property-mngt/accounting/schemas/recordTreasuryTransferSchema';
import type { JournalEntryReference } from '@/features/property-mngt/accounting/types/accounting.types';

interface RecordTreasuryTransferFormProps {
  propertyId: string;
  // Pre-selected (but still changeable) as the source, matching the account
  // whose page the "Virement entre comptes" action was clicked from - either
  // direction (caisse->banque or banque->caisse) is handled the same way,
  // the user just flips which select holds which account.
  defaultSourceAccountId?: string;
  onSuccess: (result: JournalEntryReference) => void;
}

export function RecordTreasuryTransferForm({
  propertyId,
  defaultSourceAccountId,
  onSuccess,
}: RecordTreasuryTransferFormProps) {
  const ledgerAccounts = useLedgerAccounts(propertyId);
  const treasuryAccounts = (ledgerAccounts.data ?? []).filter(
    (account) => account.role === 'BANK' || account.role === 'CASH',
  );
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RecordTreasuryTransferFormValues>({
    resolver: zodResolver(recordTreasuryTransferSchema),
    defaultValues: defaultSourceAccountId ? { sourceAccountId: defaultSourceAccountId } : undefined,
  });
  const { mutate, isPending, error } = useRecordTreasuryTransfer(propertyId);

  function onSubmit(values: RecordTreasuryTransferFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Select label="Compte source" {...register('sourceAccountId')} errorMessage={errors.sourceAccountId?.message}>
        <option value="">Sélectionner…</option>
        {treasuryAccounts.map((account) => (
          <option key={account.id} value={account.id}>
            {account.accountNumber} — {account.label}
          </option>
        ))}
      </Select>
      <Select
        label="Compte destination"
        {...register('destinationAccountId')}
        errorMessage={errors.destinationAccountId?.message}
      >
        <option value="">Sélectionner…</option>
        {treasuryAccounts.map((account) => (
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
        Enregistrer le virement
      </Button>
    </form>
  );
}
