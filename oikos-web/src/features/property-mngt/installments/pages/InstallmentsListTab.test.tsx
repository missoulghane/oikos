import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { GetPropertyInstallmentsParams } from '@/features/property-mngt/installments/api/getPropertyInstallments';

const property = { id: 'prop-1', name: 'Al Amal' };

// The API call is mocked, not the hook: the query key lives inside the hook, and
// a key that misses a filter is exactly the bug these tests guard against.
const getPropertyInstallmentsSpy = vi.fn();

// One row is enough, and it is needed: the table - and with it the sortable
// headers - is only rendered when the page has content.
const rows = [
  {
    id: 'i1',
    unitId: 'u1',
    unitNumber: 'A12',
    dueDate: '2026-01-15',
    amount: 1000,
    outstandingAmount: 1000,
    status: 'NOT_SETTLED' as const,
    period: '2026-01',
  },
];

vi.mock('@/features/property-mngt/installments/api/getPropertyInstallments', () => ({
  getPropertyInstallments: (params: GetPropertyInstallmentsParams) => {
    getPropertyInstallmentsSpy(params);
    return Promise.resolve({ content: rows, pageNumber: 0, pageSize: 5, totalPages: 1, totalElements: rows.length });
  },
}));
vi.mock('@/features/property-mngt/installments/hooks/useInstallmentCallsByProperty', () => ({
  useInstallmentCallsByProperty: () => ({ data: { content: [] }, isLoading: false, isError: false }),
}));
vi.mock('@/features/property-mngt/installments/hooks/useRegularizePropertyInstallments', () => ({
  useRegularizePropertyInstallments: () => ({ mutate: vi.fn(), isPending: false, isError: false, isSuccess: false }),
}));
vi.mock('@/features/identity/me', () => ({
  useCurrentUser: () => ({ data: null }),
  canWriteAccounting: () => false,
}));
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useOutletContext: () => ({ property }) };
});

const { InstallmentsListTab } = await import('@/features/property-mngt/installments/pages/InstallmentsListTab');

function renderTab() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/installments']}>
        <Routes>
          <Route path="/installments" element={<InstallmentsListTab />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

/** Query parameters of the most recent call to the paginated endpoint. */
function lastQuery(): GetPropertyInstallmentsParams {
  const calls = getPropertyInstallmentsSpy.mock.calls;
  return calls[calls.length - 1][0];
}

describe('InstallmentsListTab', () => {
  beforeEach(() => {
    getPropertyInstallmentsSpy.mockClear();
  });

  it('opens on the most recent due dates', async () => {
    renderTab();

    await waitFor(() => expect(getPropertyInstallmentsSpy).toHaveBeenCalled());
    expect(lastQuery().sortBy).toBe('DUE_DATE');
    expect(lastQuery().sortDirection).toBe('DESC');
  });

  it('asks the server to leave out the echeances not yet due', async () => {
    renderTab();

    // Server-side, not client-side: the list is paginated, so filtering the
    // received page would leave a page of 20 showing fewer rows and a wrong total.
    await waitFor(() => expect(lastQuery().excludeNotYetDue).toBe(true));
  });

  it('refetches as soon as the toggle is pressed, without waiting for a page change', async () => {
    renderTab();
    await waitFor(() => expect(getPropertyInstallmentsSpy).toHaveBeenCalled());
    const callsBefore = getPropertyInstallmentsSpy.mock.calls.length;

    await userEvent.click(screen.getByRole('button', { name: 'Échéances à venir' }));

    // The regression: the query key used to enumerate the filters one by one and
    // missed this one, so the cached page was served until something else moved
    // the key.
    await waitFor(() => expect(getPropertyInstallmentsSpy.mock.calls.length).toBeGreaterThan(callsBefore));
    expect(lastQuery().excludeNotYetDue).toBeUndefined();
  });

  it('counts the toggle among the active filters, unlike the sort', async () => {
    renderTab();

    expect(screen.getByRole('button', { name: 'Filtres' })).not.toHaveTextContent('1');

    await userEvent.click(screen.getByRole('button', { name: 'Échéances à venir' }));

    expect(screen.getByRole('button', { name: 'Filtres' })).toHaveTextContent('1');
  });

  it('puts them back out of sight on "Réinitialiser"', async () => {
    renderTab();
    await userEvent.click(screen.getByRole('button', { name: 'Échéances à venir' }));
    await userEvent.click(screen.getByRole('button', { name: /Réinitialiser/ }));

    await waitFor(() => expect(lastQuery().excludeNotYetDue).toBe(true));
    expect(lastQuery().sortDirection).toBe('DESC');
  });

  it('names the lot each echeance belongs to', async () => {
    renderTab();

    // The list spans the whole copropriete: without the lot, a row says how much
    // is owed without saying by whom.
    expect(await screen.findByText('A12')).toBeInTheDocument();
  });

  it('sorts on a column header instead of a "Trier par" field', async () => {
    renderTab();
    // findBy, not getBy: the table only appears once the query resolves.
    const amountHeader = await screen.findByRole('button', { name: /^Montant$/ });

    expect(screen.queryByLabelText('Trier par')).not.toBeInTheDocument();

    await userEvent.click(amountHeader);

    await waitFor(() => expect(lastQuery().sortBy).toBe('AMOUNT'));
  });

  it('flips the direction when the active column is clicked again', async () => {
    renderTab();
    const dueDateHeader = await screen.findByRole('button', { name: /^Échéance$/ });
    expect(lastQuery().sortDirection).toBe('DESC');

    // Échéance is already the active column, descending by default.
    await userEvent.click(dueDateHeader);

    await waitFor(() => expect(lastQuery().sortDirection).toBe('ASC'));
    expect(screen.getByRole('columnheader', { name: /Échéance/ })).toHaveAttribute('aria-sort', 'ascending');
  });

  it('sends the typed text as a search term, on the toolbar rather than behind the filter button', async () => {
    renderTab();
    await waitFor(() => expect(getPropertyInstallmentsSpy).toHaveBeenCalled());

    // Permanently visible: the search box is part of the toolbar, not of the
    // folded panel - same layout as the copropriétaire list.
    await userEvent.type(screen.getByRole('searchbox', { name: 'Rechercher' }), 'A12');

    await waitFor(() => expect(lastQuery().search).toBe('A12'));
  });
});
