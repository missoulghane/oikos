import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Input } from '@/shared/components/Input/Input';
import { Button } from '@/shared/components/Button/Button';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useClosePeriod } from '@/features/property-mngt/accounting/hooks/useClosePeriod';
import { useReopenPeriod } from '@/features/property-mngt/accounting/hooks/useReopenPeriod';
import {
  closePeriodSchema,
  type ClosePeriodFormValues,
} from '@/features/property-mngt/accounting/schemas/closePeriodSchema';

interface ClosePeriodFormProps {
  propertyId: string;
  /**
   * Rouvrir défait ce que la clôture a arrêté : le bouton n'apparaît qu'à qui
   * administre la copropriété, comme la garde côté API.
   */
  canReopen: boolean;
}

/**
 * Clôturer un mois, et le rouvrir. Les deux gestes partagent le même champ :
 * c'est la même question - de quel mois parle-t-on ? - et deux formulaires
 * l'auraient posée deux fois.
 */
export function ClosePeriodForm({ propertyId, canReopen }: ClosePeriodFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ClosePeriodFormValues>({ resolver: zodResolver(closePeriodSchema) });
  const close = useClosePeriod(propertyId);
  const reopen = useReopenPeriod(propertyId);

  function onClose(values: ClosePeriodFormValues) {
    reopen.reset();
    close.mutate(values.period, { onSuccess: () => reset() });
  }

  function onReopen(values: ClosePeriodFormValues) {
    close.reset();
    reopen.mutate(values.period, { onSuccess: () => reset() });
  }

  const error = close.error ?? reopen.error;

  return (
    <form onSubmit={handleSubmit(onClose)} className="flex flex-col gap-3" noValidate>
      {error && <Alert message={getErrorMessage(error)} />}
      {close.isSuccess && close.data && (
        <Alert variant="success" message={`Période ${close.data.yearMonth} clôturée.`} />
      )}
      {reopen.isSuccess && reopen.data && (
        <Alert variant="success" message={`Période ${reopen.data.yearMonth} rouverte : la saisie y est de nouveau possible.`} />
      )}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
        <Input label="Période" type="month" {...register('period')} errorMessage={errors.period?.message} />
        <Button type="submit" variant="secondary" isLoading={close.isPending}>
          Clôturer la période
        </Button>
        {canReopen && (
          <Button type="button" variant="secondary" isLoading={reopen.isPending} onClick={handleSubmit(onReopen)}>
            Rouvrir
          </Button>
        )}
      </div>
    </form>
  );
}
