import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { UnitList } from '@/features/property-mngt/properties/components/UnitList';
import { getUnits } from '@/features/property-mngt/properties/api/getUnits';
import type { Unit } from '@/features/property-mngt/properties/types/property.types';

// Factory form (not bare automock): a bare `vi.mock(path)` still loads the
// real module to derive its shape, which would drag in httpClient -> env.ts
// (VITE_API_URL) with no .env available under Vitest.
vi.mock('@/features/property-mngt/properties/api/getUnits', () => ({
  getUnits: vi.fn(),
}));

const mockedGetUnits = vi.mocked(getUnits);

const affectedUnit: Unit = {
  id: 'unit-1',
  buildingId: 'building-1',
  unitNumber: 'A12',
  unitTypeId: 'type-1',
  unitTypeName: 'Appartement',
  shares: 150,
  ownershipStatus: 'AFFECTED',
  ownerFullNames: ['Jean Dupont'],
};

function renderList() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <UnitList buildingId="building-1" propertyId="property-1" showShares />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('UnitList', () => {
  beforeEach(() => {
    mockedGetUnits.mockReset();
    mockedGetUnits.mockResolvedValue({ content: [affectedUnit], pageNumber: 0, pageSize: 5, totalElements: 1, totalPages: 1 });
  });

  it('lists units without any filter on first render', async () => {
    renderList();

    expect(await screen.findByText('Lot A12 — Appartement')).toBeInTheDocument();
    expect(mockedGetUnits).toHaveBeenCalledWith(
      expect.objectContaining({ buildingId: 'building-1', search: undefined, ownershipStatus: undefined }),
    );
  });

  it('sends the ownership status to the API when the affectation filter is set', async () => {
    const user = userEvent.setup();
    renderList();
    await screen.findByText('Lot A12 — Appartement');

    await user.selectOptions(screen.getByLabelText('Affectation'), 'NOT_AFFECTED');

    await waitFor(() =>
      expect(mockedGetUnits).toHaveBeenCalledWith(expect.objectContaining({ ownershipStatus: 'NOT_AFFECTED' })),
    );
  });

  it('sends the typed lot number as the search term', async () => {
    const user = userEvent.setup();
    renderList();
    await screen.findByText('Lot A12 — Appartement');

    await user.type(screen.getByLabelText("Rechercher un lot (numéro d'appartement, propriétaire : nom, téléphone)"), 'A12');

    await waitFor(() => expect(mockedGetUnits).toHaveBeenCalledWith(expect.objectContaining({ search: 'A12' })));
  });
});
