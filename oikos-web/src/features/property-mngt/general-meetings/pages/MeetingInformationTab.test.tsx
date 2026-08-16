import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom';
import { MeetingInformationTab } from '@/features/property-mngt/general-meetings/pages/MeetingInformationTab';
import { updateGeneralMeetingComment } from '@/features/property-mngt/general-meetings/api/updateGeneralMeetingComment';
import { listDocuments } from '@/features/property-mngt/documents/api/listDocuments';
import { useCurrentUser } from '@/features/identity/me';
import type { GeneralMeeting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

// Factory form (not bare automock): a bare `vi.mock(path)` still loads the
// real module to derive its shape, which would drag in httpClient -> env.ts
// (VITE_API_URL) with no .env available under Vitest.
vi.mock('@/features/property-mngt/general-meetings/api/updateGeneralMeetingComment', () => ({
  updateGeneralMeetingComment: vi.fn(),
}));
vi.mock('@/features/property-mngt/documents/api/listDocuments', () => ({
  listDocuments: vi.fn(),
}));
vi.mock('@/features/identity/me', () => ({
  useCurrentUser: vi.fn(),
  canWriteDocuments: () => true,
  canReadDocuments: () => true,
}));

const mockedUpdateComment = vi.mocked(updateGeneralMeetingComment);
const mockedListDocuments = vi.mocked(listDocuments);
const mockedUseCurrentUser = vi.mocked(useCurrentUser);

const property = { id: 'property-1', name: 'Résidence Al Amal' } as Property;

const meeting = {
  id: 'meeting-1',
  propertyId: 'property-1',
  propertyName: 'Résidence Al Amal',
  meetingType: 'ORDINARY',
  status: 'SCHEDULED',
  title: 'AG ordinaire 2026',
  scheduledAt: '2026-09-15T17:00:00Z',
  venueType: 'PHYSICAL',
  venueAddress: '12 rue des Orangers',
  venueLink: null,
  quorumPercentage: 0,
  votingWeightMode: 'PER_UNIT',
  comment: null,
  openedWithoutQuorum: false,
  agendaItemCount: 1,
  createdDate: '2026-08-01T10:00:00Z',
} as GeneralMeeting;

function renderTab(overrides: Partial<GeneralMeeting> = {}) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/meeting']}>
        <Routes>
          <Route element={<Outlet context={{ property, meeting: { ...meeting, ...overrides } }} />}>
            <Route path="/meeting" element={<MeetingInformationTab />} />
          </Route>
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('MeetingInformationTab', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedListDocuments.mockResolvedValue({
      content: [],
      pageNumber: 0,
      pageSize: 20,
      totalElements: 0,
      totalPages: 0,
    });
    mockedUseCurrentUser.mockReturnValue({ data: { id: 'user-1' } } as ReturnType<typeof useCurrentUser>);
  });

  it('warns that the comment is read by the copropriétaires', async () => {
    // The one thing a syndic must not get wrong here: this is not an internal note.
    renderTab();

    expect(await screen.findByText(/Lu par les copropriétaires/i)).toBeInTheDocument();
  });

  it('saves the comment through its own endpoint', async () => {
    // Its own endpoint, so that saving a comment cannot carry a stale date and venue with it.
    mockedUpdateComment.mockResolvedValue({ ...meeting, comment: '<p>Note</p>' });
    renderTab();

    const editor = await screen.findByLabelText(/Commentaire de l'assemblée/i);
    await userEvent.click(editor);
    await userEvent.type(editor, 'Le budget est joint.');
    await userEvent.click(screen.getByRole('button', { name: /enregistrer le commentaire/i }));

    await waitFor(() => expect(mockedUpdateComment).toHaveBeenCalledTimes(1));
    expect(mockedUpdateComment.mock.calls[0][0]).toBe('meeting-1');
    expect(mockedUpdateComment.mock.calls[0][1]).toContain('Le budget est joint.');
  });

  it('lists the meeting attachments, not one point’s', async () => {
    renderTab();

    await waitFor(() =>
      expect(mockedListDocuments).toHaveBeenCalledWith(
        expect.objectContaining({ ownerType: 'GENERAL_MEETING', ownerId: 'meeting-1' }),
      ),
    );
  });

  it('offers to clear an existing comment rather than hiding the field', async () => {
    // Clearing and saving is how a comment is removed - the API normalises blank to null,
    // so there is no separate delete.
    renderTab({ comment: '<p>Une note</p>' });

    expect(await screen.findByRole('button', { name: /effacer/i })).toBeInTheDocument();
  });
});
