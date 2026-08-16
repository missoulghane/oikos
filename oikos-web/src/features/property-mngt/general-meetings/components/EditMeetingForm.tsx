import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  createGeneralMeetingSchema,
  type CreateGeneralMeetingFormValues,
} from '@/features/property-mngt/general-meetings/schemas/generalMeetingSchemas';
import { useUpdateGeneralMeeting } from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import {
  MEETING_TYPE_LABELS,
  VENUE_TYPE_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { toDateTimeLocalValue } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import type { GeneralMeeting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

/**
 * Correcting the meeting's own fields, in place in the header, at any point of
 * its lifecycle - a wrong address or a postponed hour is a correction, not a
 * new assembly (ADR 0002 §8).
 *
 * <p>Reuses the creation schema: the fields and the venue rule are the same,
 * and having a second schema drift from the first is how two screens end up
 * disagreeing about what a valid meeting is.
 */
export function EditMeetingForm({ meeting, onDone }: { meeting: GeneralMeeting; onDone: () => void }) {
  const update = useUpdateGeneralMeeting(meeting.id);
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<CreateGeneralMeetingFormValues>({
    resolver: zodResolver(createGeneralMeetingSchema),
    defaultValues: {
      meetingType: meeting.meetingType,
      title: meeting.title,
      scheduledAt: toDateTimeLocalValue(meeting.scheduledAt),
      venueType: meeting.venueType ?? 'PHYSICAL',
      venueAddress: meeting.venueAddress ?? '',
      venueLink: meeting.venueLink ?? '',
    },
  });

  const venueType = watch('venueType');
  const needsAddress = venueType === 'PHYSICAL' || venueType === 'HYBRID';
  const needsLink = venueType === 'VIDEOCONFERENCE' || venueType === 'HYBRID';

  function onSubmit(values: CreateGeneralMeetingFormValues) {
    update.mutate(
      {
        meetingType: values.meetingType,
        title: values.title,
        scheduledAt: new Date(values.scheduledAt).toISOString(),
        venueType: values.venueType,
        venueAddress: needsAddress ? (values.venueAddress ?? null) : null,
        venueLink: needsLink ? (values.venueLink ?? null) : null,
      },
      { onSuccess: onDone },
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Select label="Nature" {...register('meetingType')} errorMessage={errors.meetingType?.message}>
          <option value="ORDINARY">{MEETING_TYPE_LABELS.ORDINARY}</option>
          <option value="EXTRAORDINARY">{MEETING_TYPE_LABELS.EXTRAORDINARY}</option>
        </Select>
        <Input label="Intitulé" {...register('title')} errorMessage={errors.title?.message} />
        <Input
          label="Date et heure"
          type="datetime-local"
          {...register('scheduledAt')}
          errorMessage={errors.scheduledAt?.message}
        />
        <Select label="Format" {...register('venueType')} errorMessage={errors.venueType?.message}>
          <option value="PHYSICAL">{VENUE_TYPE_LABELS.PHYSICAL}</option>
          <option value="VIDEOCONFERENCE">{VENUE_TYPE_LABELS.VIDEOCONFERENCE}</option>
          <option value="HYBRID">{VENUE_TYPE_LABELS.HYBRID}</option>
        </Select>
        {needsAddress && (
          <Input label="Adresse" {...register('venueAddress')} errorMessage={errors.venueAddress?.message} />
        )}
        {needsLink && (
          <Input label="Lien de connexion" {...register('venueLink')} errorMessage={errors.venueLink?.message} />
        )}
      </div>
      {update.isError && <Alert message={getErrorMessage(update.error)} />}
      <div className="flex gap-3">
        <Button type="submit" isLoading={update.isPending}>
          Enregistrer
        </Button>
        <Button type="button" variant="secondary" onClick={onDone}>
          Annuler
        </Button>
      </div>
    </form>
  );
}
