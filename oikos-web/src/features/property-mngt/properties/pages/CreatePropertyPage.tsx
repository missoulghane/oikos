import { useNavigate } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { CreatePropertyForm } from '@/features/property-mngt/properties/components/CreatePropertyForm';
import { useCreateProperty } from '@/features/property-mngt/properties/hooks/useCreateProperty';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { CreatePropertyFormValues } from '@/features/property-mngt/properties/schemas/createPropertySchema';

export function CreatePropertyPage() {
  const navigate = useNavigate();
  const { mutate, isPending, error } = useCreateProperty();

  function handleSubmit(values: CreatePropertyFormValues) {
    mutate(values, { onSuccess: () => navigate('/properties', { replace: true }) });
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-lg font-semibold text-slate-900">Nouvelle copropriété</h1>
      <Card className="max-w-lg">
        <CreatePropertyForm
          onSubmit={handleSubmit}
          isSubmitting={isPending}
          errorMessage={error ? getErrorMessage(error) : undefined}
        />
      </Card>
    </div>
  );
}
