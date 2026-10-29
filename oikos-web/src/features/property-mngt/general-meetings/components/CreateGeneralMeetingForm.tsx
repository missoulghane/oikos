import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  createGeneralMeetingSchema,
  type CreateGeneralMeetingFormValues,
} from '@/features/property-mngt/general-meetings/schemas/generalMeetingSchemas';
import {
  MEETING_TYPE_LABELS,
  VENUE_TYPE_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { PendingDocumentsPicker } from '@/features/property-mngt/documents';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Input } from '@/shared/components/Input/Input';
import { Select } from '@/shared/components/Select/Select';
import { RequiredFieldsHint } from '@/shared/components/RequiredFieldsHint/RequiredFieldsHint';
import { QuillEditor } from '@/shared/components/RichText/QuillEditor';

interface CreateGeneralMeetingFormProps {
  onSubmit: (values: CreateGeneralMeetingFormValues, documents: File[]) => void;
  isSubmitting: boolean;
  errorMessage?: string;
}

/**
 * Nature, intitulé, date et lieu - everything shown at the top of the meeting's
 * tabs afterwards - then, optionally, the comment and the documents that the
 * Informations tab holds. The AG is still created as a draft: what is settled
 * here is what it is and when it happens, not that it is confirmed
 * (scheduling, which freezes the agenda, is a separate act).
 *
 * <p>Presentation only, like every other form of the app: the mutation, the
 * uploads and the navigation belong to the page. The documents travel beside
 * the values rather than in them - files are not form data the schema can
 * validate, and they go to another endpoint once the meeting has an id.
 *
 * <p>Two columns from `sm` up, one on a phone. The address and the link span
 * both: they are the long values of the form, and a half-width field cut a
 * street address off mid-word.
 */
export function CreateGeneralMeetingForm({
  onSubmit,
  isSubmitting,
  errorMessage,
}: CreateGeneralMeetingFormProps) {
  const [documents, setDocuments] = useState<File[]>([]);
  const {
    register,
    control,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<CreateGeneralMeetingFormValues>({
    // Erreurs signalées à la sortie du champ, puis à chaque frappe - comme sur
    // tous les formulaires - plutôt que seulement à la validation.
    mode: 'onTouched',
    resolver: zodResolver(createGeneralMeetingSchema),
    defaultValues: {
      meetingType: 'ORDINARY',
      title: '',
      scheduledAt: '',
      venueType: 'PHYSICAL',
      venueAddress: '',
      venueLink: '',
      comment: '',
    },
  });

  const venueType = watch('venueType');
  const needsAddress = venueType === 'PHYSICAL' || venueType === 'HYBRID';
  const needsLink = venueType === 'VIDEOCONFERENCE' || venueType === 'HYBRID';

  return (
    <form
      onSubmit={handleSubmit((values) => onSubmit(values, documents))}
      className="flex flex-col gap-8"
      noValidate
    >
      <section className="flex flex-col gap-4">
        <RequiredFieldsHint />
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Select label="Nature" {...register('meetingType')} errorMessage={errors.meetingType?.message}>
            <option value="ORDINARY">{MEETING_TYPE_LABELS.ORDINARY}</option>
            <option value="EXTRAORDINARY">{MEETING_TYPE_LABELS.EXTRAORDINARY}</option>
          </Select>
          <Input
            label="Intitulé"
            required
            placeholder="AG ordinaire 2026"
            {...register('title')}
            errorMessage={errors.title?.message}
          />
          <Input
            label="Date et heure"
            type="datetime-local"
            required
            {...register('scheduledAt')}
            errorMessage={errors.scheduledAt?.message}
          />
          <Select label="Format" {...register('venueType')} errorMessage={errors.venueType?.message}>
            <option value="PHYSICAL">{VENUE_TYPE_LABELS.PHYSICAL}</option>
            <option value="VIDEOCONFERENCE">{VENUE_TYPE_LABELS.VIDEOCONFERENCE}</option>
            <option value="HYBRID">{VENUE_TYPE_LABELS.HYBRID}</option>
          </Select>
          {needsAddress && (
            <div className="sm:col-span-2">
              <Input
                label="Adresse"
                required
                placeholder="12 rue des Orangers, Casablanca"
                {...register('venueAddress')}
                errorMessage={errors.venueAddress?.message}
              />
            </div>
          )}
          {needsLink && (
            <div className="sm:col-span-2">
              <Input
                label="Lien de connexion"
                required
                placeholder="https://…"
                {...register('venueLink')}
                errorMessage={errors.venueLink?.message}
              />
            </div>
          )}
        </div>
      </section>

      {/* Same heading and warning as the Informations tab, where both are edited later:
          a syndic writing a private reminder here would be writing it to the whole copropriété. */}
      <section className="flex flex-col gap-3 border-t border-gray-200 pt-6 dark:border-gray-800">
        <div>
          <h2 className="font-medium text-gray-900 dark:text-white/90">Commentaire</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Facultatif. <strong>Lu par les copropriétaires</strong> : il apparaît dans leur espace, sur
            mobile, et dans la convocation qu'ils reçoivent. Ce n'est pas une note interne.
          </p>
        </div>
        <Controller
          name="comment"
          control={control}
          render={({ field }) => (
            <QuillEditor
              defaultValue={field.value}
              onChange={field.onChange}
              onBlur={field.onBlur}
              disabled={isSubmitting}
              minHeight={160}
              ariaLabel="Commentaire de l'assemblée générale"
              placeholder="Contexte du vote, note d'intention, précisions à porter à la connaissance des copropriétaires…"
            />
          )}
        />
        {errors.comment?.message && <Alert message={errors.comment.message} />}
      </section>

      <section className="flex flex-col gap-3 border-t border-gray-200 pt-6 dark:border-gray-800">
        <div>
          <h2 className="font-medium text-gray-900 dark:text-white/90">Documents de l'assemblée</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Facultatif. Les pièces qui éclairent l'assemblée entière — budget, rapport de gestion, devis.
            Elles sont envoyées à la création ; celles qui portent sur un point précis se joignent à ce point,
            dans l'onglet « Ordre du jour ».
          </p>
        </div>
        <PendingDocumentsPicker
          files={documents}
          onChange={setDocuments}
          disabled={isSubmitting}
          ariaLabel="Documents de l'assemblée générale"
        />
      </section>

      {errorMessage && <Alert message={errorMessage} />}

      <div>
        <Button type="submit" isLoading={isSubmitting}>
          Créer l'assemblée
        </Button>
      </div>
    </form>
  );
}
