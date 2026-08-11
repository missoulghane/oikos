import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useAddBankAccount } from '@/features/property-mngt/accounting/hooks/useAddBankAccount';
import {
  addBankAccountSchema,
  type AddBankAccountFormValues,
} from '@/features/property-mngt/accounting/schemas/addBankAccountSchema';

interface AddBankAccountFormProps {
  propertyId: string;
  onCreated?: (accountId: string) => void;
}

export function AddBankAccountForm({ propertyId, onCreated }: AddBankAccountFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AddBankAccountFormValues>({ resolver: zodResolver(addBankAccountSchema) });
  const { mutate, isPending, error } = useAddBankAccount(propertyId);

  function onSubmit(values: AddBankAccountFormValues) {
    mutate({ ...values, bankAccountNumber: values.bankAccountNumber || undefined }, {
      onSuccess: (accountId) => {
        reset();
        onCreated?.(accountId);
      },
    });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
        <Input
          label="Nom de la banque"
          placeholder="Attijariwafa Bank"
          {...register('label')}
          errorMessage={errors.label?.message}
        />
        <Input
          label="Numéro de compte (optionnel)"
          placeholder="RIB ou IBAN"
          {...register('bankAccountNumber')}
          errorMessage={errors.bankAccountNumber?.message}
        />
        <Button type="submit" isLoading={isPending}>
          Ajouter le compte bancaire
        </Button>
      </div>
    </form>
  );
}
