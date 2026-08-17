import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom';
import { ConvocationsTab } from '@/features/property-mngt/general-meetings/pages/ConvocationsTab';
import { listConvocations } from '@/features/property-mngt/general-meetings/api/listConvocations';
import { listConvocationChannels } from '@/features/property-mngt/general-meetings/api/listConvocationChannels';
import { getAttendanceSummary } from '@/features/property-mngt/general-meetings/api/getAttendanceSummary';
import { sendPendingConvocations } from '@/features/property-mngt/general-meetings/api/sendPendingConvocations';
import type { Convocation } from '@/features/property-mngt/general-meetings/types/convocation.types';
import type { GeneralMeeting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

// Factory form rather than a bare automock: a bare vi.mock still loads the real
// module to derive its shape, dragging httpClient -> env.ts in with it.
vi.mock('@/features/property-mngt/general-meetings/api/listConvocations', () => ({
  listConvocations: vi.fn(),
}));
vi.mock('@/features/property-mngt/general-meetings/api/listConvocationChannels', () => ({
  listConvocationChannels: vi.fn(),
}));
vi.mock('@/features/property-mngt/general-meetings/api/getAttendanceSummary', () => ({
  getAttendanceSummary: vi.fn(),
}));
vi.mock('@/features/property-mngt/general-meetings/api/sendPendingConvocations', () => ({
  sendPendingConvocations: vi.fn(),
}));

const mockedListConvocations = vi.mocked(listConvocations);
const mockedListChannels = vi.mocked(listConvocationChannels);
const mockedSummary = vi.mocked(getAttendanceSummary);
const mockedSendPending = vi.mocked(sendPendingConvocations);

const property = { id: 'property-1', name: 'Résidence Al Amal' } as Property;

const meeting = {
  id: 'meeting-1',
  propertyId: 'property-1',
  status: 'SCHEDULED',
  title: 'AG ordinaire 2026',
  scheduledAt: '2026-09-15T17:00:00Z',
  venueType: 'PHYSICAL',
  venueAddress: '12 rue des Orangers',
  venueLink: null,
  agendaItemCount: 1,
} as GeneralMeeting;

function convocation(overrides: Partial<Convocation> = {}): Convocation {
  return {
    id: crypto.randomUUID(),
    generalMeetingId: 'meeting-1',
    unitId: crypto.randomUUID(),
    unitNumber: 'Appartement 1',
    buildingName: null,
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

function renderTab() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/convocations']}>
        <Routes>
          <Route element={<Outlet context={{ property, meeting }} />}>
            <Route path="/convocations" element={<ConvocationsTab />} />
          </Route>
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  mockedSummary.mockRejectedValue(new Error('not part of these assertions'));
  mockedListChannels.mockResolvedValue([
    { code: 'EMAIL', label: 'Email', automated: true, position: 1 },
    { code: 'APP', label: 'Messagerie interne', automated: true, position: 2 },
    { code: 'POSTAL_MAIL', label: 'Courrier simple', automated: false, position: 3 },
  ]);
  mockedSendPending.mockResolvedValue({ sentCount: 1, failedCount: 0 });
});

describe('ConvocationsTab, while loading', () => {
  it('shows neither panel until it knows which one applies', async () => {
    // Deciding on an empty array while the query is in flight would flash "Générer" at a
    // syndic whose convocations left last week, then swap it under his cursor.
    mockedListConvocations.mockReturnValue(new Promise(() => {}));

    renderTab();

    await waitFor(() => expect(screen.getByText('Chargement des convocations…')).toBeInTheDocument());
    expect(screen.queryByRole('button', { name: 'Générer les convocations' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Envoyer par/ })).not.toBeInTheDocument();
  });
});

describe('ConvocationsTab, before anything is generated', () => {
  it('offers generating and nothing else', async () => {
    // A sending panel saying "générez d'abord" is a step the syndic cannot take,
    // dressed up as one he can.
    mockedListConvocations.mockResolvedValue([]);

    renderTab();

    expect(await screen.findByRole('button', { name: 'Générer les convocations' })).toBeInTheDocument();
    await waitFor(() => expect(screen.getByText('Aucune convocation générée')).toBeInTheDocument());
    expect(screen.queryByRole('button', { name: /Envoyer par/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Relancer/ })).not.toBeInTheDocument();
  });
});

describe('ConvocationsTab, once generated', () => {
  beforeEach(() => {
    mockedListConvocations.mockResolvedValue([
      convocation({ unitNumber: 'Appartement 1' }),
      convocation({
        unitNumber: 'Appartement 2',
        deliveryStatus: 'SENT',
        status: 'SENT',
        sentAt: '2026-12-12T12:30:00Z',
        deliveries: [
          {
            id: 'd1',
            channelCode: 'EMAIL',
            channelLabel: 'Email',
            status: 'SENT',
            sentAt: '2026-12-12T12:30:00Z',
            reference: null,
            reminder: false,
          },
        ],
      }),
    ]);
  });

  it('replaces the generate panel with one send button per automated channel', async () => {
    renderTab();

    expect(await screen.findByRole('button', { name: /Envoyer par email/ })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Envoyer par messagerie interne/ })).toBeInTheDocument();
    // A manual channel gets no button: pressing one must never claim a letter left the building.
    expect(screen.queryByRole('button', { name: /Envoyer par courrier simple/ })).not.toBeInTheDocument();
    // The primary "générer" call to action is gone; only the discreet catch-up remains.
    expect(screen.queryByRole('button', { name: 'Générer les convocations' })).not.toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: 'Générer les convocations des lots ajoutés depuis' }),
    ).toBeInTheDocument();
  });

  it('counts what each channel still has to reach, not what any channel reached', async () => {
    // One lot was emailed, so email has one left and the messagerie still has both.
    renderTab();

    expect(await screen.findByRole('button', { name: 'Envoyer par email (1)' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Envoyer par messagerie interne (2)' })).toBeInTheDocument();
  });

  it('sends on the channel whose button was pressed', async () => {
    const user = userEvent.setup();
    renderTab();

    await user.click(await screen.findByRole('button', { name: /Envoyer par messagerie interne/ }));

    await waitFor(() => expect(mockedSendPending).toHaveBeenCalledWith('meeting-1', 'APP'));
  });

  it('leaves no action button on a row - the detail page carries them', async () => {
    renderTab();

    const table = await screen.findByRole('table');
    expect(within(table).queryByRole('button', { name: 'Envoyer' })).not.toBeInTheDocument();
    expect(within(table).queryByRole('button', { name: 'Marquer remise' })).not.toBeInTheDocument();
    expect(within(table).queryByRole('button', { name: 'Présent' })).not.toBeInTheDocument();
    expect(within(table).queryByRole('button', { name: 'Absent' })).not.toBeInTheDocument();
    // The lot itself is the way in.
    expect(within(table).getByRole('link', { name: 'Appartement 1' })).toBeInTheDocument();
  });

  it('filters the table down and says how much of it is showing', async () => {
    const user = userEvent.setup();
    renderTab();

    await user.click(await screen.findByRole('button', { name: /Filtres/ }));
    await user.selectOptions(screen.getByLabelText('Envoi'), 'SENT');

    await waitFor(() => expect(screen.getByText('1 lot(s) sur 2')).toBeInTheDocument());
    const table = screen.getByRole('table');
    expect(within(table).queryByRole('link', { name: 'Appartement 1' })).not.toBeInTheDocument();
    expect(within(table).getByRole('link', { name: 'Appartement 2' })).toBeInTheDocument();
  });

  it('offers no émargement filter, which would always be empty here', async () => {
    const user = userEvent.setup();
    renderTab();

    await user.click(await screen.findByRole('button', { name: /Filtres/ }));

    expect(screen.getByLabelText('Envoi')).toBeInTheDocument();
    expect(screen.getByLabelText('Réponse')).toBeInTheDocument();
    expect(screen.getByLabelText('Canal emprunté')).toBeInTheDocument();
    expect(screen.queryByLabelText(/Émarg/)).not.toBeInTheDocument();
  });
});
