import { describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { EditUnitForm } from '@/features/property-mngt/properties/components/EditUnitForm';
import { useUpdateUnit } from '@/features/property-mngt/properties/hooks/useUpdateUnit';
import type { Building, Unit } from '@/features/property-mngt/properties/types/property.types';

vi.mock('@/features/property-mngt/properties/hooks/useUpdateUnit', () => ({ useUpdateUnit: vi.fn() }));

const mutate = vi.fn();

const building: Building = { id: 'b-1', propertyId: 'p-1', name: 'Bâtiment A', floorCount: 3 };
const unit = {
  id: 'u-1',
  buildingId: 'b-1',
  propertyId: 'p-1',
  unitNumber: 'N° 12',
  unitTypeId: 't-1',
  unitTypeName: 'Appartement',
  shares: 150,
  ownershipStatus: 'NOT_AFFECTED',
  ownerFullNames: [],
  floor: 1,
} as Unit;

function setup() {
  mutate.mockReset();
  vi.mocked(useUpdateUnit).mockReturnValue({ mutate, isPending: false, error: null } as never);
  render(<EditUnitForm unit={unit} building={building} showShares onSuccess={vi.fn()} onCancel={vi.fn()} />);
}

describe('EditUnitForm', () => {
  it('starts from the lot as it is, the « N° » shown in the field rather than typed', () => {
    setup();

    expect(screen.getByLabelText('Numéro de lot')).toHaveValue('12');
    expect(screen.getByLabelText('Étage')).toHaveValue('1');
    expect(screen.getByLabelText('Tantièmes')).toHaveValue(150);
  });

  it('sends the corrected number, prefixed, and the new floor', async () => {
    const user = userEvent.setup();
    setup();

    await user.clear(screen.getByLabelText('Numéro de lot'));
    await user.type(screen.getByLabelText('Numéro de lot'), '14');
    await user.selectOptions(screen.getByLabelText('Étage'), '3');
    await user.click(screen.getByRole('button', { name: 'Enregistrer' }));

    await waitFor(() => expect(mutate).toHaveBeenCalled());
    expect(mutate.mock.calls[0][0]).toEqual({ unitNumber: 'N° 14', floor: 3, shares: 150 });
  });

  it('only offers the floors of the lot’s building', () => {
    setup();

    expect(screen.getAllByRole('option')).toHaveLength(5); // non renseigné, RDC, 1, 2, 3
  });
});
