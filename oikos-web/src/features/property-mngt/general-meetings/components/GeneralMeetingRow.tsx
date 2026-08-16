import type { GeneralMeeting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import {
  MEETING_STATUS_COLORS,
  MEETING_STATUS_LABELS,
  MEETING_TYPE_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { formatScheduledAt, formatVenue } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { Badge } from '@/shared/components/Badge/Badge';

export function GeneralMeetingRow({ meeting }: { meeting: GeneralMeeting }) {
  return (
    <div className="flex flex-col gap-2 rounded-xl border border-gray-200 dark:border-gray-800 p-4 transition-colors hover:bg-gray-50 dark:hover:bg-white/[0.03] sm:flex-row sm:items-center sm:justify-between">
      <div className="flex flex-col gap-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-medium text-gray-900 dark:text-white/90">{meeting.title}</span>
          <Badge color={MEETING_STATUS_COLORS[meeting.status]}>{MEETING_STATUS_LABELS[meeting.status]}</Badge>
        </div>
        <span className="text-sm text-gray-500 dark:text-gray-400">
          AG {MEETING_TYPE_LABELS[meeting.meetingType].toLowerCase()} · {formatScheduledAt(meeting.scheduledAt)}
        </span>
        <span className="text-sm text-gray-400 dark:text-gray-500">{formatVenue(meeting)}</span>
      </div>
      <span className="text-sm text-gray-500 dark:text-gray-400">
        {meeting.agendaItemCount} point{meeting.agendaItemCount > 1 ? 's' : ''} à l'ordre du jour
      </span>
    </div>
  );
}
