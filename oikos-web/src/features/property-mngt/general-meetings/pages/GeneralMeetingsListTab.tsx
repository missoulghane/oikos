import { useState } from 'react';
import { Link, useOutletContext } from 'react-router-dom';
import type { Property } from '@/features/property-mngt/properties/types/property.types';
import { useGeneralMeetings } from '@/features/property-mngt/general-meetings/hooks/useGeneralMeetings';
import { GeneralMeetingRow } from '@/features/property-mngt/general-meetings/components/GeneralMeetingRow';
import type {
  GeneralMeetingFilters,
  MeetingStatus,
} from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import { MEETING_STATUS_LABELS } from '@/features/property-mngt/general-meetings/constants/generalMeetingLabels';
import { Alert } from '@/shared/components/Alert/Alert';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { Loader } from '@/shared/components/Loader/Loader';
import { Pagination } from '@/shared/components/Pagination/Pagination';
import { Select } from '@/shared/components/Select/Select';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';

const PAGE_SIZE = 10;

const STATUS_VALUES = Object.keys(MEETING_STATUS_LABELS) as MeetingStatus[];

export function GeneralMeetingsListTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState<GeneralMeetingFilters>({});

  const meetings = useGeneralMeetings(property.id, page, PAGE_SIZE, filters);

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white/90">Assemblées générales</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Préparer une AG, convoquer les lots, tenir la séance et publier le procès-verbal.
          </p>
        </div>
        {/* A link to its own route, not a dialog: every creation flow in the app is a
            page, which is also the only shape that works on a phone. */}
        <Link
          to={`/property-mngt/properties/${property.id}/general-meetings/new`}
          className="inline-flex min-h-11 w-fit items-center rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white shadow-theme-xs hover:bg-brand-600"
        >
          Nouvelle assemblée
        </Link>
      </div>

      {/* No wrapping card: the rows are already framed, and nesting a card inside a card
          only added a border and 24px of padding around the filter. */}
      <div className="flex flex-col gap-4">
        <div className="sm:max-w-xs">
          <Select
            label="Statut"
            name="status"
            value={filters.status ?? ''}
            onChange={(event) => {
              const value = event.target.value;
              setPage(0);
              setFilters(value ? { status: value as MeetingStatus } : {});
            }}
          >
            <option value="">Tous les statuts</option>
            {STATUS_VALUES.map((status) => (
              <option key={status} value={status}>
                {MEETING_STATUS_LABELS[status]}
              </option>
            ))}
          </Select>
        </div>

        {meetings.isLoading && <Loader label="Chargement des assemblées…" />}
        {meetings.isError && <Alert message={getErrorMessage(meetings.error)} />}

        {meetings.data && meetings.data.content.length === 0 && (
          <EmptyState title="Aucune assemblée générale">
            Créez une assemblée pour préparer son ordre du jour, puis convoquer les copropriétaires.
          </EmptyState>
        )}

        {meetings.data && meetings.data.content.length > 0 && (
          <ul className="flex flex-col gap-3">
            {meetings.data.content.map((meeting) => (
              <li key={meeting.id}>
                <Link to={`/property-mngt/properties/${property.id}/general-meetings/${meeting.id}`}>
                  <GeneralMeetingRow meeting={meeting} />
                </Link>
              </li>
            ))}
          </ul>
        )}

        {meetings.data && meetings.data.totalPages > 1 && (
          <Pagination pageNumber={page} totalPages={meetings.data.totalPages} onPageChange={setPage} />
        )}
      </div>

    </div>
  );
}
