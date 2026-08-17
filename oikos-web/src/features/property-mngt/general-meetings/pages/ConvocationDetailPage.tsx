import { useState } from 'react';
import { Link, useOutletContext, useParams } from 'react-router-dom';
import type { Property } from '@/features/property-mngt/properties/types/property.types';
import type { ChannelCode } from '@/features/property-mngt/general-meetings/types/convocation.types';
import type { AttendanceAnswerValue } from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { useConvocationChannels } from '@/features/property-mngt/general-meetings/hooks/useConvocationChannels';
import { useConvocation } from '@/features/property-mngt/general-meetings/hooks/useConvocation';
import { useDownloadConvocationDocument } from '@/features/property-mngt/general-meetings/hooks/useDownloadConvocationDocument';
import {
  useCheckInConvocation,
  useRecordConvocationDelivery,
  useReplyToConvocation,
  useSendConvocation,
  useUndoCheckIn,
} from '@/features/property-mngt/general-meetings/hooks/useMeetingMutations';
import {
  ATTENDANCE_ANSWERS,
  ATTENDANCE_MODE_LABELS,
  attendanceAnswerLabel,
  CONVOCATION_STATUS_COLORS,
  CONVOCATION_STATUS_LABELS,
  DELIVERY_STATUS_LABELS,
  REPLY_SOURCE_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import {
  formatLotLabel,
  formatWeight,
  nowAsDateTimeLocal,
} from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Input } from '@/shared/components/Input/Input';
import { Loader } from '@/shared/components/Loader/Loader';
import { Select } from '@/shared/components/Select/Select';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const DATE_TIME = new Intl.DateTimeFormat('fr-FR', {
  dateStyle: 'long',
  timeStyle: 'short',
  timeZone: 'Africa/Casablanca',
});

/** Two instants on the same calendar day in Casablanca, where the syndic works. */
function isSameDay(a: string, b: string): boolean {
  const day = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'short', timeZone: 'Africa/Casablanca' });
  return day.format(new Date(a)) === day.format(new Date(b));
}

function DetailRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div>
      <dt className="text-sm text-gray-500 dark:text-gray-400">{label}</dt>
      <dd className="text-gray-900 dark:text-white/90">{value}</dd>
    </div>
  );
}

/**
 * One lot's convocation in full: who it is addressed to, where it stands, and
 * every action available on it.
 *
 * <p>This is where the history lives. The tracking table has one line per lot
 * and can only summarise; a convocation emailed, then posted, then confirmed by
 * telephone is three facts, and this page is the only place they all fit.
 *
 * <p>Fetched on its own rather than picked out of the cached list, which is
 * what it used to do. The list deliberately carries no confirmation code - a
 * hundred of them in one payload is the whole copropriété's answers on one
 * screen - so this page is the only one that can show the code, and the only
 * way to have it is to ask for this convocation alone.
 */
export function ConvocationDetailPage() {
  const { property } = useOutletContext<{ property: Property }>();
  const { meetingId, convocationId } = useParams<{ meetingId: string; convocationId: string }>();
  const convocationQuery = useConvocation(convocationId ?? '');
  const channels = useConvocationChannels();
  const send = useSendConvocation(meetingId ?? '');
  const recordDelivery = useRecordConvocationDelivery(meetingId ?? '');
  const reply = useReplyToConvocation(meetingId ?? '');
  const checkIn = useCheckInConvocation(meetingId ?? '');
  const undo = useUndoCheckIn(meetingId ?? '');
  const download = useDownloadConvocationDocument();

  const [channelCode, setChannelCode] = useState('');
  const [reference, setReference] = useState('');
  const [note, setNote] = useState('');
  const [answer, setAnswer] = useState<AttendanceAnswerValue>(ATTENDANCE_ANSWERS[0].value);
  // Defaulted to now rather than left empty: recording a call as it ends is the ordinary
  // case, and the field only has to be touched for the letter that arrived on Tuesday.
  const [receivedAt, setReceivedAt] = useState(() => nowAsDateTimeLocal());

  const channelList = channels.data ?? [];
  /**
   * What a person can have done themselves, in the order a syndic works through
   * them: handed over, posted, sent recorded, emailed from their own mailbox.
   * The API accepts a recorded delivery on any channel (it uses `require`, not
   * `requireSendable`), so an email the syndic sent by hand is legitimate here.
   *
   * The messagerie is not: it is performed by the application and has its own
   * button, so recording it here could only mean claiming a send that never
   * happened. Ordered explicitly rather than by the catalog's `position`, which
   * is tuned for the sending screen - a channel the list does not name still
   * appears, at the end, so adding one stays an INSERT.
   */
  const RECORDABLE_ORDER = ['MANUAL', 'POSTAL_MAIL', 'REGISTERED_MAIL', 'EMAIL'];
  const rank = (code: ChannelCode) => {
    const index = RECORDABLE_ORDER.indexOf(code);
    return index === -1 ? RECORDABLE_ORDER.length : index;
  };
  const recordableChannels = channelList
    .filter((channel) => channel.code !== 'APP')
    .sort((a, b) => rank(a.code) - rank(b.code));
  const automatedCode = channelList.find((channel) => channel.automated)?.code ?? 'EMAIL';

  if (convocationQuery.isLoading) {
    return <Loader label="Chargement de la convocation…" />;
  }

  if (convocationQuery.isError) {
    return <Alert message={getErrorMessage(convocationQuery.error)} />;
  }

  const convocation = convocationQuery.data;
  const backPath = `/property-mngt/properties/${property.id}/general-meetings/${meetingId}/convocations`;

  if (!convocation) {
    return (
      <div className="flex flex-col gap-4">
        <Alert message="Cette convocation est introuvable pour cette assemblée." />
        <Link to={backPath} className="text-sm text-brand-500 hover:underline">
          ← Retour au suivi des convocations
        </Link>
      </div>
    );
  }

  const lotLabel = formatLotLabel(convocation.unitNumber, convocation.buildingName);
  const isBusy = send.isPending || recordDelivery.isPending || reply.isPending || checkIn.isPending || undo.isPending;
  const actionError = send.error ?? recordDelivery.error ?? reply.error ?? checkIn.error ?? undo.error;

  /**
   * Records the answer, then clears what described that one answer - the note,
   * the date - because the next answer is a different event and inheriting them
   * would attribute a phone call to a letter.
   *
   * <p>The five choices are three fields underneath: a reply, how the lot
   * announced it would attend, and whether a stand-in was announced. The server
   * drops the last two on an "absent", and on any answer the owner gave from
   * their own space.
   */
  function recordReply() {
    const choice = ATTENDANCE_ANSWERS.find((option) => option.value === answer) ?? ATTENDANCE_ANSWERS[0];
    reply.mutate({
      convocationId: convocation!.id,
      attendanceReply: choice.reply,
      attendanceMode: choice.mode,
      byProxy: choice.byProxy,
      receivedAt: receivedAt ? new Date(receivedAt).toISOString() : null,
      note,
    });
    setNote('');
    setReceivedAt(nowAsDateTimeLocal());
  }

  function markDelivered() {
    recordDelivery.mutate({
      convocationId: convocation!.id,
      channel: channelCode,
      deliveryStatus: 'SENT',
      reference,
    });
    setReference('');
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <Link to={backPath} className="text-sm text-gray-500 dark:text-gray-400 hover:underline">
          ← Retour au suivi des convocations
        </Link>
        <div className="flex flex-wrap items-center gap-2">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white/90">{lotLabel}</h3>
          <Badge color={CONVOCATION_STATUS_COLORS[convocation.status]}>
            {CONVOCATION_STATUS_LABELS[convocation.status]}
          </Badge>
        </div>
      </div>

      <div className="flex flex-wrap gap-2">
        <Button
          variant="secondary"
          isLoading={download.isPending}
          onClick={() =>
            download.mutate({ convocationId: convocation.id, fileName: `convocation-${lotLabel}.pdf` })
          }
        >
          Télécharger la convocation (PDF)
        </Button>
        <Button
          variant="secondary"
          disabled={isBusy}
          onClick={() => send.mutate({ convocationId: convocation.id, channel: automatedCode })}
        >
          Envoyer par email
        </Button>
      </div>

      {actionError && <Alert message={getErrorMessage(actionError)} />}
      {download.isError && <Alert message={getErrorMessage(download.error)} />}
      {channels.isError && <Alert message={getErrorMessage(channels.error)} />}

      <Card className="flex flex-col gap-4">
        <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <DetailRow label="Lot" value={lotLabel} />
          <DetailRow label="Voix" value={`${formatWeight(convocation.votingWeight)} voix`} />
          <DetailRow
            label="Envoi"
            value={`${DELIVERY_STATUS_LABELS[convocation.deliveryStatus]}${
              convocation.sentAt ? ` · premier envoi le ${DATE_TIME.format(new Date(convocation.sentAt))}` : ''
            }`}
          />
          <DetailRow
            label="Réponse"
            value={
              <>
                {attendanceAnswerLabel(
                  convocation.attendanceReply,
                  convocation.replyAttendanceMode,
                  convocation.replyByProxy,
                )}
                {convocation.repliedAt && ` · ${DATE_TIME.format(new Date(convocation.repliedAt))}`}
                {/* How it was obtained, not just that it was: a confirmation taken over the
                    phone and one the owner gave from their space are not the same evidence. */}
                {convocation.replySource && (
                  <span className="text-gray-500 dark:text-gray-400">
                    {' '}
                    ({REPLY_SOURCE_LABELS[convocation.replySource].toLowerCase()}
                    {/* The means is a declaration, the source is what the server witnessed -
                        shown together, but never merged into one word. */}
                    {convocation.replyMediumLabel && `, ${convocation.replyMediumLabel.toLowerCase()}`})
                  </span>
                )}
                {convocation.replyNote && (
                  <span className="block text-sm italic text-gray-500 dark:text-gray-400">
                    « {convocation.replyNote} »
                  </span>
                )}
              </>
            }
          />
          <DetailRow
            label="Émargement"
            value={
              convocation.checkedIn && convocation.checkedInAt
                ? `Émargé ${
                    convocation.attendanceMode
                      ? `(${ATTENDANCE_MODE_LABELS[convocation.attendanceMode].toLowerCase()}) `
                      : ''
                  }· ${DATE_TIME.format(new Date(convocation.checkedInAt))}`
                : 'Non émargé'
            }
          />
        </dl>

        <div>
          <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300">Destinataires</h4>
          {convocation.recipients.length === 0 ? (
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Lot non affecté : aucun copropriétaire rattaché. Il compte dans le total des voix mais ne peut ni
              répondre ni émarger.
            </p>
          ) : (
            <ul className="mt-1 flex flex-col gap-1">
              {convocation.recipients.map((recipient) => (
                <li key={recipient.fullName + (recipient.email ?? '')} className="text-sm">
                  <span className="text-gray-900 dark:text-white/90">{recipient.fullName}</span>{' '}
                  {recipient.email ? (
                    <span className="text-gray-500 dark:text-gray-400">— {recipient.email}</span>
                  ) : (
                    // Naming the reason beats leaving the syndic to guess at a FAILED row.
                    <span className="text-warning-600 dark:text-warning-400">— aucune adresse email</span>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </Card>

      {convocation.confirmationCode && (
        <Card className="flex flex-col gap-2">
          <div>
            <h4 className="font-medium text-gray-900 dark:text-white/90">Codes de confirmation</h4>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Imprimés sur la convocation de ce lot, à côté du QR code. À dicter au copropriétaire qui a égaré sa
              lettre — ils lui permettent de confirmer sa présence sans compte.
            </p>
          </div>
          <dl className="flex flex-wrap gap-6">
            <div>
              <dt className="text-sm text-gray-500 dark:text-gray-400">Référence de l'assemblée</dt>
              <dd className="font-mono text-lg uppercase tracking-widest text-gray-900 dark:text-white/90">
                {convocation.meetingPublicReference}
              </dd>
            </div>
            <div>
              <dt className="text-sm text-gray-500 dark:text-gray-400">Code de ce lot</dt>
              <dd className="font-mono text-lg uppercase tracking-widest text-gray-900 dark:text-white/90">
                {convocation.confirmationCode}
              </dd>
            </div>
          </dl>
        </Card>
      )}

      {/* The trail and the way to add to it, in one block: what a syndic reads to decide
          whether to post a letter is the same thing he comes back to record afterwards. */}
      <Card className="flex flex-col gap-3">
        <h4 className="font-medium text-gray-900 dark:text-white/90">Envois de la convocation</h4>
        {convocation.deliveries.length === 0 ? (
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Aucun envoi pour le moment — la convocation existe mais n'est encore partie par aucun canal.
          </p>
        ) : (
          <ul className="flex flex-col gap-2">
            {convocation.deliveries.map((delivery) => (
              <li
                key={delivery.id}
                className="flex flex-wrap items-center gap-2 rounded-lg border border-gray-200 dark:border-gray-800 px-3 py-2 text-sm"
              >
                <Badge color={delivery.status === 'SENT' ? 'success' : 'error'}>
                  {DELIVERY_STATUS_LABELS[delivery.status]}
                </Badge>
                <span className="text-gray-900 dark:text-white/90">{delivery.channelLabel}</span>
                {/* A reminder is a send like any other and sits in the same list; only its label
                    sets it apart, so a syndic can tell "we convoked them" from "we chased them". */}
                {delivery.reminder && <Badge color="light">Relance</Badge>}
                {delivery.sentAt && (
                  <span className="text-gray-500 dark:text-gray-400">
                    {DATE_TIME.format(new Date(delivery.sentAt))}
                  </span>
                )}
                {delivery.reference && (
                  <span className="text-gray-500 dark:text-gray-400">n° {delivery.reference}</span>
                )}
              </li>
            ))}
          </ul>
        )}
        <div className="flex flex-col gap-3 border-t border-gray-100 dark:border-gray-800 pt-4">
          <h5 className="text-sm font-medium text-gray-700 dark:text-gray-300">Enregistrer une remise</h5>
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
            <div className="sm:max-w-xs sm:flex-1">
              {/* No default: a channel picked by the form rather than by the syndic is the
                  one that ends up recorded by accident. Which channels are offered, and in
                  what order, is settled on recordableChannels above. */}
              <Select
                label="Canal"
                name="deliveryChannel"
                value={channelCode}
                onChange={(event) => setChannelCode(event.target.value)}
              >
                <option value="">Choisir…</option>
                {recordableChannels.map((channel) => (
                  <option key={channel.code} value={channel.code}>
                    {channel.label}
                  </option>
                ))}
              </Select>
            </div>
            <div className="sm:max-w-xs sm:flex-1">
              <Input
                label="Référence (facultatif)"
                name="reference"
                value={reference}
                placeholder="N° de suivi du recommandé"
                onChange={(event) => setReference(event.target.value)}
              />
            </div>
            <Button variant="secondary" disabled={isBusy || !channelCode} onClick={markDelivered}>
              Enregistrer
            </Button>
          </div>
        </div>
      </Card>

      <Card className="flex flex-col gap-4">
        <h4 className="font-medium text-gray-900 dark:text-white/90">Réponses</h4>
        {convocation.replies.length === 0 ? (
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Aucune réponse pour le moment.
          </p>
        ) : (
          <ul className="flex flex-col gap-2">
            {convocation.replies.map((entry, index) => (
              <li
                key={entry.id}
                className="flex flex-col gap-1 rounded-lg border border-gray-200 dark:border-gray-800 px-3 py-2 text-sm"
              >
                <div className="flex flex-wrap items-center gap-2">
                  <Badge color={index === 0 ? 'primary' : 'light'}>
                    {attendanceAnswerLabel(entry.attendanceReply, entry.attendanceMode, entry.byProxy)}
                  </Badge>
                  {/* Only the head of the list is the answer that counts; the rest is how it
                      got there, and a reader must not have to work that out. */}
                  {index === 0 && <span className="text-xs text-gray-500 dark:text-gray-400">fait foi</span>}
                  <span className="text-gray-900 dark:text-white/90">
                    {DATE_TIME.format(new Date(entry.receivedAt))}
                  </span>
                  <span className="text-gray-500 dark:text-gray-400">
                    {REPLY_SOURCE_LABELS[entry.source].toLowerCase()}
                    {entry.mediumLabel && `, ${entry.mediumLabel.toLowerCase()}`}
                  </span>
                </div>
                {/* Shown only when it differs from the day it was received: printing "saisie
                    le" on every line would bury the few entries where it actually matters. */}
                {entry.recordedAt && !isSameDay(entry.receivedAt, entry.recordedAt) && (
                  <span className="text-xs text-gray-400 dark:text-gray-500">
                    saisie le {DATE_TIME.format(new Date(entry.recordedAt))}
                  </span>
                )}
                {entry.note && (
                  <span className="italic text-gray-500 dark:text-gray-400">« {entry.note} »</span>
                )}
              </li>
            ))}
          </ul>
        )}
        <div className="flex flex-col gap-3 border-t border-gray-100 dark:border-gray-800 pt-4">
          <h5 className="text-sm font-medium text-gray-700 dark:text-gray-300">Enregistrer une réponse</h5>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
            {/* Five choices, three fields underneath - see ATTENDANCE_ANSWERS. Offering the
                combinations that exist beats a reply/mode/proxy triplet the form would then
                have to stop from producing "absent, sur place". */}
            <Select
              label="Réponse"
              name="attendanceAnswer"
              value={answer}
              onChange={(event) => setAnswer(event.target.value as AttendanceAnswerValue)}
            >
              {ATTENDANCE_ANSWERS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </Select>
            {/* Defaults to now, and stays backdatable: a letter that arrived on Tuesday is
                keyed in on Thursday, and it is the Tuesday that decides which answer stands. */}
            <Input
              type="datetime-local"
              label="Date"
              name="receivedAt"
              value={receivedAt}
              onChange={(event) => setReceivedAt(event.target.value)}
            />
            <Input
              label="Précision (facultatif)"
              name="replyNote"
              value={note}
              placeholder="Ex. : a appelé le bureau mardi"
              onChange={(event) => setNote(event.target.value)}
            />
          </div>
          <div>
            <Button variant="secondary" disabled={isBusy} onClick={recordReply}>
              Enregistrer
            </Button>
          </div>
        </div>
      </Card>


    </div>
  );
}
