import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Select } from '@/shared/components/Select/Select';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useAddBoardMember } from '@/features/property-mngt/board-members/hooks/useAddBoardMember';
import {
  addBoardMemberSchema,
  type AddBoardMemberFormValues,
} from '@/features/property-mngt/board-members/schemas/addBoardMemberSchema';
import { BOARD_ROLES, BOARD_ROLE_LABELS } from '@/features/property-mngt/board-members/types/boardMember.types';

interface AddBoardMemberFormProps {
  propertyId: string;
  onSuccess: () => void;
  onCancel: () => void;
}

export function AddBoardMemberForm({ propertyId, onSuccess, onCancel }: AddBoardMemberFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AddBoardMemberFormValues>({
    resolver: zodResolver(addBoardMemberSchema),
    defaultValues: { fullName: '', email: '', phone: '', boardRole: 'MEMBER' },
  });
  const { mutate, isPending, error } = useAddBoardMember(propertyId);

  function onSubmit(values: AddBoardMemberFormValues) {
    mutate(
      {
        propertyId,
        fullName: values.fullName,
        email: values.email || undefined,
        phone: values.phone || undefined,
        boardRole: values.boardRole,
      },
      { onSuccess },
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Input label="Nom complet" {...register('fullName')} errorMessage={errors.fullName?.message} />
      <Input label="Email (optionnel)" type="email" {...register('email')} errorMessage={errors.email?.message} />
      <Input label="Téléphone (optionnel)" {...register('phone')} errorMessage={errors.phone?.message} />
      <Select label="Rôle" {...register('boardRole')} errorMessage={errors.boardRole?.message}>
        {BOARD_ROLES.map((role) => (
          <option key={role} value={role}>
            {BOARD_ROLE_LABELS[role]}
          </option>
        ))}
      </Select>

      <div className="flex gap-2">
        <Button type="submit" isLoading={isPending}>
          Ajouter
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
