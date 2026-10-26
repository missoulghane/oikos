import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Outlet, Route, Routes, useLocation } from 'react-router-dom';
import { CreateGeneralMeetingPage } from '@/features/property-mngt/general-meetings/pages/CreateGeneralMeetingPage';
import { createGeneralMeeting } from '@/features/property-mngt/general-meetings/api/createGeneralMeeting';
import { uploadDocument } from '@/features/property-mngt/documents/api/uploadDocument';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

// Factory form, as in MeetingInformationTab.test: a bare vi.mock would still load httpClient.
vi.mock('@/features/property-mngt/general-meetings/api/createGeneralMeeting', () => ({
  createGeneralMeeting: vi.fn(),
}));
vi.mock('@/features/property-mngt/documents/api/uploadDocument', () => ({
  uploadDocument: vi.fn(),
}));

const mockedCreate = vi.mocked(createGeneralMeeting);
const mockedUpload = vi.mocked(uploadDocument);

const property = { id: 'property-1', name: 'Résidence Al Amal' } as Property;

function LandingProbe() {
  const state = useLocation().state as { failedUploads?: string[] } | null;
  return <p>Arrivée : {(state?.failedUploads ?? []).join(' | ') || 'aucun échec'}</p>;
}

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/new']}>
        <Routes>
          <Route element={<Outlet context={{ property }} />}>
            <Route path="/new" element={<CreateGeneralMeetingPage />} />
          </Route>
          <Route
            path="/property-mngt/properties/property-1/general-meetings/meeting-1"
            element={<LandingProbe />}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

async function fillRequiredFields() {
  await userEvent.type(screen.getByLabelText('Intitulé'), 'AG ordinaire 2026');
  await userEvent.type(screen.getByLabelText('Date et heure'), '2026-11-15T18:00');
  await userEvent.type(screen.getByLabelText('Adresse'), '12 rue des Orangers, Casablanca');
}

describe('CreateGeneralMeetingPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedCreate.mockResolvedValue({ id: 'meeting-1' });
  });

  it('sends the comment with the meeting', async () => {
    renderPage();
    await fillRequiredFields();
    const editor = screen.getByLabelText(/Commentaire de l'assemblée/i);
    await userEvent.click(editor);
    await userEvent.type(editor, 'Le budget travaux sera voté.');
    await userEvent.click(screen.getByRole('button', { name: /créer l'assemblée/i }));

    await waitFor(() => expect(mockedCreate).toHaveBeenCalledTimes(1));
    expect(mockedCreate.mock.calls[0][1].comment).toContain('Le budget travaux sera voté.');
    expect(mockedUpload).not.toHaveBeenCalled();
    expect(await screen.findByText('Arrivée : aucun échec')).toBeInTheDocument();
  });

  it('sends no comment when the editor was left empty', async () => {
    renderPage();
    await fillRequiredFields();
    await userEvent.click(screen.getByRole('button', { name: /créer l'assemblée/i }));

    await waitFor(() => expect(mockedCreate).toHaveBeenCalledTimes(1));
    expect(mockedCreate.mock.calls[0][1].comment).toBeNull();
  });

  it('attaches the chosen documents to the new meeting and reports the refused ones on arrival', async () => {
    // One refused file must not cost the others, nor the meeting already created.
    mockedUpload.mockImplementation(async ({ file }) => {
      if (file.name === 'devis.exe') {
        throw new Error('Type de fichier refusé');
      }
      return {} as Awaited<ReturnType<typeof uploadDocument>>;
    });
    renderPage();
    await fillRequiredFields();
    const picker = screen.getByLabelText("Documents de l'assemblée générale");
    await userEvent.upload(picker, new File(['a'], 'budget.pdf', { type: 'application/pdf' }));
    await userEvent.upload(picker, new File(['b'], 'devis.exe', { type: 'application/octet-stream' }));
    // A second pick adds to the first rather than replacing it.
    expect(screen.getByText('budget.pdf')).toBeInTheDocument();
    expect(screen.getByText('devis.exe')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: /créer l'assemblée/i }));

    await waitFor(() => expect(mockedUpload).toHaveBeenCalledTimes(2));
    expect(
      mockedUpload.mock.calls.map(([params]) => [params.ownerType, params.ownerId, params.file.name]),
    ).toEqual([
      ['GENERAL_MEETING', 'meeting-1', 'budget.pdf'],
      ['GENERAL_MEETING', 'meeting-1', 'devis.exe'],
    ]);
    expect(await screen.findByText(/Arrivée : devis\.exe : /)).toBeInTheDocument();
  });

  it('uploads nothing when the meeting could not be created', async () => {
    mockedCreate.mockRejectedValue(new Error('Refusé'));
    renderPage();
    await fillRequiredFields();
    await userEvent.upload(
      screen.getByLabelText("Documents de l'assemblée générale"),
      new File(['a'], 'budget.pdf', { type: 'application/pdf' }),
    );
    await userEvent.click(screen.getByRole('button', { name: /créer l'assemblée/i }));

    expect(await screen.findByRole('alert')).toBeInTheDocument();
    expect(mockedUpload).not.toHaveBeenCalled();
  });
});
