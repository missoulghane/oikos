import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import type { CurrentUser } from '@/features/identity/me/types/me.types';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useUpdateProfile } from '@/features/identity/me/hooks/useUpdateProfile';
import {
  updateProfileSchema,
  type UpdateProfileFormValues,
} from '@/features/identity/me/schemas/updateProfileSchema';

export function EditProfileForm({ user }: { user: CurrentUser }) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdateProfileFormValues>({
    resolver: zodResolver(updateProfileSchema),
    defaultValues: { fullName: user.fullName, email: user.email, phone: user.phone ?? '' },
  });
  const { mutate, isPending, isSuccess, error } = useUpdateProfile();

  function onSubmit(values: UpdateProfileFormValues) {
    mutate(values);
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {isSuccess && <Alert variant="success" message="Vos informations ont été mises à jour." />}
      {error && <Alert message={getErrorMessage(error)} />}
      <Input label="Nom complet" {...register('fullName')} errorMessage={errors.fullName?.message} />
      <Input label="Email" type="email" {...register('email')} errorMessage={errors.email?.message} />
      <Input label="Téléphone" type="tel" {...register('phone')} errorMessage={errors.phone?.message} />
      <Button type="submit" isLoading={isPending} className="w-fit">
        Enregistrer
      </Button>
    </form>
  );
}
