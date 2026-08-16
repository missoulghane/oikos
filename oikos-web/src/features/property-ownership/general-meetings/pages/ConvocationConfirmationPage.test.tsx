import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider } from '@/shared/context/ThemeContext';
import { ConvocationConfirmationPage } from '@/features/property-ownership/general-meetings/pages/ConvocationConfirmationPage';
import { getConvocationConfirmation } from '@/features/property-ownership/general-meetings/api/getConvocationConfirmation';
import { confirmConvocation } from '@/features/property-ownership/general-meetings/api/confirmConvocation';
import type { ConvocationConfirmation } from '@/features/property-ownership/general-meetings/types/convocationConfirmation.types';

// Factory form (not bare automock): a bare `vi.mock(path)` still loads the
// real module to derive its shape, which would drag in httpClient -> env.ts
// (VITE_API_URL) with no .env available under Vitest.
vi.mock('@/features/property-ownership/general-meetings/api/getConvocationConfirmation', () => ({
  getConvocationConfirmation: vi.fn(),
}));
vi.mock('@/features/property-ownership/general-meetings/api/confirmConvocation', () => ({
  confirmConvocation: vi.fn(),
}));

const mockedGet = vi.mocked(getConvocationConfirmation);
const mockedConfirm = vi.mocked(confirmConvocation);

const convocation: ConvocationConfirmation = {
  propertyName: 'Résidence Al Amal',
  meetingTitle: 'AG ordinaire 2026',
  meetingType: 'ORDINARY',
  scheduledAt: '2026-09-15T17:00:00Z',
  venueType: 'PHYSICAL',
  venueAddress: '12 rue des Orangers',
  venueLink: null,
  unitNumber: 'Appartement 1',
  buildingName: 'Bâtiment A',
  attendanceReply: 'NO_REPLY',
  repliedAt: null,
  stillOpen: true,
};

function renderPage(search = '?token=a-token') {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  // AuthLayout carries the theme toggle, so the provider is part of rendering this page
  // at all - the same wrapper the real router puts above it.
  return render(
    <ThemeProvider>
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[`/convocations/confirmation${search}`]}>
          <ConvocationConfirmationPage />
        </MemoryRouter>
      </QueryClientProvider>
    </ThemeProvider>,
  );
}

describe('ConvocationConfirmationPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedGet.mockResolvedValue(convocation);
  });

  it('names the meeting and the lot the link concerns', async () => {
    renderPage();

    expect(await screen.findByText(/Résidence Al Amal/)).toBeInTheDocument();
    expect(screen.getByText(/AG ordinaire 2026/)).toBeInTheDocument();
    expect(screen.getByText(/Bâtiment A — Appartement 1/)).toBeInTheDocument();
  });

  it('records an answer without asking for an account', async () => {
    // The whole reason the page exists: no login, no registration, no invitation
    // to create one - just the question and two buttons.
    mockedConfirm.mockResolvedValue({ ...convocation, attendanceReply: 'ATTENDING', repliedAt: '2026-08-20T09:00:00Z' });
    renderPage();

    await userEvent.click(await screen.findByRole('button', { name: /présent/i }));

    await waitFor(() => expect(mockedConfirm).toHaveBeenCalledWith('a-token', 'ATTENDING'));
    expect(screen.queryByLabelText(/mot de passe/i)).not.toBeInTheDocument();
  });

  it('lets a copropriétaire change their mind', async () => {
    mockedGet.mockResolvedValue({ ...convocation, attendanceReply: 'ATTENDING' });
    mockedConfirm.mockResolvedValue({ ...convocation, attendanceReply: 'NOT_ATTENDING' });
    renderPage();

    await userEvent.click(await screen.findByRole('button', { name: /absent/i }));

    await waitFor(() => expect(mockedConfirm).toHaveBeenCalledWith('a-token', 'NOT_ATTENDING'));
  });

  it('explains that confirmations are closed once the session has started', async () => {
    // Not a refusal and not a dead end: the visitor followed a link they were given.
    mockedGet.mockResolvedValue({ ...convocation, stillOpen: false });
    renderPage();

    expect(await screen.findByText(/confirmations sont closes/i)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /présent/i })).not.toBeInTheDocument();
  });

  it('explains an invalid link rather than showing an empty page', async () => {
    mockedGet.mockRejectedValue(new Error('404'));
    renderPage();

    expect(await screen.findByText(/Lien non valide/i)).toBeInTheDocument();
  });

  it('says so when the URL carries no token at all', async () => {
    renderPage('');

    expect(await screen.findByText(/lien est incomplet/i)).toBeInTheDocument();
    expect(mockedGet).not.toHaveBeenCalled();
  });
});
