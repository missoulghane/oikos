import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useCreateInvitation } from '@/features/property-mngt/invitations/hooks/useCreateInvitation';
import {
  createInvitationSchema,
  type CreateInvitationFormValues,
} from '@/features/property-mngt/invitations/schemas/createInvitationSchema';
import { INVITATION_TYPES, type InvitationType } from '@/features/property-mngt/invitations/types/invitation.types';

const INVITATION_TYPE_LABELS: Record<InvitationType, string> = {
  PUBLIC: 'Lien public (QR code, plusieurs demandeurs, validation manager)',
  PRIVATE: "Invitation privée - lot au choix de l’invité",
};

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
    formState: { errors },
  } = useForm<CreateInvitationFormValues>({
    resolver: zodResolver(createInvitationSchema),
    defaultValues: { type: 'PUBLIC', targetEmail: '' },
  });
  const type = watch('type');
  const { mutate, isPending, error } = useCreateInvitation(propertyId);

  function onSubmit(values: CreateInvitationFormValues) {
    mutate(
      {
        propertyId,
        type: values.type,
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
