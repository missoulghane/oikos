import type { AttendanceReply, Convocation } from '@/features/property-mngt/general-meetings/types/convocation.types';
import {
  ATTENDANCE_REPLY_LABELS,
  CONVOCATION_STATUS_COLORS,
  CONVOCATION_STATUS_LABELS,
  DELIVERY_STATUS_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { formatLotLabel, formatWeight } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { Link } from 'react-router-dom';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { DownloadIcon } from '@/shared/icons';

interface ConvocationTrackingTableProps {
  convocations: Convocation[];
  isBusy: boolean;
  detailPathOf: (convocationId: string) => string;
  onSend: (convocationId: string) => void;
  onMarkDelivered: (convocationId: string) => void;
  onReply: (convocationId: string, reply: AttendanceReply) => void;
  onDownload: (convocation: Convocation) => void;
}

/**
 * One row per lot. "Envoyer" performs the send; "Marquer remise" records that
 * a person did it - two distinct actions, because a tracking table that cannot
 * tell them apart has lost the only thing it exists to record.
 *
 * <p>Two layouts for the same data: stacked cards below `sm`, the table above
 * it. Seven columns cannot be read on a phone, and a table that only scrolls
 * sideways hides the actions - which is the column people come for.
 */
export function ConvocationTrackingTable({
  convocations,
  isBusy,
  detailPathOf,
  onSend,
  onMarkDelivered,
  onReply,
  onDownload,
}: ConvocationTrackingTableProps) {
  function ownerLabel(convocation: Convocation) {
    return convocation.recipients.length > 0 ? (
      convocation.recipients.map((recipient) => recipient.fullName).join(', ')
    ) : (
      // A lot with no owner is convoked all the same and weighs in the totals -
      // it simply cannot answer or sign in.
      <span className="italic">Lot non affecté</span>
    );
  }

  /**
   * The state of the sending, then the channels it went through. One column
   * cannot list every attempt, so it names them and leaves the dates and the
   * tracking numbers to the detail page.
   */
  function deliveryLabel(convocation: Convocation) {
    const channels = convocation.deliveries.map((delivery) => delivery.channelLabel).join(', ');
    return channels
      ? `${DELIVERY_STATUS_LABELS[convocation.deliveryStatus]} · ${channels}`
      : DELIVERY_STATUS_LABELS[convocation.deliveryStatus];
  }

  function actions(convocation: Convocation) {
    return (
      <div className="flex flex-wrap gap-2">
        {/* The letter is downloadable before anything is sent: that is what a syndic prints
            when convoking by post. */}
        <Button
          variant="secondary"
          title="Télécharger la convocation (PDF)"
          aria-label="Télécharger la convocation en PDF"
          onClick={() => onDownload(convocation)}
        >
          <DownloadIcon />
        </Button>
        <Link
          to={detailPathOf(convocation.id)}
          aria-label="Ouvrir le détail de la convocation"
          title="Détail"
          className="inline-flex min-h-11 items-center justify-center rounded-lg bg-white dark:bg-gray-800 px-3 py-2 text-sm font-medium text-gray-700 dark:text-gray-400 shadow-theme-xs ring-1 ring-inset ring-gray-300 dark:ring-gray-700 hover:bg-gray-50 dark:hover:bg-white/[0.03]"
        >
          Détail
        </Link>
        <Button variant="secondary" disabled={isBusy} onClick={() => onSend(convocation.id)}>
          Envoyer
        </Button>
        <Button variant="secondary" disabled={isBusy} onClick={() => onMarkDelivered(convocation.id)}>
          Marquer remise
        </Button>
        <Button variant="secondary" disabled={isBusy} onClick={() => onReply(convocation.id, 'ATTENDING')}>
          Présent
        </Button>
        <Button variant="secondary" disabled={isBusy} onClick={() => onReply(convocation.id, 'NOT_ATTENDING')}>
          Absent
        </Button>
      </div>
    );
  }

  return (
    <>
      <ul className="flex flex-col gap-3 sm:hidden">
        {convocations.map((convocation) => (
          <li
            key={convocation.id}
            className="flex flex-col gap-2 rounded-xl border border-gray-200 dark:border-gray-800 p-3"
          >
            <div className="flex flex-wrap items-center justify-between gap-2">
              <Link
                to={detailPathOf(convocation.id)}
                className="font-medium text-gray-900 dark:text-white/90 hover:underline"
              >
                {formatLotLabel(convocation.unitNumber, convocation.buildingName)}
              </Link>
              <Badge color={CONVOCATION_STATUS_COLORS[convocation.status]}>
                {CONVOCATION_STATUS_LABELS[convocation.status]}
              </Badge>
            </div>
            <p className="text-sm text-gray-500 dark:text-gray-400">{ownerLabel(convocation)}</p>
            <p className="text-sm text-gray-400 dark:text-gray-500">
              {formatWeight(convocation.votingWeight)} voix · {deliveryLabel(convocation)} ·{' '}
              {ATTENDANCE_REPLY_LABELS[convocation.attendanceReply]}
            </p>
            {actions(convocation)}
          </li>
        ))}
      </ul>

      <div className="hidden overflow-x-auto sm:block">
        <table className="w-full min-w-[52rem] text-left text-sm">
          <thead className="text-gray-500 dark:text-gray-400">
            <tr className="border-b border-gray-200 dark:border-gray-800">
              <th className="py-2 pr-4 font-normal">Lot</th>
              <th className="py-2 pr-4 font-normal">Copropriétaire(s)</th>
              <th className="py-2 pr-4 font-normal">Voix</th>
              <th className="py-2 pr-4 font-normal">Envoi</th>
              <th className="py-2 pr-4 font-normal">Réponse</th>
              <th className="py-2 pr-4 font-normal">Statut</th>
              <th className="py-2 font-normal">Actions</th>
            </tr>
          </thead>
          <tbody>
            {convocations.map((convocation) => (
              <tr key={convocation.id} className="border-b border-gray-100 dark:border-gray-800/60">
                <td className="py-3 pr-4">
                  <Link
                    to={detailPathOf(convocation.id)}
                    className="text-gray-900 dark:text-white/90 hover:underline"
                  >
                    {formatLotLabel(convocation.unitNumber, convocation.buildingName)}
                  </Link>
                </td>
                <td className="py-3 pr-4 text-gray-500 dark:text-gray-400">{ownerLabel(convocation)}</td>
                <td className="py-3 pr-4 text-gray-500 dark:text-gray-400">
                  {formatWeight(convocation.votingWeight)}
                </td>
                <td className="py-3 pr-4 text-gray-500 dark:text-gray-400">{deliveryLabel(convocation)}</td>
                <td className="py-3 pr-4 text-gray-500 dark:text-gray-400">
                  {ATTENDANCE_REPLY_LABELS[convocation.attendanceReply]}
                </td>
                <td className="py-3 pr-4">
                  <Badge color={CONVOCATION_STATUS_COLORS[convocation.status]}>
                    {CONVOCATION_STATUS_LABELS[convocation.status]}
                  </Badge>
                </td>
                <td className="py-3">{actions(convocation)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
