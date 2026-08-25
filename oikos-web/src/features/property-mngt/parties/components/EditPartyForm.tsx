import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { PhoneField } from '@/shared/components/PhoneField/PhoneField';
import { RadioGroup } from '@/shared/components/RadioGroup/RadioGroup';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { RequiredFieldsHint } from '@/shared/components/RequiredFieldsHint/RequiredFieldsHint';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { PARTY_TYPE_LABELS } from '@/features/property-mngt/properties/constants/partyTypeLabels';
import { PARTY_TYPES } from '@/features/property-mngt/properties/types/property.types';
import { useUpdateParty } from '@/features/property-mngt/parties/hooks/useUpdateParty';
import {
  updatePartySchema,
  type UpdatePartyFormValues,
} from '@/features/property-mngt/parties/schemas/updatePartySchema';
import type { PartyDetail } from '@/features/property-mngt/parties/types/party.types';

interface EditPartyFormProps {
  party: PartyDetail;
  onSuccess: () => void;
  onCancel: () => void;
}

const PARTY_TYPE_OPTIONS = PARTY_TYPES.map((type) => ({ value: type, label: PARTY_TYPE_LABELS[type] }));

/**
 * La fiche complète d'un contact - nom, type, coordonnées - et non plus le seul
 * téléphone. Ouverte au syndic tant qu'aucun compte n'est rattaché au contact :
 * dès qu'il y en a un, c'est son titulaire qui tient son identité à jour, et la
 * fiche passe en lecture seule (voir PartyDetailPage).
 */
export function EditPartyForm({ party, onSuccess, onCancel }: EditPartyFormProps) {
  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdatePartyFormValues>({
    resolver: zodResolver(updatePartySchema),
    defaultValues: {
      fullName: party.fullName,
      partyType: party.partyType,
      email: party.email ?? '',
      phone: party.phone ?? '',
    },
  });
  const { mutate, isPending, error } = useUpdateParty(party.id);

  function onSubmit(values: UpdatePartyFormValues) {
    // Une adresse vide part absente : l'API distingue « pas d'email » de « email
    // vide », et c'est la première qui décrit un contact sans adresse - même
    // règle qu'au rattachement d'un copropriétaire.
    mutate({ ...values, email: values.email || undefined, phone: values.phone || undefined }, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <RequiredFieldsHint />
      <Input label="Nom complet" required {...register('fullName')} errorMessage={errors.fullName?.message} />
      <RadioGroup
        label="Type"
        required
        options={PARTY_TYPE_OPTIONS}
        {...register('partyType')}
        errorMessage={errors.partyType?.message}
      />
      <Input label="Email" type="email" {...register('email')} errorMessage={errors.email?.message} />
      <Controller
        control={control}
        name="phone"
        defaultValue=""
        render={({ field }) => (
          <PhoneField
            label="Téléphone"
            name={field.name}
            value={field.value ?? ''}
            onChange={field.onChange}
            onBlur={field.onBlur}
            errorMessage={errors.phone?.message}
          />
        )}
      />
      <div className="flex gap-2">
        <Button type="submit" isLoading={isPending}>
          Enregistrer
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
