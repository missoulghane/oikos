import { Link } from 'react-router-dom';
import { useMyConvocations } from '@/features/property-ownership/general-meetings/hooks/useMyConvocations';
import { useReplyToMyConvocation } from '@/features/property-ownership/general-meetings/hooks/useReplyToMyConvocation';
import {
  ATTENDANCE_REPLY_LABELS,
  MEETING_STATUS_COLORS,
  MEETING_STATUS_LABELS,
  MEETING_TYPE_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { formatLotLabel, formatScheduledAt, formatVenue } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

/**
 * "Assemblées générales" (owner space): one row per lot owned and per meeting
 * it is convoked to. A copropriétaire answers per lot, because it is the lot
 * that carries the voice - two lots in the same AG mean two answers.
 */
export function MyGeneralMeetingsPage() {
  const convocations = useMyConvocations();
  const reply = useReplyToMyConvocation();

  if (convocations.isLoading) {
    return <Loader label="Chargement de mes assemblées…" />;
  }

  if (convocations.isError) {
    return <Alert message={getErrorMessage(convocations.error)} />;
  }

  const rows = convocations.data ?? [];

  return (
    // Same shell as every other list of the app (see GeneralMeetingsListTab):
    // one Card holding the heading and the rows, rather than a bare heading
    // over free-floating cards.
    <Card className="flex flex-col gap-4">
      <div>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white/90">Assemblées générales</h1>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Indiquez votre présence pour chacun de vos lots, et consultez les procès-verbaux publiés.
        </p>
      </div>

      {reply.isError && <Alert message={getErrorMessage(reply.error)} />}

      {rows.length === 0 && (
        <EmptyState title="Aucune convocation">
          Vous serez convoqué(e) ici dès qu'une assemblée générale sera lancée sur l'une de vos copropriétés.
        </EmptyState>
      )}

      <ul className="flex flex-col gap-3">
        {rows.map((convocation) => (
          <li key={convocation.id}>
            <div className="flex flex-col gap-4 rounded-xl border border-gray-200 dark:border-gray-800 p-4">
              <div className="flex flex-col gap-1">
                <div className="flex flex-wrap items-center gap-2">
                  <Link
                    to={`/property-ownership/general-meetings/${convocation.generalMeetingId}`}
                    className="font-medium text-gray-900 dark:text-white/90 hover:underline"
                  >
                    {convocation.meetingTitle}
                  </Link>
                  <Badge color={MEETING_STATUS_COLORS[convocation.meetingStatus]}>
                    {MEETING_STATUS_LABELS[convocation.meetingStatus]}
                  </Badge>
                </div>
                <span className="text-sm text-gray-500 dark:text-gray-400">
                  {convocation.propertyName} · AG {MEETING_TYPE_LABELS[convocation.meetingType].toLowerCase()}
                </span>
                <span className="text-sm text-gray-500 dark:text-gray-400">
                  {formatScheduledAt(convocation.scheduledAt)} — {formatVenue(convocation)}
                </span>
                <span className="text-sm text-gray-400 dark:text-gray-500">
                  Lot concerné : {formatLotLabel(convocation.unitNumber, convocation.buildingName)}
                </span>
              </div>

              <div className="flex flex-wrap items-center gap-3">
                <span className="text-sm text-gray-500 dark:text-gray-400">
                  Votre réponse : {ATTENDANCE_REPLY_LABELS[convocation.attendanceReply]}
                  {convocation.checkedIn && ' · émargé en séance'}
                </span>
                {/* Answering stays possible until the session opens - people change their mind,
                    and the last answer before the meeting is the one that counts. */}
                {(convocation.meetingStatus === 'CONVENED' || convocation.meetingStatus === 'SCHEDULED') && (
                  <div className="flex gap-2">
                    <Button
                      variant={convocation.attendanceReply === 'ATTENDING' ? 'primary' : 'secondary'}
                      isLoading={reply.isPending}
                      onClick={() => reply.mutate({ convocationId: convocation.id, reply: 'ATTENDING' })}
                    >
                      Je serai présent(e)
                    </Button>
                    <Button
                      variant={convocation.attendanceReply === 'NOT_ATTENDING' ? 'primary' : 'secondary'}
                      isLoading={reply.isPending}
                      onClick={() => reply.mutate({ convocationId: convocation.id, reply: 'NOT_ATTENDING' })}
                    >
                      Je serai absent(e)
                    </Button>
                  </div>
                )}
              </div>
            </div>
          </li>
        ))}
      </ul>
    </Card>
  );
}
