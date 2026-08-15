import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useAddUnitOwnership } from '@/features/property-mngt/properties/hooks/useAddUnitOwnership';
import { useParties } from '@/features/property-mngt/parties/hooks/useParties';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import {
  addUnitOwnershipSchema,
  type AddUnitOwnershipFormValues,
} from '@/features/property-mngt/properties/schemas/addUnitOwnershipSchema';

interface AddExistingUnitOwnerFormProps {
  unitId: string;
  propertyId: string | undefined;
  onSuccess: () => void;
  onCancel: () => void;
}

const SEARCH_DEBOUNCE_MS = 300;

export function AddExistingUnitOwnerForm({ unitId, propertyId, onSuccess, onCancel }: AddExistingUnitOwnerFormProps) {
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    const timeout = setTimeout(() => setSearch(searchInput), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [searchInput]);

  const parties = useParties(propertyId, 0, { search: search || undefined });
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AddUnitOwnershipFormValues>({ resolver: zodResolver(addUnitOwnershipSchema) });
  const { mutate, isPending, error } = useAddUnitOwnership(unitId);

  function onSubmit(values: AddUnitOwnershipFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-lg border border-gray-200 dark:border-gray-800 p-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      {parties.isError && <Alert message={getErrorMessage(parties.error)} />}
      <Input
        label="Rechercher une party (nom, email)"
        value={searchInput}
        onChange={(e) => setSearchInput(e.target.value)}
      />
      <Select label="Party" {...register('partyId')} errorMessage={errors.partyId?.message} defaultValue="" disabled={parties.isLoading}>
        <option value="" disabled>
          Sélectionner une party
        </option>
        {parties.data?.content.map((party) => (
          <option key={party.id} value={party.id}>
            {party.fullName} — {party.email}
          </option>
        ))}
      </Select>
      <Input
        label="Part de propriété (%)"
        type="number"
        min={0}
        max={100}
        step="any"
        {...register('ownershipShare', { valueAsNumber: true })}
        errorMessage={errors.ownershipShare?.message}
      />
      <div className="mt-2 flex gap-2">
        <Button type="submit" isLoading={isPending}>
          Rattacher la party
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
