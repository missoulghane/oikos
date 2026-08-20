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
      {/* Les largeurs sont portées par des conteneurs : Input passe son className
          au champ lui-même, déjà en w-full, et non à la colonne qui l'entoure.
          Le numéro de compte est le plus large des deux - un RIB fait 24
          caractères, un IBAN davantage, là où un nom de banque tient en un mot. */}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
        <div className="sm:flex-1">
          <Input
            label="Nom de la banque"
            placeholder="Attijariwafa Bank"
            {...register('label')}
            errorMessage={errors.label?.message}
          />
        </div>
        <div className="sm:flex-[2]">
          <Input
            label="Numéro de compte"
            placeholder="RIB ou IBAN"
            {...register('bankAccountNumber')}
            errorMessage={errors.bankAccountNumber?.message}
          />
        </div>
        <Button type="submit" isLoading={isPending}>
          Ajouter le compte bancaire
        </Button>
      </div>
    </form>
  );
}
