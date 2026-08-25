import { beforeEach, describe, expect, it, vi } from 'vitest';
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

function setup(unitTypes: { id: string; name: string }[], floorCount = 3) {
  vi.mocked(useAddUnit).mockReturnValue({ mutate, isPending: false, error: null } as never);
  vi.mocked(useUnitTypeDefinitions).mockReturnValue({
    data: unitTypes.map((unitType) => ({ ...unitType, propertyId: 'p-1' })),
    isLoading: false,
    isError: false,
    error: null,
  } as never);

  render(
    <AddUnitForm
      building={{ id: 'b-1', propertyId: 'p-1', name: 'Bâtiment A', floorCount }}
      showShares={false}
      onSuccess={vi.fn()}
      onCancel={vi.fn()}
    />,
  );
}

describe('AddUnitForm', () => {
  beforeEach(() => {
    mutate.mockReset();
  });

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

  it('asks for the lot type before anything else', async () => {
    setup([{ id: 'type-appartement', name: 'Appartement' }]);

    // Le type d'abord : c'est lui qui dit ce qu'on ajoute.
    const type = screen.getByRole('group', { name: /Type de lot/ });
    const number = screen.getByLabelText(/Numéro de lot/);
    expect(type.compareDocumentPosition(number) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
  });

  it('only offers the floors the building actually has, ground floor included', async () => {
    setup([{ id: 'type-appartement', name: 'Appartement' }], 2);

    const floors = screen.getByLabelText('Étage');
    expect(Array.from(floors.querySelectorAll('option')).map((option) => option.textContent)).toEqual([
      'Non renseigné',
      'Rez-de-chaussée',
      '1er étage',
      '2e étage',
    ]);
  });

  it('submits the chosen floor', async () => {
    const user = userEvent.setup();
    setup([{ id: 'type-appartement', name: 'Appartement' }], 4);

    await user.type(screen.getByLabelText(/Numéro de lot/), '32');
    await user.selectOptions(screen.getByLabelText('Étage'), '3');
    await user.click(screen.getByRole('button', { name: /ajouter le lot/i }));

    await waitFor(() =>
      expect(mutate).toHaveBeenCalledWith(
        { unitNumber: 'N° 32', unitTypeId: 'type-appartement', shares: 0, floor: 3 },
        expect.anything(),
      ),
    );
  });

  it('refuses a lot number that is not a plain number', async () => {
    const user = userEvent.setup();
    setup([{ id: 'type-appartement', name: 'Appartement' }]);

    await user.type(screen.getByLabelText(/Numéro de lot/), 'A12');
    await user.click(screen.getByRole('button', { name: /ajouter le lot/i }));

    expect(await screen.findByText('Le numéro de lot ne peut contenir que des chiffres')).toBeInTheDocument();
    expect(mutate).not.toHaveBeenCalled();
  });

  it('submits the selected lot type', async () => {
    const user = userEvent.setup();
    setup([
      { id: 'type-appartement', name: 'Appartement' },
      { id: 'type-parking', name: 'Parking' },
    ]);

    await user.type(screen.getByLabelText(/Numéro de lot/), '32');
    await user.click(screen.getByRole('radio', { name: 'Parking' }));
    await user.click(screen.getByRole('button', { name: /ajouter le lot/i }));

    // floor: null, et surtout pas 0 - « non renseigné » n'est pas le rez-de-chaussée.
    await waitFor(() =>
      expect(mutate).toHaveBeenCalledWith(
        { unitNumber: 'N° 32', unitTypeId: 'type-parking', shares: 0, floor: null },
        expect.anything(),
      ),
    );
  });
});
