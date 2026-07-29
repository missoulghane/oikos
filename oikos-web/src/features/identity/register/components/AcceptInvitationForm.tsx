import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import {
  acceptInvitationSchema,
  type AcceptInvitationFormValues,
} from '@/features/identity/register/schemas/acceptInvitationSchema';

interface AcceptInvitationFormProps {
  onSubmit: (values: AcceptInvitationFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

export function AcceptInvitationForm({ onSubmit, isSubmitting, errorMessage }: AcceptInvitationFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AcceptInvitationFormValues>({
    resolver: zodResolver(acceptInvitationSchema),
    defaultValues: { password: '' },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {errorMessage && <Alert message={errorMessage} />}
      <p className="text-sm text-slate-500">
        Si vous avez déjà un compte Oikos, laissez ce champ vide. Sinon, choisissez un mot de passe pour créer votre
        compte.
      </p>
      <Input
        label="Mot de passe (nouveau compte uniquement)"
        type="password"
        autoComplete="new-password"
        {...register('password')}
        errorMessage={errors.password?.message}
      />
      <Button type="submit" isLoading={isSubmitting} className="mt-2">
        Accepter l'invitation
      </Button>
    </form>
  );
}
