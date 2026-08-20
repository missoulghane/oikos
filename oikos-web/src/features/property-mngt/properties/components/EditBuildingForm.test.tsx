import { describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { EditBuildingForm } from '@/features/property-mngt/properties/components/EditBuildingForm';
import { useUpdateBuilding } from '@/features/property-mngt/properties/hooks/useUpdateBuilding';
import type { Building } from '@/features/property-mngt/properties/types/property.types';

vi.mock('@/features/property-mngt/properties/hooks/useUpdateBuilding', () => ({ useUpdateBuilding: vi.fn() }));

const mutate = vi.fn();

const building: Building = { id: 'b-1', propertyId: 'p-1', name: 'Bâtiment A', floorCount: 5 };

function setup() {
  mutate.mockReset();
  vi.mocked(useUpdateBuilding).mockReturnValue({ mutate, isPending: false, error: null } as never);
  render(<EditBuildingForm building={building} onSuccess={vi.fn()} onCancel={vi.fn()} />);
}

describe('EditBuildingForm', () => {
  it("part des valeurs actuelles de l'immeuble", () => {
    setup();

    expect(screen.getByLabelText("Nom de l'immeuble")).toHaveValue('Bâtiment A');
    expect(screen.getByLabelText("Nombre d'étages")).toHaveValue(5);
  });

  it('envoie le nom et le nombre d’étages corrigés', async () => {
    const user = userEvent.setup();
    setup();

    await user.clear(screen.getByLabelText("Nom de l'immeuble"));
    await user.type(screen.getByLabelText("Nom de l'immeuble"), 'Bâtiment B');
    await user.clear(screen.getByLabelText("Nombre d'étages"));
    await user.type(screen.getByLabelText("Nombre d'étages"), '7');
    await user.click(screen.getByRole('button', { name: 'Enregistrer' }));

    await waitFor(() => expect(mutate.mock.calls[0][0]).toEqual({ name: 'Bâtiment B', floorCount: 7 }));
  });

  it('accepte un immeuble de plain-pied', async () => {
    // Zéro étage est une villa ou un local commercial, pas une saisie ratée.
    const user = userEvent.setup();
    setup();

    await user.clear(screen.getByLabelText("Nombre d'étages"));
    await user.type(screen.getByLabelText("Nombre d'étages"), '0');
    await user.click(screen.getByRole('button', { name: 'Enregistrer' }));

    await waitFor(() => expect(mutate.mock.calls[0][0]).toEqual({ name: 'Bâtiment A', floorCount: 0 }));
  });

  it('refuse de laisser un immeuble sans nom, sans rien envoyer', async () => {
    // La liste des lots titre chaque section avec ce nom : vidé, elle perdrait
    // son seul repère de lecture.
    const user = userEvent.setup();
    setup();

    await user.clear(screen.getByLabelText("Nom de l'immeuble"));
    await user.click(screen.getByRole('button', { name: 'Enregistrer' }));

    expect(await screen.findByText("Le nom de l'immeuble est requis")).toBeInTheDocument();
    expect(mutate).not.toHaveBeenCalled();
  });

  it('refuse un nombre d’étages négatif', async () => {
    const user = userEvent.setup();
    setup();

    await user.clear(screen.getByLabelText("Nombre d'étages"));
    await user.type(screen.getByLabelText("Nombre d'étages"), '-1');
    await user.click(screen.getByRole('button', { name: 'Enregistrer' }));

    expect(await screen.findByText('Ne peut pas être négatif')).toBeInTheDocument();
    expect(mutate).not.toHaveBeenCalled();
  });
});
