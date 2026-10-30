import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { UnitInfoSection } from '@/features/property-mngt/properties/components/UnitInfoSection';
import type { Building, Unit } from '@/features/property-mngt/properties/types/property.types';

vi.mock('@/features/property-mngt/properties/components/EditUnitForm', () => ({
  EditUnitForm: () => <p>Formulaire de modification</p>,
}));

const building: Building = { id: 'b-1', propertyId: 'p-1', name: 'Bâtiment A', floorCount: 3 };
const unit = {
  id: 'u-1',
  buildingId: 'b-1',
  propertyId: 'p-1',
  unitNumber: 'N° 12',
  unitTypeId: 't-1',
  unitTypeName: 'Appartement',
  shares: 150,
  ownershipStatus: 'AFFECTED',
  ownerFullNames: [],
  floor: null,
} as Unit;

describe('UnitInfoSection', () => {
  it('lists what the lot is, an unknown floor shown rather than left out', () => {
    render(<UnitInfoSection unit={unit} building={building} showShares={false} />);

    expect(screen.getByText('N° 12')).toBeInTheDocument();
    expect(screen.getByText('Appartement')).toBeInTheDocument();
    expect(screen.getByText('Bâtiment A')).toBeInTheDocument();
    expect(screen.getByText('Non renseigné')).toBeInTheDocument();
    expect(screen.getByText('Affecté')).toBeInTheDocument();
    // Pas de tantièmes hors du mode tantièmes.
    expect(screen.queryByText('Tantièmes')).not.toBeInTheDocument();
  });

  it('shows the shares in shares mode', () => {
    render(<UnitInfoSection unit={unit} building={building} showShares />);

    expect(screen.getByText('Tantièmes')).toBeInTheDocument();
    expect(screen.getByText('150')).toBeInTheDocument();
  });

  it('opens the edit form in the block, in place of the information', async () => {
    const user = userEvent.setup();
    render(<UnitInfoSection unit={unit} building={building} showShares={false} />);

    await user.click(screen.getByRole('button', { name: 'Modifier le lot' }));

    expect(screen.getByText('Formulaire de modification')).toBeInTheDocument();
    expect(screen.queryByText('Bâtiment A')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Modifier le lot' })).not.toBeInTheDocument();
  });

  it('waits for the building before offering the edit', () => {
    render(<UnitInfoSection unit={unit} building={undefined} showShares={false} />);

    expect(screen.getByRole('button', { name: 'Modifier le lot' })).toBeDisabled();
  });
});
