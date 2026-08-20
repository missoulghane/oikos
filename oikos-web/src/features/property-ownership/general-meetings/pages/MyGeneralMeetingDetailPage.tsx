import { Link, useParams } from 'react-router-dom';
import { useGeneralMeeting } from '@/features/property-mngt/general-meetings/hooks/useGeneralMeeting';
import { useAgendaItems } from '@/features/property-mngt/general-meetings/hooks/useAgendaItems';
import { isNotFound, useMeetingMinutes } from '@/features/property-mngt/general-meetings/hooks/useMeetingMinutes';
import {
  MAJORITY_RULE_LABELS,
  MEETING_STATUS_COLORS,
  MEETING_STATUS_LABELS,
  MEETING_TYPE_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { formatScheduledAt, formatVenue } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { AttachmentsPanel } from '@/features/property-mngt/documents';
import { AgendaItemResultSummary } from '@/features/property-ownership/general-meetings/components/AgendaItemResultSummary';
import { RichTextContent } from '@/shared/components/RichText/RichTextContent';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

/**
 * A copropriétaire's read-only view of an AG they are convoked to: what will
 * be decided, then - once the ballots are closed - what was.
 *
 * <p>Reads go through the same endpoints as the back-office: the API gates
 * them on meeting:read, which every owner holds. Nothing here is a duplicated
 * owner-specific projection.
 */
export function MyGeneralMeetingDetailPage() {
  const { meetingId } = useParams<{ meetingId: string }>();
  const meeting = useGeneralMeeting(meetingId ?? '');
  const agenda = useAgendaItems(meetingId ?? '');
  const minutes = useMeetingMinutes(meetingId ?? '');

  if (meeting.isLoading) {
    return <Loader label="Chargement de l'assemblée…" />;
  }

  if (meeting.isError) {
    return <Alert message={getErrorMessage(meeting.error)} />;
  }

  if (!meeting.data) {
    return null;
  }

  // A draft or validated PV is internal to the syndic until it is published.
  const publishedMinutes = minutes.data?.status === 'PUBLISHED' ? minutes.data : null;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link
          to="/property-ownership/general-meetings"
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          ← Retour aux assemblées générales
        </Link>
        <div className="flex flex-wrap items-center gap-2">
          <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">{meeting.data.title}</h1>
          <Badge color={MEETING_STATUS_COLORS[meeting.data.status]}>
            {MEETING_STATUS_LABELS[meeting.data.status]}
          </Badge>
        </div>
      </div>

      <Card className="flex flex-col gap-3">
        <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Copropriété</dt>
            <dd className="text-gray-900 dark:text-white/90">{meeting.data.propertyName}</dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Nature</dt>
            <dd className="text-gray-900 dark:text-white/90">{MEETING_TYPE_LABELS[meeting.data.meetingType]}</dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Date et heure</dt>
            <dd className="text-gray-900 dark:text-white/90">{formatScheduledAt(meeting.data.scheduledAt)}</dd>
          </div>
          <div>
            <dt className="text-sm text-gray-500 dark:text-gray-400">Lieu</dt>
            <dd className="text-gray-900 dark:text-white/90">{formatVenue(meeting.data)}</dd>
          </div>
        </dl>
      </Card>

      {meeting.data.comment && (
        <Card className="flex flex-col gap-2">
          <h2 className="font-medium text-gray-900 dark:text-white/90">Note du syndic</h2>
          {/* Written by a person, so sanitized before it touches the DOM - the API stores
              the editor's HTML as-is and validates none of it. */}
          <RichTextContent html={meeting.data.comment} />
        </Card>
      )}

      <Card className="flex flex-col gap-3">
        <h2 className="font-medium text-gray-900 dark:text-white/90">Documents de l'assemblée</h2>
        <AttachmentsPanel
          ownerType="GENERAL_MEETING"
          ownerId={meeting.data.id}
          canWrite={false}
          emptyLabel="Aucun document joint à cette assemblée."
        />
      </Card>

      <Card className="flex flex-col gap-4">
        <h2 className="font-medium text-gray-900 dark:text-white/90">Ordre du jour</h2>
        {agenda.isLoading && <Loader label="Chargement de l'ordre du jour…" />}
        {agenda.isError && <Alert message={getErrorMessage(agenda.error)} />}
        <ol className="flex flex-col gap-3">
          {(agenda.data ?? []).map((item, index) => (
            <li key={item.id} className="rounded-xl border border-gray-200 dark:border-gray-800 p-4">
              <div className="flex flex-col gap-1">
                <span className="font-medium text-gray-900 dark:text-white/90">
                  {index + 1}. {item.label}
                </span>
                {item.description && (
                  <p className="text-sm text-gray-500 dark:text-gray-400">{item.description}</p>
                )}
                <span className="text-xs text-gray-400 dark:text-gray-500">
                  {MAJORITY_RULE_LABELS[item.majorityRule]}
                </span>
              </div>
              {/* Silent when the point carries nothing - fifteen "aucune pièce jointe" would
                  bury the two points that do have one. */}
              <AttachmentsPanel ownerType="AGENDA_ITEM" ownerId={item.id} canWrite={false} hideWhenEmpty />
              {item.voteSessionStatus === 'CLOSED' && <AgendaItemResultSummary agendaItemId={item.id} />}
            </li>
          ))}
        </ol>
      </Card>

      <Card className="flex flex-col gap-3">
        <h2 className="font-medium text-gray-900 dark:text-white/90">Procès-verbal</h2>
        {minutes.isError && !isNotFound(minutes.error) && <Alert message={getErrorMessage(minutes.error)} />}
        {publishedMinutes ? (
          // The minutes are API-composed, but the syndic edits the draft by hand before
          // publication - so the string is user input whatever produced it first.
          <RichTextContent html={publishedMinutes.content} variant="document" />
        ) : (
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Le procès-verbal sera consultable ici dès sa publication par le syndic.
          </p>
        )}
      </Card>
    </div>
  );
}
