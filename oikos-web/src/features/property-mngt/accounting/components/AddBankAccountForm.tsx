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

export function AddBankAccountForm({ propertyId }: { propertyId: string }) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AddBankAccountFormValues>({ resolver: zodResolver(addBankAccountSchema) });
  const { mutate, isPending, error } = useAddBankAccount(propertyId);

  function onSubmit(values: AddBankAccountFormValues) {
    mutate(values, { onSuccess: () => reset() });
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
        <Button type="submit" isLoading={isPending}>
          Ajouter le compte bancaire
        </Button>
      </div>
    </form>
  );
}
