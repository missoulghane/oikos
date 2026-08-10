import { describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AddUnitForm } from '@/features/property-mngt/properties/components/AddUnitForm';
import { useAddUnit } from '@/features/property-mngt/properties/hooks/useAddUnit';
import { useUnitTypeDefinitions } from '@/features/property-mngt/properties/hooks/useUnitTypeDefinitions';

vi.mock('@/features/property-mngt/properties/hooks/useAddUnit', () => ({ useAddUnit: vi.fn() }));
vi.mock('@/features/property-mngt/properties/hooks/useUnitTypeDefinitions', () => ({
  useUnitTypeDefinitions: vi.fn(),
}));

const mutate = vi.fn();

function setup(unitTypes: { id: string; name: string }[]) {
  vi.mocked(useAddUnit).mockReturnValue({ mutate, isPending: false, error: null } as never);
  vi.mocked(useUnitTypeDefinitions).mockReturnValue({
    data: unitTypes.map((unitType) => ({ ...unitType, propertyId: 'p-1' })),
    isLoading: false,
    isError: false,
    error: null,
  } as never);

  render(
    <AddUnitForm propertyId="p-1" buildingId="b-1" showShares={false} onSuccess={vi.fn()} onCancel={vi.fn()} />,
  );
}

describe('AddUnitForm', () => {
  it('renders the lot types as radios and preselects Appartement', async () => {
    setup([
      { id: 'type-parking', name: 'Parking' },
      { id: 'type-appartement', name: 'Appartement' },
    ]);

    // Preselected even though it is not first in the list.
    await waitFor(() => expect(screen.getByRole('radio', { name: 'Appartement' })).toBeChecked());
    expect(screen.getByRole('radio', { name: 'Parking' })).not.toBeChecked();
  });

  it('falls back to the first type when the property has no Appartement', async () => {
    setup([
      { id: 'type-others', name: 'OTHERS' },
      { id: 'type-cave', name: 'Cave' },
    ]);

    await waitFor(() => expect(screen.getByRole('radio', { name: 'OTHERS' })).toBeChecked());
  });

  it('submits the selected lot type', async () => {
    const user = userEvent.setup();
    setup([
      { id: 'type-appartement', name: 'Appartement' },
      { id: 'type-parking', name: 'Parking' },
    ]);

    await user.type(screen.getByLabelText('Numéro de lot'), 'A12');
    await user.click(screen.getByRole('radio', { name: 'Parking' }));
    await user.click(screen.getByRole('button', { name: /ajouter le lot/i }));

    await waitFor(() =>
      expect(mutate).toHaveBeenCalledWith(
        { unitNumber: 'A12', unitTypeId: 'type-parking', shares: 0 },
        expect.anything(),
      ),
    );
  });
});
