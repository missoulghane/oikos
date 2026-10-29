import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useCreateBoardInvitation } from '@/features/property-mngt/board-members/hooks/useCreateBoardInvitation';
import {
  createBoardInvitationSchema,
  type CreateBoardInvitationFormValues,
} from '@/features/property-mngt/board-members/schemas/createBoardInvitationSchema';
import type { BoardRole } from '@/features/property-mngt/board-members/types/boardMember.types';
import { useBoardRoles } from '@/shared/hooks/useBoardRoles';

interface CreateBoardInvitationFormProps {
  propertyId: string;
  onSuccess: () => void;
  onCancel: () => void;
  defaultTargetEmail?: string;
  defaultBoardRole?: BoardRole;
}

export function CreateBoardInvitationForm({
  propertyId,
  onSuccess,
  onCancel,
  defaultTargetEmail,
  defaultBoardRole,
}: CreateBoardInvitationFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreateBoardInvitationFormValues>({
    resolver: zodResolver(createBoardInvitationSchema),
    defaultValues: { targetEmail: defaultTargetEmail ?? '', boardRole: defaultBoardRole ?? 'MEMBER' },
  });
  const { mutate, isPending, error } = useCreateBoardInvitation(propertyId);
  const boardRoles = useBoardRoles();

  function onSubmit(values: CreateBoardInvitationFormValues) {
    mutate(
      { propertyId, targetEmail: values.targetEmail, boardRole: values.boardRole },
      { onSuccess },
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Input
        label="Email du destinataire"
        type="email"
        {...register('targetEmail')}
        errorMessage={errors.targetEmail?.message}
      />
      {/* Monté une fois le catalogue chargé : enregistré plus tôt, le select
          afficherait sa première option sans que la valeur du formulaire suive. */}
      {boardRoles.data ? (
        <Select label="Rôle proposé" {...register('boardRole')} errorMessage={errors.boardRole?.message}>
          {boardRoles.data.map((role) => (
            <option key={role.code} value={role.code}>
              {role.label}
            </option>
          ))}
        </Select>
      ) : (
        <Loader label="Chargement des rôles…" />
      )}

      <div className="flex gap-2">
        <Button type="submit" isLoading={isPending}>
          Envoyer l'invitation
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
