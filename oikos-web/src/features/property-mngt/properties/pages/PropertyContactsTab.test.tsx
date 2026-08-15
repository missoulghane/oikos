import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { PropertyContactsTab } from '@/features/property-mngt/properties/pages/PropertyContactsTab';
import { getPropertyContacts } from '@/features/property-mngt/properties/api/getPropertyContacts';
import type { Property, PropertyContact } from '@/features/property-mngt/properties/types/property.types';

// Factory form (not bare automock): a bare `vi.mock(path)` still loads the
// real module to derive its shape, which would drag in httpClient -> env.ts
// (VITE_API_URL) with no .env available under Vitest.
vi.mock('@/features/property-mngt/properties/api/getPropertyContacts', () => ({
  getPropertyContacts: vi.fn(),
}));
vi.mock('@/features/property-mngt/parties/hooks/useBulkInviteParties', () => ({
  useBulkInviteParties: () => ({ mutate: vi.fn(), isPending: false, isSuccess: false, isError: false }),
}));

// PropertyContactsTab reads its property from the router outlet context; the
// test route below renders it directly, so stub that hook rather than building
// a whole nested layout route just for the id.
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useOutletContext: () => ({ property }) };
});

const mockedGetPropertyContacts = vi.mocked(getPropertyContacts);

const property = { id: 'property-1' } as Property;

const linkedContact: PropertyContact = {
  id: 'ownership-1',
  partyId: 'party-1',
  partyFullName: 'Jean Dupont',
  partyType: 'INDIVIDUAL',
  partyEmail: 'jean.dupont@example.com',
  partyPhone: '0601020304',
  unitId: 'unit-1',
  unitNumber: 'A12',
  buildingName: 'Bâtiment A',
  ownershipShare: 100,
  hasLinkedAccount: true,
};

function renderTab() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/contacts']}>
        <Routes>
          <Route element={<PropertyContactsTab />} path="/contacts" />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('PropertyContactsTab', () => {
  beforeEach(() => {
    mockedGetPropertyContacts.mockReset();
    mockedGetPropertyContacts.mockResolvedValue({
      content: [linkedContact],
      pageNumber: 0,
      pageSize: 5,
      totalElements: 1,
      totalPages: 1,
    });
  });

  it('lists contacts without any account filter on first render', async () => {
    renderTab();

    expect(await screen.findByText(/Jean Dupont/)).toBeInTheDocument();
    expect(mockedGetPropertyContacts).toHaveBeenCalledWith(
      expect.objectContaining({ propertyId: 'property-1', hasLinkedAccount: undefined }),
    );
  });

  it('asks for the contacts without an account when "Sans compte" is picked', async () => {
    const user = userEvent.setup();
    renderTab();
    await screen.findByText(/Jean Dupont/);

    await user.selectOptions(screen.getByLabelText('Compte utilisateur'), 'NOT_LINKED');

    await waitFor(() =>
      expect(mockedGetPropertyContacts).toHaveBeenCalledWith(expect.objectContaining({ hasLinkedAccount: false })),
    );
  });

  it('asks for the contacts with an active account when "Compte actif" is picked', async () => {
    const user = userEvent.setup();
    renderTab();
    await screen.findByText(/Jean Dupont/);

    await user.selectOptions(screen.getByLabelText('Compte utilisateur'), 'LINKED');

    await waitFor(() =>
      expect(mockedGetPropertyContacts).toHaveBeenCalledWith(expect.objectContaining({ hasLinkedAccount: true })),
    );
  });

  it('sorts the contacts on a column header, asking the server for the new order', async () => {
    const user = userEvent.setup();
    renderTab();
    await screen.findByText(/Jean Dupont/);

    await user.click(screen.getByRole('button', { name: /^Compte$/ }));

    // Paginated server-side: the order has to come from the API, reordering the
    // rows on screen would only reorder the current page. A newly clicked column
    // starts descending - see nextSortDirection.
    await waitFor(() =>
      expect(mockedGetPropertyContacts).toHaveBeenLastCalledWith(
        expect.objectContaining({ sortBy: 'ACCOUNT_STATUS', sortDirection: 'DESC' }),
      ),
    );
  });
});
