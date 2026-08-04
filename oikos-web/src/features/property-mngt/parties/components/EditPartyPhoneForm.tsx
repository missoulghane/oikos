import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useUpdatePartyPhone } from '@/features/property-mngt/parties/hooks/useUpdatePartyPhone';
import {
  updatePartyPhoneSchema,
  type UpdatePartyPhoneFormValues,
} from '@/features/property-mngt/parties/schemas/updatePartyPhoneSchema';

interface EditPartyPhoneFormProps {
  partyId: string;
  currentPhone: string | null;
  onSuccess: () => void;
  onCancel: () => void;
}

export function EditPartyPhoneForm({ partyId, currentPhone, onSuccess, onCancel }: EditPartyPhoneFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdatePartyPhoneFormValues>({
    resolver: zodResolver(updatePartyPhoneSchema),
    defaultValues: { phone: currentPhone ?? '' },
  });
  const { mutate, isPending, error } = useUpdatePartyPhone(partyId);

  function onSubmit(values: UpdatePartyPhoneFormValues) {
    mutate(values.phone, { onSuccess });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <Input label="Téléphone" type="tel" {...register('phone')} errorMessage={errors.phone?.message} />
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
