import { describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RegisterPropertyAdminForm } from '@/features/identity/register/components/RegisterPropertyAdminForm';

/**
 * Le formulaire court d'un cabinet, pendant du wizard en sept étapes : il crée
 * une copropriété avec les mêmes champs, et doit donc la laisser aussi
 * renseignée. C'est la ville qui manquait.
 */
function renderForm(onSubmit = vi.fn()) {
  render(<RegisterPropertyAdminForm onSubmit={onSubmit} isSubmitting={false} submitLabel="Créer mon compte" />);
  return onSubmit;
}

async function fillEverything(user: ReturnType<typeof userEvent.setup>, city: string) {
  await user.type(screen.getByLabelText('Nom complet'), 'Jane Doe');
  await user.type(screen.getByLabelText('Email'), 'jane.doe@example.com');
  await user.type(screen.getByLabelText('Téléphone'), '0612345678');
  await user.type(screen.getByLabelText('Mot de passe'), 'motdepasse123');
  await user.type(screen.getByLabelText('Confirmer le mot de passe'), 'motdepasse123');
  await user.type(screen.getByLabelText('Nom de la copropriété'), 'Résidence Exemple');
  await user.type(screen.getByLabelText('Adresse'), '12 rue Exemple');
  if (city !== '') {
    await user.type(screen.getByLabelText('Ville'), city);
  }
  await user.click(screen.getByRole('button', { name: 'Créer mon compte' }));
}

describe('RegisterPropertyAdminForm', () => {
  it('envoie la ville à côté de l’adresse', async () => {
    const user = userEvent.setup();
    const onSubmit = renderForm();

    await fillEverything(user, 'Casablanca');

    await waitFor(() =>
      expect(onSubmit.mock.calls[0][0]).toMatchObject({
        propertyName: 'Résidence Exemple',
        propertyAddress: '12 rue Exemple',
        propertyCity: 'Casablanca',
      }),
    );
  });

  it('refuse de créer une copropriété sans ville', async () => {
    // L'API l'accepterait ; c'est ce formulaire qui l'exige, pour rester iso
    // avec le wizard plutôt que de produire des fiches à moitié remplies.
    const user = userEvent.setup();
    const onSubmit = renderForm();

    await fillEverything(user, '');

    expect(await screen.findByText('La ville est requise')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });
});
