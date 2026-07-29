import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { useAddUnitOwner } from '@/features/property-mngt/properties/hooks/useAddUnitOwner';
import { useParties } from '@/features/property-mngt/parties/hooks/useParties';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { PARTY_TYPES } from '@/features/property-mngt/properties/types/property.types';
import { addUnitOwnerSchema, type AddUnitOwnerFormValues } from '@/features/property-mngt/properties/schemas/addUnitOwnerSchema';

interface AddUnitOwnerFormProps {
  unitId: string;
  propertyId: string;
  onSuccess: () => void;
  onCancel: () => void;
}

const SEARCH_DEBOUNCE_MS = 300;

export function AddUnitOwnerForm({ unitId, propertyId, onSuccess, onCancel }: AddUnitOwnerFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AddUnitOwnerFormValues>({
    resolver: zodResolver(addUnitOwnerSchema),
    defaultValues: { ownershipShare: 0 },
  });
  const { mutate, isPending, error } = useAddUnitOwner(unitId);
  const { onChange: onEmailChange, ...emailField } = register('email');

  const [emailInput, setEmailInput] = useState('');
  const [debouncedEmail, setDebouncedEmail] = useState('');
  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedEmail(emailInput.trim()), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [emailInput]);

  const parties = useParties(propertyId, 0, debouncedEmail || undefined);
  const existingParty = parties.data?.content.find(
    (party) => party.email.toLowerCase() === debouncedEmail.toLowerCase(),
  );

  function onSubmit(values: AddUnitOwnerFormValues) {
    mutate(values, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-lg border border-gray-200 p-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      {existingParty && (
        <Alert
          variant="warning"
          message={`Un contact existe déjà avec cet email : ${existingParty.fullName}. En confirmant, ce lot sera rattaché à ce contact existant — le nom, le téléphone et le type saisis ci-dessus seront ignorés.`}
        />
      )}
      <Input label="Nom complet" {...register('fullName')} errorMessage={errors.fullName?.message} />
      <Select label="Type" {...register('partyType')} errorMessage={errors.partyType?.message} defaultValue="">
        <option value="" disabled>
          Sélectionner un type
        </option>
        {PARTY_TYPES.map((type) => (
          <option key={type} value={type}>
            {PARTY_TYPE_LABELS[type]}
          </option>
        ))}
      </Select>
      <Input
        label="Email"
        type="email"
        {...emailField}
        onChange={(e) => {
          void onEmailChange(e);
          setEmailInput(e.target.value);
        }}
        errorMessage={errors.email?.message}
      />
      <Input label="Téléphone" {...register('phone')} errorMessage={errors.phone?.message} />
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
          {existingParty ? 'Rattacher au contact existant' : 'Ajouter le propriétaire'}
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
