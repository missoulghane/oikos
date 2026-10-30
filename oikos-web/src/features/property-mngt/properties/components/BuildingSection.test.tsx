import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BuildingSection } from '@/features/property-mngt/properties/components/BuildingSection';
import type { Building } from '@/features/property-mngt/properties/types/property.types';

vi.mock('@/features/property-mngt/properties/components/UnitList', () => ({
  UnitList: () => <p>Liste des lots</p>,
}));
vi.mock('@/features/property-mngt/properties/components/AddUnitForm', () => ({
  AddUnitForm: () => <p>Formulaire d'ajout</p>,
}));

const building: Building = { id: 'b-1', propertyId: 'p-1', name: 'Bâtiment A', floorCount: 3 };

describe('BuildingSection', () => {
  it('shows only the add form while a lot is being added, not the list next to it', async () => {
    const user = userEvent.setup();
    render(<BuildingSection building={building} showShares={false} />);
    expect(screen.getByText('Liste des lots')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Ajouter un lot' }));

    expect(screen.getByText("Formulaire d'ajout")).toBeInTheDocument();
    expect(screen.queryByText('Liste des lots')).not.toBeInTheDocument();
  });
});
