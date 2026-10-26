import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { PropertyDocumentsTab } from '@/features/property-mngt/documents/pages/PropertyDocumentsTab';
import { listDocuments } from '@/features/property-mngt/documents/api/listDocuments';
import { uploadDocument } from '@/features/property-mngt/documents/api/uploadDocument';
import { downloadDocument } from '@/features/property-mngt/documents/api/downloadDocument';
import type { Document } from '@/features/property-mngt/documents/types/document.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

// Factory form (not bare automock): a bare `vi.mock(path)` still loads the
// real module to derive its shape, which would drag in httpClient -> env.ts
// (VITE_API_URL) with no .env available under Vitest.
vi.mock('@/features/property-mngt/documents/api/listDocuments', () => ({ listDocuments: vi.fn() }));
vi.mock('@/features/property-mngt/documents/api/uploadDocument', () => ({ uploadDocument: vi.fn() }));
vi.mock('@/features/property-mngt/documents/api/downloadDocument', () => ({ downloadDocument: vi.fn() }));
vi.mock('@/features/property-mngt/documents/api/deleteDocument', () => ({ deleteDocument: vi.fn() }));
vi.mock('@/features/identity/me', () => ({
  useCurrentUser: () => ({ data: { id: 'user-1' } }),
  canReadDocuments: () => true,
  canWriteDocuments: () => true,
}));
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useOutletContext: () => ({ property }) };
});

const property = { id: 'property-1' } as Property;
const mockedList = vi.mocked(listDocuments);
const mockedUpload = vi.mocked(uploadDocument);
const mockedDownload = vi.mocked(downloadDocument);

const reglement: Document = {
  id: 'doc-1',
  ownerType: 'PROPERTY',
  ownerId: 'property-1',
  fileName: 'scan-001.pdf',
  title: 'Règlement de copropriété',
  contentType: 'application/pdf',
  sizeBytes: 2048,
  uploadedBy: 'user-1',
  uploadedAt: '2026-10-01T10:00:00Z',
};

function renderTab() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <PropertyDocumentsTab />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('PropertyDocumentsTab', () => {
  beforeEach(() => {
    mockedList.mockReset();
    mockedUpload.mockReset();
    mockedDownload.mockReset();
    mockedList.mockResolvedValue({ content: [reglement], pageNumber: 0, pageSize: 10, totalElements: 1, totalPages: 1 });
    mockedUpload.mockResolvedValue(reglement);
    mockedDownload.mockResolvedValue(undefined);
  });

  it('shows the title, with the file name beside it, and downloads on a click on the title', async () => {
    const user = userEvent.setup();
    renderTab();

    const title = await screen.findByRole('button', { name: 'Règlement de copropriété' });
    expect(screen.getByText(/scan-001\.pdf/)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Télécharger' })).not.toBeInTheDocument();

    await user.click(title);

    await waitFor(() => expect(mockedDownload).toHaveBeenCalled());
    expect(mockedDownload.mock.calls[0].slice(0, 2)).toEqual(['doc-1', 'scan-001.pdf']);
  });

  it('searches from the first page once typing pauses', async () => {
    const user = userEvent.setup();
    renderTab();
    await screen.findByRole('button', { name: 'Règlement de copropriété' });

    await user.type(screen.getByLabelText('Rechercher un document'), 'budget');

    await waitFor(() =>
      expect(mockedList).toHaveBeenLastCalledWith(
        expect.objectContaining({ ownerId: 'property-1', page: 0, size: 10, search: 'budget' }),
      ),
    );
  });

  it('sends each file with the title typed for it, under the « Ajouter » wording', async () => {
    const user = userEvent.setup();
    renderTab();

    const file = new File(['%PDF'], 'scan-002.pdf', { type: 'application/pdf' });
    await user.upload(screen.getByLabelText('Choisir des documents'), file);
    await user.type(screen.getByLabelText('Titre de scan-002.pdf'), 'Budget 2026');
    await user.click(screen.getByRole('button', { name: 'Ajouter' }));

    await waitFor(() => expect(mockedUpload).toHaveBeenCalled());
    expect(mockedUpload.mock.calls[0][0]).toEqual(
      expect.objectContaining({ ownerType: 'PROPERTY', ownerId: 'property-1', file, title: 'Budget 2026' }),
    );
  });
});
