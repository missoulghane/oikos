import { describe, expect, it } from 'vitest';
import type { Convocation, ConvocationDelivery } from '@/features/property-mngt/general-meetings/types/convocation.types';
import {
  DEFAULT_CONVOCATION_FILTERS,
  filterAndSortConvocations,
  pendingCountOn,
  type ConvocationFiltersValue,
} from '@/features/property-mngt/general-meetings/utils/filterConvocations';

function delivery(overrides: Partial<ConvocationDelivery> = {}): ConvocationDelivery {
  return {
    id: crypto.randomUUID(),
    channelCode: 'EMAIL',
    channelLabel: 'Email',
    status: 'SENT',
    sentAt: '2026-12-12T12:30:00Z',
    reference: null,
    reminder: false,
    ...overrides,
  };
}

function convocation(overrides: Partial<Convocation> = {}): Convocation {
  return {
    id: crypto.randomUUID(),
    generalMeetingId: 'meeting-1',
    unitId: crypto.randomUUID(),
    unitNumber: 'Appartement 1',
    buildingName: 'Bâtiment A',
    recipients: [{ fullName: 'Rachid Tazi', email: 'rachid@example.com' }],
    votingWeight: 120,
    deliveries: [],
    sentAt: null,
    deliveryStatus: 'TO_SEND',
    attendanceReply: 'NO_REPLY',
    repliedAt: null,
    replySource: null,
    replyNote: null,
    checkedIn: false,
    attendanceMode: null,
    checkedInAt: null,
    status: 'TO_SEND',
    meetingPublicReference: null,
    confirmationCode: null,
    ...overrides,
  } as Convocation;
}

function withFilters(overrides: Partial<ConvocationFiltersValue>): ConvocationFiltersValue {
  return { ...DEFAULT_CONVOCATION_FILTERS, ...overrides };
}

function lotsOf(convocations: Convocation[]): (string | null)[] {
  return convocations.map((c) => c.unitNumber);
}

describe('filterAndSortConvocations', () => {
  it('keeps every lot when nothing is filtered', () => {
    const rows = [convocation(), convocation({ unitNumber: 'Appartement 2' })];

    expect(filterAndSortConvocations(rows, DEFAULT_CONVOCATION_FILTERS)).toHaveLength(2);
  });

  it('sorts lots the way a human numbers them, not the way a string sorts', () => {
    // The reason for numeric collation: plain string order puts 10 before 2.
    const rows = [
      convocation({ unitNumber: 'Appartement 10' }),
      convocation({ unitNumber: 'Appartement 2' }),
      convocation({ unitNumber: 'Appartement 1' }),
    ];

    expect(lotsOf(filterAndSortConvocations(rows, DEFAULT_CONVOCATION_FILTERS))).toEqual([
      'Appartement 1',
      'Appartement 2',
      'Appartement 10',
    ]);
  });

  it('filters on the sending and on the reply independently', () => {
    const silent = convocation({ unitNumber: 'A1', deliveryStatus: 'SENT', attendanceReply: 'NO_REPLY' });
    const answered = convocation({ unitNumber: 'A2', deliveryStatus: 'SENT', attendanceReply: 'ATTENDING' });
    const unreachable = convocation({ unitNumber: 'A3', deliveryStatus: 'FAILED' });
    const rows = [silent, answered, unreachable];

    expect(lotsOf(filterAndSortConvocations(rows, withFilters({ deliveryStatus: 'FAILED' })))).toEqual(['A3']);
    expect(lotsOf(filterAndSortConvocations(rows, withFilters({ attendanceReply: 'NO_REPLY' })))).toEqual(['A1', 'A3']);
  });

  it('offers no way to filter on émargement', () => {
    // Deliberate: nobody has signed in before the session opens, so the choice would
    // always return nothing here. The badge still shows it; the filter does not exist.
    expect(Object.keys(DEFAULT_CONVOCATION_FILTERS)).not.toContain('checkedIn');
    expect(Object.keys(DEFAULT_CONVOCATION_FILTERS)).not.toContain('status');
  });

  it('finds a lot by a channel it failed on, not only one it succeeded on', () => {
    // "Which lots did the email run touch" includes the ones it could not reach -
    // those are precisely the rows worth opening.
    const bounced = convocation({
      unitNumber: 'A1',
      deliveries: [delivery({ status: 'FAILED', sentAt: null })],
    });
    const posted = convocation({
      unitNumber: 'A2',
      deliveries: [delivery({ channelCode: 'POSTAL_MAIL', channelLabel: 'Courrier simple' })],
    });

    expect(lotsOf(filterAndSortConvocations([bounced, posted], withFilters({ channelCode: 'EMAIL' })))).toEqual(['A1']);
  });

  it('searches the lot and its owners, ignoring case and accents', () => {
    const tazi = convocation({ unitNumber: 'A1', recipients: [{ fullName: 'Rachid Tazi', email: null }] });
    const sefriou = convocation({
      unitNumber: 'A2',
      buildingName: 'Résidence Béta',
      recipients: [{ fullName: 'Hicham Sefriou', email: null }],
    });
    const rows = [tazi, sefriou];

    expect(lotsOf(filterAndSortConvocations(rows, withFilters({ search: 'tazi' })))).toEqual(['A1']);
    expect(lotsOf(filterAndSortConvocations(rows, withFilters({ search: 'residence beta' })))).toEqual(['A2']);
  });

  it('leaves lots that never went out at the bottom of a date sort, both ways', () => {
    // They have no date at all; letting them lead a descending sort would bury the
    // recent sends the column was clicked for.
    const early = convocation({ unitNumber: 'A1', sentAt: '2026-12-12T12:30:00Z' });
    const late = convocation({ unitNumber: 'A2', sentAt: '2026-12-14T12:30:00Z' });
    const never = convocation({ unitNumber: 'A3', sentAt: null });
    const rows = [never, late, early];

    expect(lotsOf(filterAndSortConvocations(rows, withFilters({ sortBy: 'SENT_AT', sortDirection: 'ASC' })))).toEqual([
      'A1',
      'A2',
      'A3',
    ]);
    expect(lotsOf(filterAndSortConvocations(rows, withFilters({ sortBy: 'SENT_AT', sortDirection: 'DESC' })))).toEqual([
      'A2',
      'A1',
      'A3',
    ]);
  });

  it('breaks ties on the lot so a run sent at once keeps a readable order', () => {
    const sameInstant = '2026-12-12T12:30:00Z';
    const rows = [
      convocation({ unitNumber: 'Appartement 3', sentAt: sameInstant }),
      convocation({ unitNumber: 'Appartement 1', sentAt: sameInstant }),
      convocation({ unitNumber: 'Appartement 2', sentAt: sameInstant }),
    ];

    expect(lotsOf(filterAndSortConvocations(rows, withFilters({ sortBy: 'SENT_AT' })))).toEqual([
      'Appartement 1',
      'Appartement 2',
      'Appartement 3',
    ]);
  });

  it('sorts on voting weight as a number', () => {
    const rows = [
      convocation({ unitNumber: 'A1', votingWeight: 80 }),
      convocation({ unitNumber: 'A2', votingWeight: 120 }),
      convocation({ unitNumber: 'A3', votingWeight: 9 }),
    ];

    expect(lotsOf(filterAndSortConvocations(rows, withFilters({ sortBy: 'WEIGHT', sortDirection: 'DESC' })))).toEqual([
      'A2',
      'A1',
      'A3',
    ]);
  });
});

describe('pendingCountOn', () => {
  it('counts what one channel still has to reach, not what any channel reached', () => {
    // The whole point of the per-channel buttons: a lot already emailed is still
    // waiting on the messagerie, and each button has to offer its own population.
    const emailed = convocation({ deliveries: [delivery()] });
    const untouched = convocation({ deliveries: [] });

    expect(pendingCountOn([emailed, untouched], 'EMAIL')).toBe(1);
    expect(pendingCountOn([emailed, untouched], 'APP')).toBe(2);
  });

  it('still counts a lot whose only attempt on that channel failed', () => {
    const bounced = convocation({ deliveries: [delivery({ status: 'FAILED', sentAt: null })] });

    expect(pendingCountOn([bounced], 'EMAIL')).toBe(1);
  });

  it('does not count a lot reached by a reminder on that channel', () => {
    // A reminder went out by that channel, which is all this question asks.
    const chased = convocation({ deliveries: [delivery({ reminder: true })] });

    expect(pendingCountOn([chased], 'EMAIL')).toBe(0);
  });
});
