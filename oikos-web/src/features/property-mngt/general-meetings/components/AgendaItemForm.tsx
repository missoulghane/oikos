import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  agendaItemSchema,
  type AgendaItemFormValues,
} from '@/features/property-mngt/general-meetings/schemas/generalMeetingSchemas';
import { MAJORITY_RULE_LABELS } from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import type { AgendaItem, MajorityRule } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';

const MAJORITY_RULES = Object.keys(MAJORITY_RULE_LABELS) as MajorityRule[];

/**
 * The majority rule is asked for up front and has no default beyond "simple":
 * it is what decides the outcome, and each option names the voices it will be
 * measured against.
 *
 * <p>In creation mode the fields are cleared after each submit: the form stays
 * on screen to take the next point, and keeping the previous one's wording
 * there is how a second "Approbation des comptes" ends up on the agenda. In
 * edit mode nothing is cleared - the form closes on success instead.
 */
export function AgendaItemForm({
  item,
  isSubmitting,
  onSubmit,
  onCancel,
}: {
  item?: AgendaItem;
  isSubmitting: boolean;
  onSubmit: (values: AgendaItemFormValues) => void;
  onCancel?: () => void;
}) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AgendaItemFormValues>({
    resolver: zodResolver(agendaItemSchema),
    defaultValues: {
      label: item?.label ?? '',
      description: item?.description ?? '',
      majorityRule: item?.majorityRule ?? 'SIMPLE',
    },
  });

  function submit(values: AgendaItemFormValues) {
    onSubmit(values);
    if (!item) {
      // Back to the defaults rather than reset() with no argument: the latter
      // restores the *last submitted* values in react-hook-form, which is
      // exactly what we are trying to get rid of.
      reset({ label: '', description: '', majorityRule: 'SIMPLE' });
    }
  }

  return (
    <form onSubmit={handleSubmit(submit)} className="flex flex-col gap-4">
      <Input
        label="Libellé du point"
        placeholder="Approbation des comptes 2025"
        {...register('label')}
        errorMessage={errors.label?.message}
      />
      <div className="flex flex-col gap-1">
        <label htmlFor="description" className="text-sm font-medium text-gray-700 dark:text-gray-300">
          Description (facultative)
        </label>
        <textarea
          id="description"
          rows={3}
          className="rounded-lg border border-gray-300 dark:border-gray-700 bg-transparent px-3 py-2 text-base text-gray-800 dark:text-white/90 shadow-theme-xs focus:border-brand-300 focus:outline-none focus:ring-3 focus:ring-brand-500/20"
          {...register('description')}
        />
      </div>
      <Select label="Majorité requise" {...register('majorityRule')} errorMessage={errors.majorityRule?.message}>
        {MAJORITY_RULES.map((rule) => (
          <option key={rule} value={rule}>
            {MAJORITY_RULE_LABELS[rule]}
          </option>
        ))}
      </Select>
      <div className="flex gap-3">
        <Button type="submit" isLoading={isSubmitting}>
          {item ? 'Enregistrer' : 'Ajouter le point'}
        </Button>
        {onCancel && (
          <Button type="button" variant="secondary" onClick={onCancel}>
            Annuler
          </Button>
        )}
      </div>
    </form>
  );
}
