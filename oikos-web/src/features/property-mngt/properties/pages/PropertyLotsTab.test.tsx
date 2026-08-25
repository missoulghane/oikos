import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { PropertyLotsTab } from '@/features/property-mngt/properties/pages/PropertyLotsTab';
import { useBuildings } from '@/features/property-mngt/properties/hooks/useBuildings';
import type { Building, Property } from '@/features/property-mngt/properties/types/property.types';

vi.mock('@/features/property-mngt/properties/hooks/useBuildings', () => ({ useBuildings: vi.fn() }));
// L'immeuble ouvert est ce qui est testé ici, pas le contenu de sa carte : la
// vraie section irait chercher la liste des lots (donc httpClient, donc env.ts).
vi.mock('@/features/property-mngt/properties/components/BuildingSection', () => ({
  BuildingSection: ({ building }: { building: Building }) => <div>Lots de {building.name}</div>,
}));
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useOutletContext: () => ({ property }) };
});

const property = { id: 'property-1', duesCalculationMode: 'FLAT_RATE' } as Property;

function building(id: string, name: string): Building {
  return { id, propertyId: 'property-1', name, floorCount: 3 };
}

function renderTab(buildings: Building[]) {
  vi.mocked(useBuildings).mockReturnValue({
    data: { content: buildings, pageNumber: 0, pageSize: 20, totalElements: buildings.length, totalPages: 1 },
    isLoading: false,
    isError: false,
    error: null,
  } as never);

  render(
    <MemoryRouter initialEntries={['/lots']}>
      <Routes>
        <Route element={<PropertyLotsTab />} path="/lots" />
      </Routes>
    </MemoryRouter>,
  );
}

describe('PropertyLotsTab', () => {
  it('shows one building at a time and switches on click', async () => {
    const user = userEvent.setup();
    renderTab([building('b-1', 'Bâtiment A'), building('b-2', 'Bâtiment B')]);

    // Le premier immeuble est ouvert, le second attend derrière son onglet.
    expect(screen.getByText('Lots de Bâtiment A')).toBeInTheDocument();
    expect(screen.queryByText('Lots de Bâtiment B')).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Bâtiment B' }));

    expect(screen.getByText('Lots de Bâtiment B')).toBeInTheDocument();
    expect(screen.queryByText('Lots de Bâtiment A')).not.toBeInTheDocument();
  });

  it('offers no tab when the copropriété has a single building', () => {
    renderTab([building('b-1', 'Bâtiment unique')]);

    expect(screen.getByText('Lots de Bâtiment unique')).toBeInTheDocument();
    // Un choix à une seule option n'est pas un choix : seule l'action d'ajout reste.
    expect(screen.queryByRole('navigation', { name: 'Immeubles' })).not.toBeInTheDocument();
  });
});
