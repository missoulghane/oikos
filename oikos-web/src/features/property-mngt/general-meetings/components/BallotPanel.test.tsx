import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BallotPanel } from '@/features/property-mngt/general-meetings/components/BallotPanel';
import { listVotes } from '@/features/property-mngt/general-meetings/api/listVotes';
import { castVote } from '@/features/property-mngt/general-meetings/api/castVote';
import { getAgendaItemResult } from '@/features/property-mngt/general-meetings/api/getAgendaItemResult';
import type { AgendaItem } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import type { Convocation } from '@/features/property-mngt/general-meetings/types/convocation.types';
import type { Vote } from '@/features/property-mngt/general-meetings/types/vote.types';

// Factory form, like the confirmation page's own test: a bare automock still loads the
// real module to derive its shape, dragging in httpClient -> env.ts with no .env here.
vi.mock('@/features/property-mngt/general-meetings/api/listVotes', () => ({ listVotes: vi.fn() }));
vi.mock('@/features/property-mngt/general-meetings/api/castVote', () => ({ castVote: vi.fn() }));
vi.mock('@/features/property-mngt/general-meetings/api/getAgendaItemResult', () => ({
  getAgendaItemResult: vi.fn(),
}));
vi.mock('@/features/property-mngt/general-meetings/api/openVoteSession', () => ({ openVoteSession: vi.fn() }));
vi.mock('@/features/property-mngt/general-meetings/api/closeVoteSession', () => ({ closeVoteSession: vi.fn() }));
vi.mock('@/features/property-mngt/general-meetings/api/recordShowOfHands', () => ({ recordShowOfHands: vi.fn() }));

const mockedListVotes = vi.mocked(listVotes);
const mockedCastVote = vi.mocked(castVote);
const mockedResult = vi.mocked(getAgendaItemResult);

const item: AgendaItem = {
  id: 'item-1',
  generalMeetingId: 'meeting-1',
  label: 'Approbation des comptes 2025',
  description: null,
  position: 1,
  majorityRule: 'SIMPLE',
  voteSessionStatus: 'OPEN',
  createdDate: '2026-08-01T10:00:00Z',
};

function convocation(id: string, unitId: string, unitNumber: string, checkedIn: boolean): Convocation {
  return {
    id,
    generalMeetingId: 'meeting-1',
    unitId,
    unitNumber,
    buildingName: 'Bâtiment A',
    recipients: [],
    votingWeight: 120,
    deliveries: [],
    replies: [],
    sentAt: null,
    deliveryStatus: 'SENT',
    attendanceReply: 'ATTENDING',
    repliedAt: null,
    replySource: null,
    replyMediumCode: null,
    replyMediumLabel: null,
    replyNote: null,
    replyAttendanceMode: null,
    replyByProxy: false,
    checkedIn,
    attendanceMode: checkedIn ? 'ON_SITE' : null,
    checkedInAt: null,
    status: 'CONFIRMED',
    meetingPublicReference: null,
    confirmationCode: null,
  };
}

const rows = [convocation('c-1', 'unit-1', 'Appartement 1', true), convocation('c-2', 'unit-2', 'Appartement 2', true)];

const vote: Vote = {
  id: 'vote-1',
  agendaItemId: 'item-1',
  unitId: 'unit-1',
  unitNumber: 'Appartement 1',
  buildingName: 'Bâtiment A',
  votingWeight: 120,
  choice: 'FOR',
  castAt: '2026-09-15T18:00:00Z',
};

function renderPanel(overrides: Partial<AgendaItem> = {}) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <BallotPanel
        meetingId="meeting-1"
        item={{ ...item, ...overrides }}
        position={1}
        convocations={rows}
        sessionInProgress
      />
    </QueryClientProvider>,
  );
}

describe('BallotPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedListVotes.mockResolvedValue([]);
    mockedResult.mockRejectedValue(new Error('no result yet'));
  });

  it('offers the roll call as a control, not as a line of text', async () => {
    // It sat under the show-of-hands buttons as a bare text link, and the ordinary way to
    // hold a ballot read as a footnote to the shortcut.
    renderPanel();

    expect(await screen.findByRole('button', { name: /détail des votes/i })).toBeInTheDocument();
  });

  it('marks what each lot has on record', async () => {
    // A roll is read down the list and voted across it: without the mark, the only way to
    // know whether a lot had already been called was to remember.
    mockedListVotes.mockResolvedValue([vote]);
    renderPanel();

    await userEvent.click(await screen.findByRole('button', { name: /détail des votes/i }));

    const voted = (await screen.findByText(/Appartement 1/)).closest('li')!;
    const notVoted = screen.getByText(/Appartement 2/).closest('li')!;
    // By the badge, not by the button of the same name that sits in the same row.
    expect(within(voted).getByText('Pour', { selector: 'span' })).toBeInTheDocument();
    expect(within(notVoted).getByText(/Pas encore voté/)).toBeInTheDocument();
  });

  it('records a vote and keeps showing it', async () => {
    renderPanel();

    await userEvent.click(await screen.findByRole('button', { name: /détail des votes/i }));
    mockedCastVote.mockResolvedValue(vote);
    mockedListVotes.mockResolvedValue([vote]);
    const lot = (await screen.findByText(/Appartement 1/)).closest('li')!;
    await userEvent.click(within(lot).getByRole('button', { name: 'Pour' }));

    await waitFor(() => expect(mockedCastVote).toHaveBeenCalledWith('item-1', 'unit-1', 'FOR'));
    await waitFor(() => expect(within(lot).getByText('Pour', { selector: 'span' })).toBeInTheDocument());
  });

  it('still shows who voted what once the ballot is closed', async () => {
    // Closing settles the votes, it does not put them away: the roll is what a contested
    // result is checked against, and what the minutes are written from.
    mockedListVotes.mockResolvedValue([vote]);
    renderPanel({ voteSessionStatus: 'CLOSED' });

    await userEvent.click(await screen.findByRole('button', { name: /déplier/i }));
    await userEvent.click(screen.getByRole('button', { name: /détail des votes/i }));

    const voted = (await screen.findByText(/Appartement 1/)).closest('li')!;
    expect(within(voted).getByText('Pour', { selector: 'span' })).toBeInTheDocument();
    // Read-only: the API refuses a vote on a closed session, so the three buttons that could
    // only fail are gone - and so is the show of hands.
    expect(within(voted).queryByRole('button', { name: 'Contre' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /tous pour/i })).not.toBeInTheDocument();
    expect(screen.getByText(/N’a pas voté/)).toBeInTheDocument();
  });
});
