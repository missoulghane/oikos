import { useState } from 'react';
import { Link, NavLink, Outlet, useOutletContext, useParams } from 'react-router-dom';
import type { Property } from '@/features/property-mngt/properties/types/property.types';
import { useGeneralMeeting } from '@/features/property-mngt/general-meetings/hooks/useGeneralMeeting';
import {
  MEETING_STATUS_COLORS,
  MEETING_STATUS_LABELS,
  MEETING_TYPE_LABELS,
  VOTING_WEIGHT_MODE_LABELS,
} from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { formatScheduledAt, formatVenue } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { EditMeetingForm } from '@/features/property-mngt/general-meetings/components/EditMeetingForm';
import type { GeneralMeeting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

export interface GeneralMeetingContext {
  property: Property;
  meeting: GeneralMeeting;
}

const TABS = [
  { path: '', label: 'Informations', end: true },
  { path: 'agenda', label: "Ordre du jour" },
  { path: 'convocations', label: 'Convocations' },
  { path: 'session', label: 'Séance' },
  { path: 'minutes', label: 'Procès-verbal' },
];

/**
 * Deliberately thin: a breadcrumb, then everything that identifies the meeting
 * - nature, date, place, and how its voices are counted - on two small lines,
 * then the tabs. No card between the title and the tabs: on a phone it pushed
 * every tab below the fold, and the whole point of tabs is that the content
 * starts straight away.
 */
export function GeneralMeetingDetailLayout() {
  const { property } = useOutletContext<{ property: Property }>();
  const { meetingId } = useParams<{ meetingId: string }>();
  const meeting = useGeneralMeeting(meetingId ?? '');
  const [isEditing, setIsEditing] = useState(false);

  if (meeting.isLoading) {
    return <Loader label="Chargement de l'assemblée…" />;
  }

  if (meeting.isError) {
    return <Alert message={getErrorMessage(meeting.error)} />;
  }

  if (!meeting.data) {
    return null;
  }

  const base = `/property-mngt/properties/${property.id}/general-meetings/${meeting.data.id}`;

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <Link
          to={`/property-mngt/properties/${property.id}/general-meetings`}
          className="text-sm text-gray-500 dark:text-gray-400 hover:underline"
        >
          ← Retour aux assemblées
        </Link>
        <div className="flex flex-wrap items-center gap-2">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white/90">{meeting.data.title}</h2>
          <Badge color={MEETING_STATUS_COLORS[meeting.data.status]}>
            {MEETING_STATUS_LABELS[meeting.data.status]}
          </Badge>
          {/* Editable at any status, on purpose (ADR 0002 §8): correcting an address or
              postponing an hour must not mean recreating the assembly. */}
          {!isEditing && (
            <Button variant="secondary" className="min-h-0 px-2 py-1 text-xs" onClick={() => setIsEditing(true)}>
              Modifier
            </Button>
          )}
        </div>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          AG {MEETING_TYPE_LABELS[meeting.data.meetingType].toLowerCase()} ·{' '}
          {formatScheduledAt(meeting.data.scheduledAt)} · {formatVenue(meeting.data)}
        </p>
        <p className="text-sm text-gray-400 dark:text-gray-500">
          {VOTING_WEIGHT_MODE_LABELS[meeting.data.votingWeightMode]}
          {meeting.data.quorumPercentage > 0
            ? ` · quorum ${meeting.data.quorumPercentage.toLocaleString('fr-FR')} %`
            : ' · aucun quorum requis'}
        </p>
      </div>

      {isEditing && (
        <Card>
          <EditMeetingForm meeting={meeting.data} onDone={() => setIsEditing(false)} />
        </Card>
      )}

      {/* Opening without quorum has legal consequences: it stays visible on every tab of the
          meeting, and is printed in the minutes. */}
      {meeting.data.openedWithoutQuorum && (
        <Alert
          variant="warning"
          message="La séance a été ouverte alors que le quorum n'était pas atteint. Cette mention figure au procès-verbal."
        />
      )}

      {/* Scrolls sideways rather than wrapping onto two rows on a narrow screen. */}
      <nav className="-mx-1 flex gap-1 overflow-x-auto border-b border-gray-200 dark:border-gray-800">
        {TABS.map((tab) => (
          <NavLink
            key={tab.path}
            to={tab.path ? `${base}/${tab.path}` : base}
            end={tab.end}
            className={({ isActive }) =>
              `-mb-px shrink-0 border-b-2 px-3 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? 'border-brand-500 text-brand-500'
                  : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
              }`
            }
          >
            {tab.label}
          </NavLink>
        ))}
      </nav>

      <Outlet context={{ property, meeting: meeting.data } satisfies GeneralMeetingContext} />
    </div>
  );
}
