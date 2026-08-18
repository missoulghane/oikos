import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider } from '@/shared/context/ThemeContext';
import { ConvocationConfirmationPage } from '@/features/property-ownership/general-meetings/pages/ConvocationConfirmationPage';
import { getConvocationConfirmation } from '@/features/property-ownership/general-meetings/api/getConvocationConfirmation';
import { confirmConvocation } from '@/features/property-ownership/general-meetings/api/confirmConvocation';
import { getConvocationConfirmationByCode } from '@/features/property-ownership/general-meetings/api/getConvocationConfirmationByCode';
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
vi.mock('@/features/property-ownership/general-meetings/api/getConvocationConfirmationByCode', () => ({
  getConvocationConfirmationByCode: vi.fn(),
}));
vi.mock('@/features/property-ownership/general-meetings/api/confirmConvocationByCode', () => ({
  confirmConvocationByCode: vi.fn(),
}));

const mockedGet = vi.mocked(getConvocationConfirmation);
const mockedConfirm = vi.mocked(confirmConvocation);
const mockedGetByCode = vi.mocked(getConvocationConfirmationByCode);

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
    mockedGetByCode.mockResolvedValue(convocation);
  });

  it('names the meeting and the lot the link concerns', async () => {
    renderPage();

    expect(await screen.findByText(/Résidence Al Amal/)).toBeInTheDocument();
    expect(screen.getByText(/AG ordinaire 2026/)).toBeInTheDocument();
    expect(screen.getByText(/Bâtiment A — Appartement 1/)).toBeInTheDocument();
  });

  it('records an answer without asking for an account', async () => {
    // The whole reason the page exists: no login, no registration, no invitation
    // to create one - the question, the lot's own code, and two buttons.
    mockedConfirm.mockResolvedValue({ ...convocation, attendanceReply: 'ATTENDING', repliedAt: '2026-08-20T09:00:00Z' });
    renderPage();

    await userEvent.type(await screen.findByLabelText(/Code de votre lot/i), 'w754a1');
    await userEvent.click(screen.getByRole('button', { name: /présent/i }));

    await waitFor(() => expect(mockedConfirm).toHaveBeenCalledWith('a-token', 'ATTENDING', 'w754a1'));
    expect(screen.queryByLabelText(/mot de passe/i)).not.toBeInTheDocument();
  });

  it('does not answer on the link alone', async () => {
    // A link is forwarded, printed, left on a table. The code is on the letter, in the
    // hands of whoever answers for the lot. The API refuses without it; the page says so
    // before the click rather than after a rejected answer.
    renderPage();

    expect(await screen.findByRole('button', { name: /présent/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /absent/i })).toBeDisabled();

    await userEvent.type(screen.getByLabelText(/Code de votre lot/i), 'w754');
    expect(screen.getByRole('button', { name: /présent/i })).toBeDisabled();
    expect(mockedConfirm).not.toHaveBeenCalled();
  });

  it('lets a copropriétaire change their mind', async () => {
    mockedGet.mockResolvedValue({ ...convocation, attendanceReply: 'ATTENDING' });
    mockedConfirm.mockResolvedValue({ ...convocation, attendanceReply: 'NOT_ATTENDING' });
    renderPage();

    await userEvent.type(await screen.findByLabelText(/Code de votre lot/i), 'w754a1');
    await userEvent.click(screen.getByRole('button', { name: /absent/i }));

    await waitFor(() => expect(mockedConfirm).toHaveBeenCalledWith('a-token', 'NOT_ATTENDING', 'w754a1'));
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

  it('offers the code form when the URL carries no token', async () => {
    // The paper path: whoever typed the address by hand has no token, only the two codes
    // printed on their letter.
    renderPage('');

    expect(await screen.findByLabelText(/Référence de l'assemblée/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Code de votre lot/i)).toBeInTheDocument();
    expect(mockedGet).not.toHaveBeenCalled();
  });

  it('does not query until both codes are complete', async () => {
    // A wrong pair costs one of the copropriétaire's own attempts against the server's cap -
    // one request per keystroke would burn through it on the way to a correct code.
    renderPage('');

    await userEvent.type(await screen.findByLabelText(/Référence de l'assemblée/i), 'x7k2m');
    expect(screen.getByRole('button', { name: /continuer/i })).toBeDisabled();
    expect(mockedGetByCode).not.toHaveBeenCalled();
  });

  it('confirms through the pair of codes', async () => {
    mockedGetByCode.mockResolvedValue(convocation);
    renderPage('');

    await userEvent.type(await screen.findByLabelText(/Référence de l'assemblée/i), 'x7k2m9');
    await userEvent.type(screen.getByLabelText(/Code de votre lot/i), 'w754a1');
    await userEvent.click(screen.getByRole('button', { name: /continuer/i }));

    await waitFor(() => expect(mockedGetByCode).toHaveBeenCalledWith('x7k2m9', 'w754a1'));
    expect(await screen.findByText(/Bâtiment A — Appartement 1/)).toBeInTheDocument();
  });

  it('does not ask for the code twice on the paper path', async () => {
    // It was already typed to get here, and asking again would read as a refusal of the
    // code the visitor has just entered.
    mockedGetByCode.mockResolvedValue(convocation);
    renderPage('?ag=x7k2m9&code=w754a1');

    expect(await screen.findByRole('button', { name: /présent/i })).toBeEnabled();
    expect(screen.queryByLabelText(/Code de votre lot/i)).not.toBeInTheDocument();
  });

  it('reads both codes straight from the URL, as the QR code supplies them', async () => {
    mockedGetByCode.mockResolvedValue(convocation);
    renderPage('?ag=x7k2m9&code=w754a1');

    await waitFor(() => expect(mockedGetByCode).toHaveBeenCalledWith('x7k2m9', 'w754a1'));
  });
});
