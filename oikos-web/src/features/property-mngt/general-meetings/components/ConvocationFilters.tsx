import { Select } from '@/shared/components/Select/Select';
import {
  ATTENDANCE_REPLY_LABELS,
  DELIVERY_STATUS_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import type {
  AttendanceReply,
  ConvocationChannel,
  DeliveryStatus,
} from '@/features/property-mngt/general-meetings/types/convocation.types';
import type { ConvocationFiltersValue } from '@/features/property-mngt/general-meetings/utils/filterConvocations';

/**
 * Three dimensions, each mirroring a column of the table below it: Envoi,
 * Réponse, Canal. The free-text search lives on the FilterPanel itself, and
 * sorting on the column headers, as everywhere else in the app.
 *
 * <p>There is deliberately no filter on émargement, although the status badge
 * shows it: nothing is signed in before the session opens, so the choice would
 * only ever return an empty table here. Ticking off the room belongs to the
 * Séance tab.
 */
const SENDING_STATUSES: DeliveryStatus[] = ['TO_SEND', 'SENT', 'FAILED'];
const REPLIES: AttendanceReply[] = ['ATTENDING', 'NOT_ATTENDING', 'NO_REPLY'];

interface ConvocationFiltersProps {
  value: ConvocationFiltersValue;
  channels: ConvocationChannel[];
  onChange: (value: ConvocationFiltersValue) => void;
}

export function ConvocationFilters({ value, channels, onChange }: ConvocationFiltersProps) {
  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
      <Select
        label="Envoi"
        name="deliveryStatus"
        value={value.deliveryStatus}
        onChange={(e) => onChange({ ...value, deliveryStatus: e.target.value as DeliveryStatus | '' })}
      >
        <option value="">Tous</option>
        {SENDING_STATUSES.map((status) => (
          <option key={status} value={status}>
            {DELIVERY_STATUS_LABELS[status]}
          </option>
        ))}
      </Select>
      <Select
        label="Réponse"
        name="attendanceReply"
        value={value.attendanceReply}
        onChange={(e) => onChange({ ...value, attendanceReply: e.target.value as AttendanceReply | '' })}
      >
        <option value="">Toutes</option>
        {REPLIES.map((reply) => (
          <option key={reply} value={reply}>
            {ATTENDANCE_REPLY_LABELS[reply]}
          </option>
        ))}
      </Select>
      {/* Every channel, not only the automated ones: a syndic looks for the lots he posted
          letters to just as often as for the ones the application emailed. Labels come from
          the catalog, so a new channel appears here without touching this file. */}
      <Select
        label="Canal emprunté"
        name="channelCode"
        value={value.channelCode}
        onChange={(e) => onChange({ ...value, channelCode: e.target.value })}
      >
        <option value="">Tous</option>
        {channels.map((channel) => (
          <option key={channel.code} value={channel.code}>
            {channel.label}
          </option>
        ))}
      </Select>
    </div>
  );
}
