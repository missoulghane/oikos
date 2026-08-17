import type {
  AttendanceReply,
  ChannelCode,
  Convocation,
  DeliveryStatus,
} from '@/features/property-mngt/general-meetings/types/convocation.types';
import { formatLotLabel } from '@/features/property-mngt/general-meetings/utils/formatMeeting';
import { matchesSearch } from '@/shared/utils/normalizeForSearch';
import type { SortDirection } from '@/shared/utils/sorting';

export type ConvocationSortField = 'LOT' | 'SENT_AT' | 'WEIGHT';

export interface ConvocationFiltersValue {
  /**
   * Where the sending stands, not the convocation's synthetic status. The two
   * differ on purpose: the synthetic one also carries "émargé", which means
   * nothing in this tab - nobody has signed in before the session opens, and
   * ticking off the room belongs to the Séance tab. Filtering on the sending
   * and on the reply separately covers everything a syndic looks for here.
   */
  deliveryStatus: DeliveryStatus | '';
  attendanceReply: AttendanceReply | '';
  channelCode: ChannelCode | '';
  search: string;
  sortBy: ConvocationSortField;
  sortDirection: SortDirection;
}

/** Ascending by lot: the table is scanned lot by lot looking for the silent ones. */
export const DEFAULT_CONVOCATION_FILTERS: ConvocationFiltersValue = {
  deliveryStatus: '',
  attendanceReply: '',
  channelCode: '',
  search: '',
  sortBy: 'LOT',
  sortDirection: 'ASC',
};

/** Sorting always holds a value, so it would inflate the "active filters" count. */
export const CONVOCATION_SORT_KEYS = ['sortBy', 'sortDirection'] as const;

function lotLabelOf(convocation: Convocation): string {
  return formatLotLabel(convocation.unitNumber, convocation.buildingName);
}

/**
 * Lot, then everyone it would be addressed to. An unowned lot is searchable by
 * its number alone, which is all it has.
 */
function haystackOf(convocation: Convocation): string {
  return [lotLabelOf(convocation), ...convocation.recipients.map((recipient) => recipient.fullName)].join(' ');
}

function matches(convocation: Convocation, filters: ConvocationFiltersValue): boolean {
  if (filters.deliveryStatus && convocation.deliveryStatus !== filters.deliveryStatus) {
    return false;
  }
  if (filters.attendanceReply && convocation.attendanceReply !== filters.attendanceReply) {
    return false;
  }
  // Failures count as "went through this channel" too: what the syndic asks when picking
  // Email is "which lots did the email run touch", and the ones it could not reach are
  // exactly the rows worth looking at.
  if (filters.channelCode && !convocation.deliveries.some((d) => d.channelCode === filters.channelCode)) {
    return false;
  }
  return !filters.search || matchesSearch(haystackOf(convocation), filters.search);
}

/**
 * Lots never sent sort last whichever way the date runs. They have no date at
 * all, and letting them lead a descending sort would bury the recent sends the
 * column was clicked for - "à envoyer" is a filter, not a date.
 */
function compareSentAt(a: Convocation, b: Convocation, direction: SortDirection): number {
  if (!a.sentAt || !b.sentAt) {
    return a.sentAt ? -1 : b.sentAt ? 1 : 0;
  }
  const chronological = a.sentAt.localeCompare(b.sentAt);
  return direction === 'ASC' ? chronological : -chronological;
}

function compare(a: Convocation, b: Convocation, filters: ConvocationFiltersValue): number {
  const byLot = lotLabelOf(a).localeCompare(lotLabelOf(b), 'fr', { numeric: true });
  switch (filters.sortBy) {
    case 'SENT_AT': {
      const bySentAt = compareSentAt(a, b, filters.sortDirection);
      // Lot order as the tiebreak, so lots sent in the same run keep a stable,
      // readable order instead of whatever the server returned.
      return bySentAt !== 0 ? bySentAt : byLot;
    }
    case 'WEIGHT': {
      const byWeight = a.votingWeight - b.votingWeight;
      const directed = filters.sortDirection === 'ASC' ? byWeight : -byWeight;
      return directed !== 0 ? directed : byLot;
    }
    default:
      // "Appartement 10" must come after "Appartement 2", hence numeric collation.
      return filters.sortDirection === 'ASC' ? byLot : -byLot;
  }
}

/**
 * Filtering, sorting and paging happen here rather than on the server, and that
 * is a deliberate exception to how the Échéances list works.
 *
 * <p>The set is one row per lot of a single copropriété, already fetched whole:
 * ListConvocationsByMeetingService is unpaged by design, because the status it
 * would be filtered on is derived from the deliveries rather than stored, and no
 * SQL predicate can express it. Re-deriving that server-side to page a few dozen
 * rows would buy nothing a syndic could see. Past roughly a thousand lots in one
 * copropriété this needs revisiting - and what needs rethinking then is the
 * derived status, not the paging.
 */
export function filterAndSortConvocations(
  convocations: Convocation[],
  filters: ConvocationFiltersValue,
): Convocation[] {
  return convocations.filter((convocation) => matches(convocation, filters)).sort((a, b) => compare(a, b, filters));
}

/**
 * How many lots this channel still has to reach - the count each send button
 * carries. Deliberately asked of one channel rather than of the convocation as
 * a whole: since the syndic presses one button per channel, a lot already
 * emailed is still waiting on the messagerie. Mirrors Convocation.hasBeenSentBy
 * server-side, and the two must stay in step or a button will offer a number
 * the run does not act on.
 */
export function pendingCountOn(convocations: Convocation[], channelCode: ChannelCode): number {
  return convocations.filter(
    (convocation) =>
      !convocation.deliveries.some((delivery) => delivery.status === 'SENT' && delivery.channelCode === channelCode),
  ).length;
}
