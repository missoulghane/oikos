import { describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CreatePropertyForm } from '@/features/property-mngt/properties/components/CreatePropertyForm';

describe('CreatePropertyForm', () => {
  it('shows validation errors and blocks submission when required fields are empty', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<CreatePropertyForm onSubmit={onSubmit} isSubmitting={false} />);

    await user.click(screen.getByRole('button', { name: /créer la copropriété/i }));

    expect(await screen.findByText('Le nom est requis')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('submits the parsed form values', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<CreatePropertyForm onSubmit={onSubmit} isSubmitting={false} />);

    await user.type(screen.getByLabelText('Nom de la copropriété'), 'Résidence Les Oliviers');
    await user.type(screen.getByLabelText('Adresse'), '12 rue de la Paix, Casablanca');
    await user.type(screen.getByLabelText("Nom de l'immeuble"), 'Bâtiment A');
    await user.type(screen.getByLabelText("Nombre d'étages"), '4');
    await user.click(screen.getByRole('button', { name: /créer la copropriété/i }));

    await waitFor(() =>
      expect(onSubmit).toHaveBeenCalledWith(
        {
          name: 'Résidence Les Oliviers',
          address: '12 rue de la Paix, Casablanca',
          firstBuildingName: 'Bâtiment A',
          firstBuildingFloorCount: 4,
        },
        expect.anything(),
      ),
    );
  });
});
