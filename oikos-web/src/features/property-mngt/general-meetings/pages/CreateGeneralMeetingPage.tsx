import { Link, useNavigate, useOutletContext } from 'react-router-dom';
import type { Property } from '@/features/property-mngt/properties/types/property.types';
import { CreateGeneralMeetingForm } from '@/features/property-mngt/general-meetings/components/CreateGeneralMeetingForm';
import { useCreateGeneralMeeting } from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import { Card } from '@/shared/components/Card/Card';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import type { CreateGeneralMeetingFormValues } from '@/features/property-mngt/general-meetings/schemas/generalMeetingSchemas';

/**
 * A page, not an overlay: every creation flow in the app is its own route
 * (CreatePropertyPage, CreateBankAccountPage, RecordOwnerPaymentPage), which
 * is what keeps a form usable on a phone - a dialog over a small screen has
 * nowhere to go.
 */
export function CreateGeneralMeetingPage() {
  const { property } = useOutletContext<{ property: Property }>();
  const navigate = useNavigate();
  const createMeeting = useCreateGeneralMeeting(property.id);

  function handleSubmit(values: CreateGeneralMeetingFormValues) {
    const payload = {
      meetingType: values.meetingType,
      title: values.title,
      // <input type="datetime-local"> yields a local wall time with no zone; the API takes
      // an instant, so it is resolved against the browser's zone rather than sent ambiguous.
      scheduledAt: new Date(values.scheduledAt).toISOString(),
      venueType: values.venueType,
      venueAddress: values.venueType === 'VIDEOCONFERENCE' ? null : (values.venueAddress ?? null),
      venueLink: values.venueType === 'PHYSICAL' ? null : (values.venueLink ?? null),
    };
    createMeeting.mutate(payload, {
      // Straight into the new draft: the next thing to do is write its agenda,
      // and going back to the list would only mean finding it again.
      onSuccess: (created) =>
        navigate(`/property-mngt/properties/${property.id}/general-meetings/${created.id}`, { replace: true }),
    });
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to={`/property-mngt/properties/${property.id}/general-meetings`}
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          ← Retour aux assemblées
        </Link>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Nouvelle assemblée générale</h1>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          L'assemblée est créée en brouillon : ces informations s'affichent en tête de ses onglets et restent
          modifiables tant qu'elle n'est pas planifiée.
        </p>
      </div>
      {/* Full width: the form has six fields laid out on two columns, and a half-width card
          left the right half of the page empty. */}
      <Card>
        <CreateGeneralMeetingForm
          onSubmit={handleSubmit}
          isSubmitting={createMeeting.isPending}
          errorMessage={createMeeting.error ? getErrorMessage(createMeeting.error) : undefined}
        />
      </Card>
    </div>
  );
}
