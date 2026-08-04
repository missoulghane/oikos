import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useBuildings } from '@/features/property-mngt/properties/hooks/useBuildings';
import { useUnits } from '@/features/property-mngt/properties/hooks/useUnits';
import { useCreateInvitation } from '@/features/property-mngt/invitations/hooks/useCreateInvitation';
import {
  createInvitationSchema,
  type CreateInvitationFormValues,
} from '@/features/property-mngt/invitations/schemas/createInvitationSchema';
import { INVITATION_TYPES, type InvitationType } from '@/features/property-mngt/invitations/types/invitation.types';

const INVITATION_TYPE_LABELS: Record<InvitationType, string> = {
  PUBLIC: 'Lien public (QR code, plusieurs candidats, validation manager)',
  PRIVATE_WITH_UNIT: 'Invitation privée - lot déjà attribué',
  PRIVATE_WITHOUT_UNIT: 'Invitation privée - lot au choix de l’invité',
};

const UNIT_PICKER_SIZE = 100;

interface CreateInvitationFormProps {
  propertyId: string;
  onSuccess: () => void;
  onCancel: () => void;
}

export function CreateInvitationForm({ propertyId, onSuccess, onCancel }: CreateInvitationFormProps) {
  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<CreateInvitationFormValues>({
    resolver: zodResolver(createInvitationSchema),
    defaultValues: { type: 'PUBLIC', unitId: '', targetEmail: '' },
  });
  const type = watch('type');
  const [buildingId, setBuildingId] = useState('');
  const buildings = useBuildings(propertyId);
  const units = useUnits(buildingId, 0, undefined, UNIT_PICKER_SIZE);
  const availableUnits = (units.data?.content ?? []).filter((unit) => unit.ownershipStatus === 'NOT_AFFECTED');
  const { mutate, isPending, error } = useCreateInvitation(propertyId);

  useEffect(() => {
    setValue('unitId', '');
  }, [buildingId, setValue]);

  function onSubmit(values: CreateInvitationFormValues) {
    mutate(
      {
        propertyId,
        type: values.type,
        unitId: values.type === 'PRIVATE_WITH_UNIT' ? values.unitId : undefined,
        targetEmail: values.type !== 'PUBLIC' ? values.targetEmail : undefined,
      },
      { onSuccess },
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Select label="Type d'invitation" {...register('type')} errorMessage={errors.type?.message}>
        {INVITATION_TYPES.map((invitationType) => (
          <option key={invitationType} value={invitationType}>
            {INVITATION_TYPE_LABELS[invitationType]}
          </option>
        ))}
      </Select>

      {type === 'PRIVATE_WITH_UNIT' && (
        <>
          <Select label="Immeuble" value={buildingId} onChange={(e) => setBuildingId(e.target.value)}>
            <option value="">Sélectionnez un immeuble</option>
            {buildings.data?.content.map((building) => (
              <option key={building.id} value={building.id}>
                {building.name}
              </option>
            ))}
          </Select>
          <Select
            label="Lot"
            {...register('unitId')}
            errorMessage={errors.unitId?.message}
            disabled={!buildingId}
          >
            <option value="">
              {!buildingId
                ? "Sélectionnez d'abord un immeuble"
                : availableUnits.length === 0
                  ? 'Aucun lot disponible'
                  : 'Sélectionnez un lot'}
            </option>
            {availableUnits.map((unit) => (
              <option key={unit.id} value={unit.id}>
                {unit.unitNumber} — {unit.unitTypeName}
              </option>
            ))}
          </Select>
        </>
      )}

      {type !== 'PUBLIC' && (
        <Input
          label="Email du destinataire"
          type="email"
          {...register('targetEmail')}
          errorMessage={errors.targetEmail?.message}
        />
      )}

      <div className="flex gap-2">
        <Button type="submit" isLoading={isPending}>
          Créer l'invitation
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
