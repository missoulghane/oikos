import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useAddPropertyManager } from '@/features/property-mngt/properties/hooks/useAddPropertyManager';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import {
  addPropertyManagerSchema,
  type AddPropertyManagerFormValues,
} from '@/features/property-mngt/properties/schemas/addPropertyManagerSchema';

export function AddPropertyManagerForm({ propertyId }: { propertyId: string }) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AddPropertyManagerFormValues>({ resolver: zodResolver(addPropertyManagerSchema) });
  const { mutate, isPending, isSuccess, error } = useAddPropertyManager(propertyId);

  function onSubmit(values: AddPropertyManagerFormValues) {
    mutate(values.email, { onSuccess: () => reset() });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      {isSuccess && <Alert variant="success" message="Membre ajouté avec succès." />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
        <div className="flex-1">
          <Input
            label="Email du membre à inviter"
            type="email"
            {...register('email')}
            errorMessage={errors.email?.message}
          />
        </div>
        <Button type="submit" isLoading={isPending}>
          Inviter
        </Button>
      </div>
    </form>
  );
}
