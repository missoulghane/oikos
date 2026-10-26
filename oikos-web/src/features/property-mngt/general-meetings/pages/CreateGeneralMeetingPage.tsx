import { Link, useNavigate, useOutletContext } from 'react-router-dom';
import type { Property } from '@/features/property-mngt/properties/types/property.types';
import { CreateGeneralMeetingForm } from '@/features/property-mngt/general-meetings/components/CreateGeneralMeetingForm';
import { useCreateGeneralMeeting } from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import type { MeetingInformationLocationState } from '@/features/property-mngt/general-meetings/pages/MeetingInformationTab';
import { useUploadDocuments } from '@/features/property-mngt/documents';
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
  const uploadDocuments = useUploadDocuments();

  async function handleSubmit(values: CreateGeneralMeetingFormValues, documents: File[]) {
    const payload = {
      meetingType: values.meetingType,
      title: values.title,
      // <input type="datetime-local"> yields a local wall time with no zone; the API takes
      // an instant, so it is resolved against the browser's zone rather than sent ambiguous.
      scheduledAt: new Date(values.scheduledAt).toISOString(),
      venueType: values.venueType,
      venueAddress: values.venueType === 'VIDEOCONFERENCE' ? null : (values.venueAddress ?? null),
      venueLink: values.venueType === 'PHYSICAL' ? null : (values.venueLink ?? null),
      comment: values.comment ? values.comment : null,
    };
    let created: { id: string };
    try {
      created = await createMeeting.mutateAsync(payload);
    } catch {
      // Shown by the form through createMeeting.error; nothing was created, nothing to upload.
      return;
    }
    // The meeting exists from here on, whatever happens to its files: a refused one is
    // reported on the Informations tab - where the documents live and where it can be
    // sent again - rather than holding the syndic on a creation page that has done its job.
    const failedUploads =
      documents.length > 0
        ? await uploadDocuments.mutateAsync({ ownerType: 'GENERAL_MEETING', ownerId: created.id, files: documents })
        : [];
    const state: MeetingInformationLocationState = { failedUploads };
    // Straight into the new draft, on its Informations tab: the comment and the documents
    // just given are there, and the agenda is one tab away.
    navigate(`/property-mngt/properties/${property.id}/general-meetings/${created.id}`, { replace: true, state });
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
          modifiables tant qu'elle n'est pas planifiée. Le commentaire et les documents peuvent aussi être ajoutés
          plus tard, depuis l'onglet « Informations ».
        </p>
      </div>
      {/* Full width: the form lays its fields out on two columns and holds a rich-text
          editor, and a half-width card left the right half of the page empty. */}
      <Card>
        <CreateGeneralMeetingForm
          onSubmit={handleSubmit}
          isSubmitting={createMeeting.isPending || uploadDocuments.isPending}
          errorMessage={createMeeting.error ? getErrorMessage(createMeeting.error) : undefined}
        />
      </Card>
    </div>
  );
}
