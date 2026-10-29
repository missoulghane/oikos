import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { EditPropertyInfoForm } from '@/features/property-mngt/properties/components/EditPropertyInfoForm';
import { useUpdateProperty } from '@/features/property-mngt/properties/hooks/useUpdateProperty';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

vi.mock('@/features/property-mngt/properties/hooks/useUpdateProperty', () => ({ useUpdateProperty: vi.fn() }));

const mutate = vi.fn();

const property: Property = {
  id: 'p-1',
  name: 'Résidence Les Oliviers',
  address: '12 rue de la Paix',
  city: '',
  duesCalculationMode: 'FLAT_RATE',
  projectedBudget: null,
};

function setup() {
  mutate.mockReset();
  vi.mocked(useUpdateProperty).mockReturnValue({ mutate, isPending: false, error: null } as never);
  render(<EditPropertyInfoForm property={property} onSuccess={vi.fn()} onCancel={vi.fn()} />);
}

describe('EditPropertyInfoForm', () => {
  it('no longer shows the « champs obligatoires » note', () => {
    setup();

    expect(screen.queryByText(/Les champs suivis d'un/)).not.toBeInTheDocument();
  });

  it('requires the city, and says so as soon as the field is left', async () => {
    const user = userEvent.setup();
    setup();

    await user.click(screen.getByLabelText(/^Ville/));
    await user.tab();

    expect(await screen.findByText('La ville est requise')).toBeInTheDocument();
    expect(mutate).not.toHaveBeenCalled();
  });
});
