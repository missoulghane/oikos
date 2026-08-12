import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useChangePassword } from '@/features/identity/me/hooks/useChangePassword';
import {
  changePasswordSchema,
  type ChangePasswordFormValues,
} from '@/features/identity/me/schemas/changePasswordSchema';

export function ChangePasswordForm() {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
  });
  const { mutate, isPending, isSuccess, error } = useChangePassword();

  function onSubmit(values: ChangePasswordFormValues) {
    mutate(
      { currentPassword: values.currentPassword, newPassword: values.newPassword },
      { onSuccess: () => reset() },
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {isSuccess && <Alert variant="success" message="Votre mot de passe a été modifié." />}
      {error && <Alert message={getErrorMessage(error)} />}
      <Input
        label="Mot de passe actuel"
        type="password"
        autoComplete="current-password"
        {...register('currentPassword')}
        errorMessage={errors.currentPassword?.message}
      />
      <Input
        label="Nouveau mot de passe"
        type="password"
        autoComplete="new-password"
        {...register('newPassword')}
        errorMessage={errors.newPassword?.message}
      />
      <Input
        label="Confirmer le nouveau mot de passe"
        type="password"
        autoComplete="new-password"
        {...register('confirmPassword')}
        errorMessage={errors.confirmPassword?.message}
      />
      <Button type="submit" isLoading={isPending} className="w-fit">
        Modifier le mot de passe
      </Button>
    </form>
  );
}
