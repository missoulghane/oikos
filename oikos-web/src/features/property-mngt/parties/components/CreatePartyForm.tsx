import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useCreateParty } from '@/features/property-mngt/parties/hooks/useCreateParty';
import { createPartySchema, type CreatePartyFormValues } from '@/features/property-mngt/parties/schemas/createPartySchema';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { PARTY_TYPES } from '@/features/property-mngt/properties/types/property.types';

interface CreatePartyFormProps {
  propertyId: string;
  onSuccess: (partyId: string, fullName: string) => void;
  onCancel?: () => void;
}

/**
 * Creates a standalone contact - not tied to any unit/lot, unlike
 * AddUnitOwnerForm which always creates an ownership alongside the party.
 * Used for contacts that are not copropriétaires (board members, staff...) -
 * suppliers are no longer Party-backed (Partie 2: a supplier is just a
 * charge account/label, see RecordSupplierPaymentForm).
 *
 * Deliberately a <div>, not a <form>: this component can be embedded inline
 * inside other forms - a nested <form> is invalid HTML and made the outer
 * form's submit behave unpredictably. Submission is triggered directly via
 * handleSubmit() on the button's onClick instead of an onSubmit handler.
 */
export function CreatePartyForm({ propertyId, onSuccess, onCancel }: CreatePartyFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreatePartyFormValues>({ resolver: zodResolver(createPartySchema) });
  const { mutate, isPending, error } = useCreateParty(propertyId);

  function onSubmit(values: CreatePartyFormValues) {
    mutate(values, { onSuccess: (result) => onSuccess(result.id, values.fullName) });
  }

  return (
    <div className="flex flex-col gap-4 rounded-lg border border-gray-200 p-4">
      {error && <Alert message={getErrorMessage(error)} />}
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
      <Input label="Email" type="email" {...register('email')} errorMessage={errors.email?.message} />
      <Input label="Téléphone" {...register('phone')} errorMessage={errors.phone?.message} />
      <div className="flex gap-2">
        <Button type="button" isLoading={isPending} onClick={handleSubmit(onSubmit)}>
          Créer le contact
        </Button>
        {onCancel && (
          <Button type="button" variant="secondary" onClick={onCancel}>
            Annuler
          </Button>
        )}
      </div>
    </div>
  );
}
