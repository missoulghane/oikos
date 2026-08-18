import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom';
import { MinutesTab } from '@/features/property-mngt/general-meetings/pages/MinutesTab';
import { getMeetingMinutes } from '@/features/property-mngt/general-meetings/api/getMeetingMinutes';
import { updateMeetingMinutes } from '@/features/property-mngt/general-meetings/api/updateMeetingMinutes';
import type { GeneralMeeting } from '@/features/property-mngt/general-meetings/types/generalMeeting.types';
import type { MeetingMinutes } from '@/features/property-mngt/general-meetings/types/minutes.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

// Factory form, as elsewhere: a bare automock still loads the real module to derive its
// shape, dragging in httpClient -> env.ts.
vi.mock('@/features/property-mngt/general-meetings/api/getMeetingMinutes', () => ({
  getMeetingMinutes: vi.fn(),
}));
vi.mock('@/features/property-mngt/general-meetings/api/updateMeetingMinutes', () => ({
  updateMeetingMinutes: vi.fn(),
}));

const mockedGetMinutes = vi.mocked(getMeetingMinutes);
const mockedUpdate = vi.mocked(updateMeetingMinutes);

const property = { id: 'property-1', name: 'Résidence Al Amal' } as Property;

const meeting = {
  id: 'meeting-1',
  propertyId: 'property-1',
  status: 'CLOSED',
  title: 'AG ordinaire 2026',
  agendaItemCount: 1,
} as GeneralMeeting;

const DRAFT_HTML = '<h1>Procès-verbal — AG ordinaire 2026</h1><p>Assemblée générale ordinaire.</p>';

function minutes(content: string): MeetingMinutes {
  return {
    id: 'minutes-1',
    generalMeetingId: 'meeting-1',
    content,
    status: 'DRAFT',
    publishedAt: null,
    createdDate: '2026-09-16T09:00:00Z',
  };
}

function renderTab() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/meeting']}>
        <Routes>
          <Route element={<Outlet context={{ property, meeting }} />}>
            <Route path="/meeting" element={<MinutesTab />} />
          </Route>
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('MinutesTab', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedGetMinutes.mockResolvedValue(minutes(DRAFT_HTML));
  });

  it('shows the draft as a document, not as its markup', async () => {
    // The draft is composed with headings, bold and lists in it. A textarea handed the
    // syndic "<h1>Procès-verbal — …" to type around, which is asking the wrong person for
    // the wrong thing.
    renderTab();

    const editor = await screen.findByLabelText(/Contenu du procès-verbal/i);
    expect(editor.tagName).not.toBe('TEXTAREA');
    expect(within(editor).getByRole('heading', { name: /Procès-verbal — AG ordinaire 2026/ })).toBeInTheDocument();
    expect(screen.queryByText(/<h1>/)).not.toBeInTheDocument();
  });

  it('offers the headings the document is made of', async () => {
    // The one editor of the app whose toolbar carries them, because it is the one field
    // read back through sanitizeDocumentHtml.
    renderTab();

    await screen.findByLabelText(/Contenu du procès-verbal/i);
    expect(document.querySelector('.ql-header')).not.toBeNull();
  });

  it('saves what was typed, as HTML', async () => {
    mockedUpdate.mockResolvedValue(minutes(DRAFT_HTML));
    renderTab();

    const editor = await screen.findByLabelText(/Contenu du procès-verbal/i);
    await userEvent.click(editor);
    await userEvent.type(editor, 'Le syndic a rappelé le calendrier.');
    await userEvent.click(screen.getByRole('button', { name: 'Enregistrer' }));

    await waitFor(() => expect(mockedUpdate).toHaveBeenCalledTimes(1));
    expect(mockedUpdate.mock.calls[0][1]).toContain('Le syndic a rappelé le calendrier.');
    // The composed structure survives the round trip rather than being flattened to text.
    expect(mockedUpdate.mock.calls[0][1]).toContain('<h1>');
  });
});
