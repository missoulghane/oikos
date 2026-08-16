import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useDownloadConvocationDocument } from '@/features/property-mngt/general-meetings/hooks/useDownloadConvocationDocument';
import type { GeneralMeetingContext } from '@/features/property-mngt/general-meetings/pages/GeneralMeetingDetailLayout';
import { useConvocationChannels } from '@/features/property-mngt/general-meetings/hooks/useConvocationChannels';
import { useConvocations } from '@/features/property-mngt/general-meetings/hooks/useConvocations';
import { useAttendanceSummary } from '@/features/property-mngt/general-meetings/hooks/useAttendanceSummary';
import {
  useGenerateConvocations,
  useRecordConvocationDelivery,
  useRemindConvocations,
  useReplyToConvocation,
  useScheduleGeneralMeeting,
  useSendConvocation,
  useSendPendingConvocations,
} from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import { AttendanceSummaryCard } from '@/features/property-mngt/general-meetings/components/AttendanceSummaryCard';
import { ConvocationTrackingTable } from '@/features/property-mngt/general-meetings/components/ConvocationTrackingTable';
import { formatLotLabel } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Loader } from '@/shared/components/Loader/Loader';
import { Select } from '@/shared/components/Select/Select';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

/**
 * Two separate actions, in the order they are performed: generate, then send.
 * They used to be one button, whose failure mode - "some emails went out, some
 * didn't, and the meeting is convened either way" - could not be explained to
 * anyone. Generating now creates the rows and convokes the meeting; sending is
 * a deliberate second step, on a set the syndic has had a chance to look at.
 *
 * <p>The channels come from the server rather than from a constant here: the
 * catalog is data precisely so that adding one needs no release of this app.
 */
export function ConvocationsTab() {
  const { property, meeting } = useOutletContext<GeneralMeetingContext>();
  const convocations = useConvocations(meeting.id);
  const channels = useConvocationChannels();
  const summary = useAttendanceSummary(meeting.id);
  const generate = useGenerateConvocations(meeting.id);
  const sendPending = useSendPendingConvocations(meeting.id);
  const schedule = useScheduleGeneralMeeting(meeting.id);
  const remind = useRemindConvocations(meeting.id);
  const send = useSendConvocation(meeting.id);
  const recordDelivery = useRecordConvocationDelivery(meeting.id);
  const reply = useReplyToConvocation(meeting.id);
  const download = useDownloadConvocationDocument();
  // Null until the syndic picks one, and only then a code. The default is the first row of
  // the catalog - what the product offers first is the catalog's decision, and its `position`
  // already says so. Derived rather than pushed into state by an effect, which would render
  // once with no selection and once more with it.
  const [chosenChannelCode, setChosenChannelCode] = useState<string | null>(null);

  const channelList = channels.data ?? [];
  const selectedChannel =
    channelList.find((channel) => channel.code === chosenChannelCode) ?? channelList[0];
  const channelCode = selectedChannel?.code ?? '';

  const rows = convocations.data ?? [];
  const hasAgenda = meeting.agendaItemCount > 0;
  const hasDateAndVenue = Boolean(meeting.scheduledAt && meeting.venueType);
  const pendingCount = rows.filter((convocation) => convocation.deliveryStatus !== 'SENT').length;
  const isAutomatedChannel = selectedChannel?.automated ?? false;
  const automatedCode = channelList.find((channel) => channel.automated)?.code ?? 'EMAIL';

  const canGenerate =
    hasAgenda && hasDateAndVenue && meeting.status !== 'CLOSED' && meeting.status !== 'MINUTES_PUBLISHED';

  /**
   * Generating convokes the meeting when it is still a draft: the date and the
   * venue were settled at creation, so there is nothing left to ask, and the
   * DRAFT -> SCHEDULED transition is what "ready to convoke" means. It sends
   * nothing - that is the button below.
   */
  async function generateAll() {
    if (meeting.status === 'DRAFT' && meeting.scheduledAt && meeting.venueType) {
      await schedule.mutateAsync({
        scheduledAt: meeting.scheduledAt,
        venueType: meeting.venueType,
        venueAddress: meeting.venueAddress,
        venueLink: meeting.venueLink,
      });
    }
    await generate.mutateAsync();
  }

  return (
    <div className="flex flex-col gap-6">
      {!hasAgenda && (
        <Alert
          variant="warning"
          message="Ajoutez au moins un point à l'ordre du jour avant de convoquer les copropriétaires."
        />
      )}
      {!hasDateAndVenue && (
        <Alert variant="warning" message="Cette assemblée n'a ni date ni lieu : elle ne peut pas être convoquée." />
      )}

      {summary.data && <AttendanceSummaryCard summary={summary.data} />}

      <Card className="flex flex-col gap-4">
        <div>
          <h3 className="font-medium text-gray-900 dark:text-white/90">1. Générer les convocations</h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Une convocation est créée pour <strong>chaque lot</strong> de la copropriété, y compris ceux sans
            copropriétaire rattaché : ils comptent dans le total des voix. Rien n'est envoyé à cette étape, et
            relancer l'action ne crée aucun doublon.
          </p>
        </div>
        <div>
          <Button isLoading={generate.isPending || schedule.isPending} disabled={!canGenerate} onClick={generateAll}>
            Générer les convocations
          </Button>
        </div>
        {schedule.isError && <Alert message={getErrorMessage(schedule.error)} />}
        {generate.isError && <Alert message={getErrorMessage(generate.error)} />}
      </Card>

      <Card className="flex flex-col gap-4">
        <div>
          <h3 className="font-medium text-gray-900 dark:text-white/90">2. Envoyer les convocations</h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {rows.length === 0
              ? 'Générez d’abord les convocations.'
              : `${pendingCount} convocation(s) sans envoi abouti sur ${rows.length}. Renvoyer ne réexpédie que celles-là — jamais un lot déjà joint, quel qu'ait été le canal.`}
          </p>
        </div>
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
          <div className="sm:max-w-xs sm:flex-1">
            <Select
              label="Canal d'envoi"
              name="channel"
              value={channelCode}
              onChange={(event) => setChosenChannelCode(event.target.value)}
            >
              {channelList.map((channel) => (
                <option key={channel.code} value={channel.code}>
                  {channel.label}
                </option>
              ))}
            </Select>
          </div>
          <Button
            isLoading={sendPending.isPending}
            disabled={pendingCount === 0 || !isAutomatedChannel}
            onClick={() => sendPending.mutate(channelCode)}
          >
            Envoyer les convocations en attente
          </Button>
          <Button variant="secondary" isLoading={remind.isPending} onClick={() => remind.mutate()}>
            Relancer les non-répondants
          </Button>
        </div>
        {channels.isError && <Alert message={getErrorMessage(channels.error)} />}
        {/* Courrier and recommandé are never "sent" by the app: pressing a button must not be able
            to claim a letter left the building. */}
        {selectedChannel && !isAutomatedChannel && (
          <Alert
            variant="warning"
            message={`« ${selectedChannel.label} » n'est pas automatisé : imprimez les convocations générées, remettez-les, puis marquez la remise ligne par ligne dans le suivi.`}
          />
        )}
        {sendPending.isError && <Alert message={getErrorMessage(sendPending.error)} />}
        {sendPending.isSuccess && (
          <Alert
            variant={sendPending.data.failedCount > 0 ? 'warning' : 'success'}
            message={
              sendPending.data.failedCount > 0
                ? `${sendPending.data.sentCount} convocation(s) envoyée(s), ${sendPending.data.failedCount} en échec — les lots concernés apparaissent en « Échec » dans le suivi.`
                : `${sendPending.data.sentCount} convocation(s) envoyée(s).`
            }
          />
        )}
        {remind.isError && <Alert message={getErrorMessage(remind.error)} />}
        {remind.isSuccess && (
          <Alert
            variant="success"
            message={`${remind.data.remindedCount} relance(s) envoyée(s) aux lots joignables restés sans réponse.`}
          />
        )}
      </Card>

      <Card className="flex flex-col gap-4">
        <h3 className="font-medium text-gray-900 dark:text-white/90">Suivi des convocations</h3>
        {convocations.isLoading && <Loader label="Chargement des convocations…" />}
        {convocations.isError && <Alert message={getErrorMessage(convocations.error)} />}
        {send.isError && <Alert message={getErrorMessage(send.error)} />}
        {recordDelivery.isError && <Alert message={getErrorMessage(recordDelivery.error)} />}
        {reply.isError && <Alert message={getErrorMessage(reply.error)} />}
        {download.isError && <Alert message={getErrorMessage(download.error)} />}

        {convocations.data && rows.length === 0 && (
          <EmptyState title="Aucune convocation générée">
            Générez les convocations pour ouvrir le suivi lot par lot.
          </EmptyState>
        )}

        {rows.length > 0 && (
          <ConvocationTrackingTable
            convocations={rows}
            isBusy={send.isPending || recordDelivery.isPending || reply.isPending}
            detailPathOf={(convocationId) =>
              `/property-mngt/properties/${property.id}/general-meetings/${meeting.id}/convocations/${convocationId}`
            }
            onDownload={(convocation) =>
              download.mutate({
                convocationId: convocation.id,
                fileName: `convocation-${formatLotLabel(convocation.unitNumber, convocation.buildingName)}.pdf`,
              })
            }
            // The row's two shortcuts use the channel the run is set up with, so that "envoyer"
            // and "marquer remise" on one line mean the same thing as the bulk action above.
            onSend={(convocationId) => send.mutate({ convocationId, channel: automatedCode })}
            onMarkDelivered={(convocationId) =>
              recordDelivery.mutate({ convocationId, channel: channelCode, deliveryStatus: 'SENT' })
            }
            onReply={(convocationId, attendanceReply) => reply.mutate({ convocationId, reply: attendanceReply })}
          />
        )}
      </Card>
    </div>
  );
}
