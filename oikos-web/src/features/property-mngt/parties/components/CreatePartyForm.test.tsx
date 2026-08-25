import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CreatePartyForm } from '@/features/property-mngt/parties/components/CreatePartyForm';
import { useCreateParty } from '@/features/property-mngt/parties/hooks/useCreateParty';

vi.mock('@/features/property-mngt/parties/hooks/useCreateParty', () => ({ useCreateParty: vi.fn() }));

const mutate = vi.fn();

function setup() {
  mutate.mockReset();
  vi.mocked(useCreateParty).mockReturnValue({ mutate, isPending: false, error: null } as never);
  render(<CreatePartyForm propertyId="p-1" onSuccess={vi.fn()} onCancel={vi.fn()} />);
}

describe('CreatePartyForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('propose le type en radios, Particulier coché', () => {
    setup();

    expect(screen.getByRole('radio', { name: 'Particulier' })).toBeChecked();
    expect(screen.getByRole('radio', { name: 'Société' })).not.toBeChecked();
    expect(screen.queryByRole('combobox', { name: 'Type' })).not.toBeInTheDocument();
  });

  it('coche l’invitation par défaut et l’envoie', async () => {
    const user = userEvent.setup();
    setup();

    await user.type(screen.getByLabelText('Nom complet'), 'Jane Doe');
    await user.type(screen.getByLabelText('Email'), 'jane.doe@example.com');

    expect(screen.getByRole('checkbox', { name: /Inviter à créer un compte/ })).toBeChecked();
    await user.click(screen.getByRole('button', { name: 'Créer le contact' }));

    await waitFor(() =>
      expect(mutate.mock.calls[0][0]).toMatchObject({
        fullName: 'Jane Doe',
        partyType: 'INDIVIDUAL',
        email: 'jane.doe@example.com',
        invite: true,
      }),
    );
  });

  it('n’envoie rien quand la case est décochée', async () => {
    // Un gardien, un prestataire : tous les contacts n'ont pas vocation à
    // recevoir un lien de création de compte.
    const user = userEvent.setup();
    setup();

    await user.type(screen.getByLabelText('Nom complet'), 'Jane Doe');
    await user.type(screen.getByLabelText('Email'), 'jane.doe@example.com');
    await user.click(screen.getByRole('checkbox', { name: /Inviter à créer un compte/ }));
    await user.click(screen.getByRole('button', { name: 'Créer le contact' }));

    await waitFor(() => expect(mutate.mock.calls[0][0]).toMatchObject({ invite: false }));
  });

  it('crée un contact qui n’a qu’un téléphone, sans invitation possible', async () => {
    const user = userEvent.setup();
    setup();

    expect(screen.getByRole('checkbox', { name: /Inviter à créer un compte/ })).toBeDisabled();

    await user.type(screen.getByLabelText('Nom complet'), 'Jane Doe');
    await user.type(screen.getByLabelText('Téléphone'), '0612345678');
    await user.click(screen.getByRole('button', { name: 'Créer le contact' }));

    await waitFor(() =>
      expect(mutate.mock.calls[0][0]).toMatchObject({ fullName: 'Jane Doe', phone: '+212612345678', email: undefined }),
    );
  });
});
