import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useCreateFinancialAccount } from '@/features/property-mngt/accounting/hooks/useCreateFinancialAccount';
import { FINANCIAL_ACCOUNT_TYPE_LABELS } from '@/features/property-mngt/accounting/constants/financialAccountTypeLabels';
import {
  createFinancialAccountSchema,
  type CreateFinancialAccountFormValues,
} from '@/features/property-mngt/accounting/schemas/createFinancialAccountSchema';

interface CreateFinancialAccountFormProps {
  propertyId: string;
}

export function CreateFinancialAccountForm({ propertyId }: CreateFinancialAccountFormProps) {
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreateFinancialAccountFormValues>({
    resolver: zodResolver(createFinancialAccountSchema),
    defaultValues: { currency: 'MAD' },
  });
  const { mutate, isPending, error } = useCreateFinancialAccount(propertyId);

  function onSubmit(values: CreateFinancialAccountFormValues) {
    mutate(values, { onSuccess: () => navigate(`/property-mngt/properties/${propertyId}/accounting/financial-accounts`) });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:flex-wrap">
        <Input label="Nom" {...register('name')} errorMessage={errors.name?.message} />
        <Select label="Type" {...register('type')} errorMessage={errors.type?.message}>
          <option value="">Sélectionner…</option>
          {Object.entries(FINANCIAL_ACCOUNT_TYPE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </Select>
        <Input label="Devise" {...register('currency')} errorMessage={errors.currency?.message} />
        <Button type="submit" isLoading={isPending}>
          Créer le compte
        </Button>
      </div>
    </form>
  );
}
