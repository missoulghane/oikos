import { useState } from 'react';
import { Link, useOutletContext, useParams } from 'react-router-dom';
import type { Property } from '@/features/property-mngt/properties/types/property.types';
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
  ATTENDANCE_MODE_LABELS,
  ATTENDANCE_REPLY_LABELS,
  CONVOCATION_STATUS_COLORS,
  CONVOCATION_STATUS_LABELS,
  DELIVERY_STATUS_LABELS,
  REPLY_SOURCE_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { formatLotLabel, formatWeight } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
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

  const channelList = channels.data ?? [];
  const selectedChannel = channelList.find((channel) => channel.code === channelCode) ?? channelList[0];
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

  /** Answering clears the note: it describes one answer, not the lot. */
  function answer(attendanceReply: 'ATTENDING' | 'NOT_ATTENDING') {
    reply.mutate({ convocationId: convocation!.id, reply: attendanceReply, note });
    setNote('');
  }

  function markDelivered() {
    recordDelivery.mutate({
      convocationId: convocation!.id,
      channel: selectedChannel?.code ?? automatedCode,
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
                {ATTENDANCE_REPLY_LABELS[convocation.attendanceReply]}
                {convocation.repliedAt && ` · ${DATE_TIME.format(new Date(convocation.repliedAt))}`}
                {/* How it was obtained, not just that it was: a confirmation taken over the
                    phone and one the owner gave from their space are not the same evidence. */}
                {convocation.replySource && (
                  <span className="text-gray-500 dark:text-gray-400">
                    {' '}
                    ({REPLY_SOURCE_LABELS[convocation.replySource].toLowerCase()})
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

      <Card className="flex flex-col gap-3">
        <div>
          <h4 className="font-medium text-gray-900 dark:text-white/90">Historique des envois</h4>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Chaque tentative, réussie ou non, sur chaque canal. Un envoi n'en efface jamais un autre : c'est cette
            liste qui prouve ce qui est parti, quand, et par quelle voie.
          </p>
        </div>
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
      </Card>

      <Card className="flex flex-col gap-4">
        <h4 className="font-medium text-gray-900 dark:text-white/90">Enregistrer une remise</h4>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Pour ce qu'une personne a fait elle-même : une lettre postée, un recommandé déposé, une convocation
          remise en main propre. Le numéro de suivi n'a de sens que pour un recommandé.
        </p>
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
          <div className="sm:max-w-xs sm:flex-1">
            <Select
              label="Canal"
              name="deliveryChannel"
              value={selectedChannel?.code ?? ''}
              onChange={(event) => setChannelCode(event.target.value)}
            >
              {channelList.map((channel) => (
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
          <Button variant="secondary" disabled={isBusy || channelList.length === 0} onClick={markDelivered}>
            Marquer comme remise
          </Button>
        </div>
      </Card>

      <Card className="flex flex-col gap-4">
        <h4 className="font-medium text-gray-900 dark:text-white/90">Actions</h4>
        <div className="sm:max-w-md">
          <Input
            label="Précision sur la réponse (facultatif)"
            name="replyNote"
            value={note}
            placeholder="Ex. : a appelé le bureau mardi"
            onChange={(event) => setNote(event.target.value)}
          />
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Jointe à la réponse enregistrée ci-dessous. Inutile si le copropriétaire répond lui-même : l'origine de
            sa réponse est déjà tracée.
          </p>
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
          <Button variant="secondary" disabled={isBusy} onClick={() => answer('ATTENDING')}>
            Présent
          </Button>
          <Button variant="secondary" disabled={isBusy} onClick={() => answer('NOT_ATTENDING')}>
            Absent
          </Button>
          {convocation.checkedIn ? (
            <Button variant="secondary" disabled={isBusy} onClick={() => undo.mutate(convocation.id)}>
              Annuler l'émargement
            </Button>
          ) : (
            <>
              <Button
                variant="secondary"
                disabled={isBusy}
                onClick={() => checkIn.mutate({ convocationId: convocation.id, mode: 'ON_SITE' })}
              >
                Émarger sur place
              </Button>
              <Button
                variant="secondary"
                disabled={isBusy}
                onClick={() => checkIn.mutate({ convocationId: convocation.id, mode: 'REMOTE' })}
              >
                Émarger à distance
              </Button>
            </>
          )}
        </div>
      </Card>
    </div>
  );
}
