import type { Convocation } from '@/features/property-mngt/general-meetings/types/convocation.types';
import {
  ATTENDANCE_REPLY_LABELS,
  CONVOCATION_STATUS_COLORS,
  CONVOCATION_STATUS_LABELS,
  DELIVERY_STATUS_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { formatLotLabel, formatWeight } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import type { ConvocationSortField } from '@/features/property-mngt/general-meetings/utils/filterConvocations';
import { Link } from 'react-router-dom';
import { Badge } from '@/shared/components/Badge/Badge';
import { SortableColumnHeader } from '@/shared/components/SortableColumnHeader/SortableColumnHeader';
import type { SortDirection } from '@/shared/utils/sorting';

interface ConvocationTrackingTableProps {
  convocations: Convocation[];
  sortBy: ConvocationSortField;
  sortDirection: SortDirection;
  onSort: (field: ConvocationSortField) => void;
  detailPathOf: (convocationId: string) => string;
}

/**
 * One row per lot, and nothing but reading.
 *
 * <p>The row used to carry six buttons - envoyer, marquer remise, présent,
 * absent, PDF, détail - and they are gone. Every one of them already exists on
 * the convocation's own page, so nothing is lost but a click; what is gained is
 * a table that can be scanned. "Marquer remise" in particular has no business
 * on a line: recording that a letter was posted needs a channel and a tracking
 * number, and the shortcut silently borrowed both from a dropdown sitting above
 * the table.
 *
 * <p>Two layouts for the same data: stacked cards below `sm`, the table above
 * it. Six columns cannot be read on a phone.
 */
export function ConvocationTrackingTable({
  convocations,
  sortBy,
  sortDirection,
  onSort,
  detailPathOf,
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
   * tracking numbers to the detail page. Reminders are marked rather than
   * listed again: three chases by email would otherwise read as "Email, Email,
   * Email" and say nothing.
   */
  function deliveryLabel(convocation: Convocation) {
    const channels = [
      ...new Set(convocation.deliveries.filter((d) => !d.reminder).map((d) => d.channelLabel)),
    ].join(', ');
    const reminders = convocation.deliveries.filter((d) => d.reminder).length;
    const relance = reminders > 0 ? ` · ${reminders} relance${reminders > 1 ? 's' : ''}` : '';
    return channels
      ? `${DELIVERY_STATUS_LABELS[convocation.deliveryStatus]} · ${channels}${relance}`
      : `${DELIVERY_STATUS_LABELS[convocation.deliveryStatus]}${relance}`;
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
          </li>
        ))}
      </ul>

      <div className="hidden overflow-x-auto sm:block">
        <table className="w-full min-w-[44rem] text-left text-sm">
          <thead className="text-gray-500 dark:text-gray-400">
            <tr className="border-b border-gray-200 dark:border-gray-800">
              <SortableColumnHeader field="LOT" activeField={sortBy} direction={sortDirection} onSort={onSort}>
                Lot
              </SortableColumnHeader>
              <th className="py-2 pr-4 font-normal">Copropriétaire(s)</th>
              <SortableColumnHeader field="WEIGHT" activeField={sortBy} direction={sortDirection} onSort={onSort}>
                Voix
              </SortableColumnHeader>
              <SortableColumnHeader field="SENT_AT" activeField={sortBy} direction={sortDirection} onSort={onSort}>
                Envoi
              </SortableColumnHeader>
              <th className="py-2 pr-4 font-normal">Réponse</th>
              <th className="py-2 font-normal">Statut</th>
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
                <td className="py-3 pr-4 text-gray-500 dark:text-gray-400 tabular-nums">
                  {formatWeight(convocation.votingWeight)}
                </td>
                <td className="py-3 pr-4 text-gray-500 dark:text-gray-400">{deliveryLabel(convocation)}</td>
                <td className="py-3 pr-4 text-gray-500 dark:text-gray-400">
                  {ATTENDANCE_REPLY_LABELS[convocation.attendanceReply]}
                </td>
                <td className="py-3">
                  <Badge color={CONVOCATION_STATUS_COLORS[convocation.status]}>
                    {CONVOCATION_STATUS_LABELS[convocation.status]}
                  </Badge>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
