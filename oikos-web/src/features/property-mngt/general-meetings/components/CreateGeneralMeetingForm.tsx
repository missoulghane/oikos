import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  createGeneralMeetingSchema,
  type CreateGeneralMeetingFormValues,
} from '@/features/property-mngt/general-meetings/schemas/generalMeetingSchemas';
import {
  MEETING_TYPE_LABELS,
  VENUE_TYPE_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';

interface CreateGeneralMeetingFormProps {
  onSubmit: (values: CreateGeneralMeetingFormValues) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

/**
 * Nature, intitulé, date et lieu - everything shown at the top of the meeting's
 * tabs afterwards. The AG is still created as a draft: what is settled here is
 * what it is and when it happens, not that it is confirmed (scheduling, which
 * freezes the agenda, is a separate act).
 *
 * <p>Presentation only, like every other form of the app: the mutation and the
 * navigation belong to the page.
 *
 * <p>Two columns from `sm` up, one on a phone: the card is full-width, and
 * stretching six single-column fields across it would read worse than the
 * half-width version it replaces.
 */
export function CreateGeneralMeetingForm({
  onSubmit,
  isSubmitting,
  errorMessage,
}: CreateGeneralMeetingFormProps) {
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<CreateGeneralMeetingFormValues>({
    resolver: zodResolver(createGeneralMeetingSchema),
    defaultValues: {
      meetingType: 'ORDINARY',
      title: '',
      scheduledAt: '',
      venueType: 'PHYSICAL',
      venueAddress: '',
      venueLink: '',
    },
  });

  const venueType = watch('venueType');
  const needsAddress = venueType === 'PHYSICAL' || venueType === 'HYBRID';
  const needsLink = venueType === 'VIDEOCONFERENCE' || venueType === 'HYBRID';

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-6">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Select label="Nature" {...register('meetingType')} errorMessage={errors.meetingType?.message}>
          <option value="ORDINARY">{MEETING_TYPE_LABELS.ORDINARY}</option>
          <option value="EXTRAORDINARY">{MEETING_TYPE_LABELS.EXTRAORDINARY}</option>
        </Select>
        <Input
          label="Intitulé"
          placeholder="AG ordinaire 2026"
          {...register('title')}
          errorMessage={errors.title?.message}
        />
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
          <Input
            label="Adresse"
            placeholder="12 rue des Orangers, Casablanca"
            {...register('venueAddress')}
            errorMessage={errors.venueAddress?.message}
          />
        )}
        {needsLink && (
          <Input
            label="Lien de connexion"
            placeholder="https://…"
            {...register('venueLink')}
            errorMessage={errors.venueLink?.message}
          />
        )}
      </div>

      {errorMessage && <Alert message={errorMessage} />}

      <div>
        <Button type="submit" isLoading={isSubmitting}>
          Créer l'assemblée
        </Button>
      </div>
    </form>
  );
}
