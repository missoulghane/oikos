import { useEffect, useState } from 'react';
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
import { UnitPicker } from '@/features/property-mngt/properties/components/UnitPicker';
import {
  recordOwnerPaymentSchema,
  type RecordOwnerPaymentFormValues,
} from '@/features/property-mngt/installments/schemas/recordOwnerPaymentSchema';

interface RecordOwnerPaymentFormProps {
  propertyId: string;
  // Fixed when reached from the unit's own page; when absent (accounting overview
  // "click an account, saisir une recette" flow) a lot picker is shown instead.
  unitId?: string;
  // Set when reached from a treasury account's operations page - the account is
  // then fixed rather than user-selected, mirroring the expense-side flow.
  fixedTreasuryAccountId?: string;
  onSuccess: () => void;
}

export function RecordOwnerPaymentForm({
  propertyId,
  unitId: fixedUnitId,
  fixedTreasuryAccountId,
  onSuccess,
}: RecordOwnerPaymentFormProps) {
  const [pickedUnitId, setPickedUnitId] = useState('');
  const unitId = fixedUnitId ?? pickedUnitId;
  const ledgerAccounts = useLedgerAccounts(propertyId);
  const treasuryAccounts = (ledgerAccounts.data ?? []).filter(
    (account) => account.role === 'BANK' || account.role === 'CASH',
  );
  const {
    register,
    handleSubmit,
    watch,
    setValue,
    getValues,
    resetField,
    formState: { errors },
  } = useForm<RecordOwnerPaymentFormValues>({
    resolver: zodResolver(recordOwnerPaymentSchema),
    defaultValues: fixedTreasuryAccountId ? { treasuryAccountId: fixedTreasuryAccountId } : undefined,
  });
  const { mutate, isPending, error } = useRecordOwnerPayment(propertyId, unitId);

  // Moroccan cash-accounting rule: a caisse account only ever receives especes,
  // a banque account never does - the mode field is locked/filtered accordingly
  // as soon as the impacted account is known (fixed above, or picked below).
  // The backend enforces the same rule (PostOwnerPaymentJournalEntryService).
  const selectedTreasuryAccountId = watch('treasuryAccountId');
  const selectedAccount = ledgerAccounts.data?.find((account) => account.id === selectedTreasuryAccountId);
  const isCashAccount = selectedAccount?.role === 'CASH';
  const isBankAccount = selectedAccount?.role === 'BANK';
  const modeOptions = isBankAccount
    ? Object.entries(PAYMENT_MODE_LABELS).filter(([mode]) => mode !== 'CASH')
    : Object.entries(PAYMENT_MODE_LABELS);

  useEffect(() => {
    if (isCashAccount) {
      setValue('mode', 'CASH');
    } else if (isBankAccount && getValues('mode') === 'CASH') {
      resetField('mode');
    }
  }, [isCashAccount, isBankAccount, setValue, getValues, resetField]);

  function onSubmit(values: RecordOwnerPaymentFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      {!fixedUnitId && <UnitPicker propertyId={propertyId} unitId={pickedUnitId} onChange={setPickedUnitId} />}
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
      <Select
        label="Moyen de paiement"
        {...register('mode')}
        errorMessage={errors.mode?.message}
        disabled={isCashAccount}
      >
        <option value="">Sélectionner…</option>
        {modeOptions.map(([value, label]) => (
          <option key={value} value={value}>
            {label}
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
      <Button type="submit" isLoading={isPending} disabled={!unitId}>
        Enregistrer le paiement
      </Button>
    </form>
  );
}
