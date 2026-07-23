import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useAddUnitTypeDefinition } from '@/features/property-mngt/properties/hooks/useAddUnitTypeDefinition';
import {
  unitTypeNameSchema,
  type UnitTypeNameFormValues,
} from '@/features/property-mngt/properties/schemas/unitTypeNameSchema';

export function AddUnitTypeForm({ propertyId }: { propertyId: string }) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<UnitTypeNameFormValues>({ resolver: zodResolver(unitTypeNameSchema) });
  const { mutate, isPending, error } = useAddUnitTypeDefinition(propertyId);

  function onSubmit(values: UnitTypeNameFormValues) {
    mutate(values.name, { onSuccess: () => reset() });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-2" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
        <Input label="Nouveau type de lot" {...register('name')} errorMessage={errors.name?.message} />
        <Button type="submit" isLoading={isPending}>
          Ajouter un type
        </Button>
      </div>
    </form>
  );
}
