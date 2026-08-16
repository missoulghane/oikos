import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import type { GeneralMeetingContext } from '@/features/property-mngt/general-meetings/pages/GeneralMeetingDetailLayout';
import { useAgendaItems } from '@/features/property-mngt/general-meetings/hooks/useAgendaItems';
import { useAttendanceSummary } from '@/features/property-mngt/general-meetings/hooks/useAttendanceSummary';
import { useConvocations } from '@/features/property-mngt/general-meetings/hooks/useConvocations';
import {
  useCheckInConvocation,
  useCloseGeneralMeeting,
  useOpenGeneralMeeting,
  useUndoCheckIn,
} from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import { AttendanceSummaryCard } from '@/features/property-mngt/general-meetings/components/AttendanceSummaryCard';
import { BallotPanel } from '@/features/property-mngt/general-meetings/components/BallotPanel';
import { ATTENDANCE_MODE_LABELS } from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { formatLotLabel, formatWeight } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export function SessionTab() {
  const { meeting } = useOutletContext<GeneralMeetingContext>();
  const convocations = useConvocations(meeting.id);
  const summary = useAttendanceSummary(meeting.id);
  const agenda = useAgendaItems(meeting.id);
  const checkIn = useCheckInConvocation(meeting.id);
  const undo = useUndoCheckIn(meeting.id);
  const openMeeting = useOpenGeneralMeeting(meeting.id);
  const closeMeeting = useCloseGeneralMeeting(meeting.id);
  const [confirmForce, setConfirmForce] = useState(false);

  const rows = convocations.data ?? [];
  const canCheckIn = meeting.status === 'CONVENED' || meeting.status === 'IN_PROGRESS';
  const quorumMissing = summary.data ? summary.data.quorumRequired && !summary.data.quorumReached : false;

  return (
    <div className="flex flex-col gap-6">
      {meeting.status === 'DRAFT' || meeting.status === 'SCHEDULED' ? (
        <Alert variant="warning" message="Convoquez les copropriétaires avant d'ouvrir la séance." />
      ) : null}

      {summary.data && <AttendanceSummaryCard summary={summary.data} />}

      <Card className="flex flex-col gap-4">
        <h3 className="font-medium text-gray-900 dark:text-white/90">Tenue de la séance</h3>
        {openMeeting.isError && <Alert message={getErrorMessage(openMeeting.error)} />}
        {closeMeeting.isError && <Alert message={getErrorMessage(closeMeeting.error)} />}

        {meeting.status === 'CONVENED' && (
          <div className="flex flex-col gap-3">
            {/* Opening without quorum is lawful but consequential: it has to be asked for
                explicitly, and it is then recorded on the AG and printed in the minutes. */}
            {quorumMissing && (
              <Alert
                variant="warning"
                message="Le quorum n'est pas atteint. Ouvrir la séance malgré tout est possible, mais la mention figurera au procès-verbal."
              />
            )}
            <div className="flex flex-wrap gap-3">
              <Button isLoading={openMeeting.isPending} onClick={() => openMeeting.mutate(false)}>
                Ouvrir la séance
              </Button>
              {quorumMissing &&
                (confirmForce ? (
                  <Button
                    variant="secondary"
                    isLoading={openMeeting.isPending}
                    onClick={() => openMeeting.mutate(true)}
                  >
                    Confirmer l'ouverture sans quorum
                  </Button>
                ) : (
                  <Button variant="secondary" onClick={() => setConfirmForce(true)}>
                    Ouvrir sans quorum…
                  </Button>
                ))}
            </div>
          </div>
        )}

        {meeting.status === 'IN_PROGRESS' && (
          <div className="flex flex-wrap items-center gap-3">
            <Button variant="secondary" isLoading={closeMeeting.isPending} onClick={() => closeMeeting.mutate()}>
              Clôturer la séance
            </Button>
            <span className="text-sm text-gray-500 dark:text-gray-400">
              Clôturer permet ensuite de rédiger le procès-verbal.
            </span>
          </div>
        )}

        {(meeting.status === 'CLOSED' || meeting.status === 'MINUTES_PUBLISHED') && (
          <p className="text-sm text-gray-500 dark:text-gray-400">
            La séance est clôturée. Rendez-vous dans l'onglet « Procès-verbal ».
          </p>
        )}
      </Card>

      <Card className="flex flex-col gap-4">
        <div>
          <h3 className="font-medium text-gray-900 dark:text-white/90">Émargement</h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Seul un lot émargé peut voter, et seul son poids compte pour le quorum. Une réponse « présent » ne
            suffit pas.
          </p>
        </div>
        {convocations.isLoading && <Loader label="Chargement de la feuille de présence…" />}
        {convocations.isError && <Alert message={getErrorMessage(convocations.error)} />}
        {checkIn.isError && <Alert message={getErrorMessage(checkIn.error)} />}
        {undo.isError && <Alert message={getErrorMessage(undo.error)} />}

        {convocations.data && rows.length === 0 && (
          <EmptyState title="Aucune convocation">Générez les convocations pour établir la feuille de présence.</EmptyState>
        )}

        {rows.length > 0 && (
          <ul className="flex flex-col gap-2">
            {rows.map((convocation) => (
              <li
                key={convocation.id}
                className="flex flex-col gap-2 rounded-xl border border-gray-200 dark:border-gray-800 p-3 sm:flex-row sm:items-center sm:justify-between"
              >
                <div className="flex flex-col">
                  <span className="text-gray-900 dark:text-white/90">
                    {formatLotLabel(convocation.unitNumber, convocation.buildingName)}
                  </span>
                  <span className="text-sm text-gray-500 dark:text-gray-400">
                    {formatWeight(convocation.votingWeight)} voix
                    {convocation.checkedIn && convocation.attendanceMode
                      ? ` · émargé (${ATTENDANCE_MODE_LABELS[convocation.attendanceMode].toLowerCase()})`
                      : ''}
                  </span>
                </div>
                <div className="flex flex-wrap gap-2">
                  {convocation.checkedIn ? (
                    <Button variant="secondary" disabled={!canCheckIn} onClick={() => undo.mutate(convocation.id)}>
                      Annuler l'émargement
                    </Button>
                  ) : (
                    <>
                      <Button
                        variant="secondary"
                        disabled={!canCheckIn}
                        onClick={() => checkIn.mutate({ convocationId: convocation.id, mode: 'ON_SITE' })}
                      >
                        Présent sur place
                      </Button>
                      <Button
                        variant="secondary"
                        disabled={!canCheckIn}
                        onClick={() => checkIn.mutate({ convocationId: convocation.id, mode: 'REMOTE' })}
                      >
                        À distance
                      </Button>
                    </>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </Card>

      <div className="flex flex-col gap-4">
        <h3 className="font-medium text-gray-900 dark:text-white/90">Scrutins</h3>
        {agenda.isLoading && <Loader label="Chargement des points…" />}
        {(agenda.data ?? []).map((item, index) => (
          <BallotPanel
            // Remounted when the ballot's status changes so opening one unfolds its panel,
            // rather than leaving the syndic to unfold what they just opened.
            key={`${item.id}-${item.voteSessionStatus}`}
            meetingId={meeting.id}
            item={item}
            position={index + 1}
            convocations={rows}
            sessionInProgress={meeting.status === 'IN_PROGRESS'}
          />
        ))}
      </div>
    </div>
  );
}
