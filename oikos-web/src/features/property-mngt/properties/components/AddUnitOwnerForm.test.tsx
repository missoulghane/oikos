import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AddUnitOwnerForm } from '@/features/property-mngt/properties/components/AddUnitOwnerForm';
import { useAddUnitOwner } from '@/features/property-mngt/properties/hooks/useAddUnitOwner';
import { useParties } from '@/features/property-mngt/parties/hooks/useParties';

vi.mock('@/features/property-mngt/properties/hooks/useAddUnitOwner', () => ({ useAddUnitOwner: vi.fn() }));
vi.mock('@/features/property-mngt/parties/hooks/useParties', () => ({ useParties: vi.fn() }));

const mutate = vi.fn();

interface KnownParty {
  id: string;
  fullName: string;
  email: string;
  phone: string | null;
}

/**
 * useParties est appelé deux fois par le formulaire (une recherche par email,
 * une par téléphone) : le faux répond la même page pour les deux, ce que
 * l'annuaire réel ferait aussi sur une recherche libre.
 */
function setup(knownParties: KnownParty[] = []) {
  mutate.mockReset();
  vi.mocked(useAddUnitOwner).mockReturnValue({ mutate, isPending: false, error: null } as never);
  vi.mocked(useParties).mockReturnValue({
    data: { content: knownParties, page: 0, size: 20, totalElements: knownParties.length, totalPages: 1 },
    isLoading: false,
    isError: false,
    error: null,
  } as never);

  render(<AddUnitOwnerForm unitId="u-1" propertyId="p-1" onSuccess={vi.fn()} onCancel={vi.fn()} />);
}

describe('AddUnitOwnerForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('propose Particulier et Société en radios, Particulier coché', () => {
    setup();

    expect(screen.getByRole('radio', { name: 'Particulier' })).toBeChecked();
    expect(screen.getByRole('radio', { name: 'Société' })).not.toBeChecked();
    expect(screen.queryByRole('combobox', { name: 'Type' })).not.toBeInTheDocument();
  });

  it('coche l’invitation par défaut dès qu’une adresse est saisie', async () => {
    const user = userEvent.setup();
    setup();

    await user.type(screen.getByLabelText('Email (optionnel)'), 'jane.doe@example.com');

    expect(screen.getByRole('checkbox', { name: /Inviter à créer un compte/ })).toBeChecked();
    expect(screen.getByRole('checkbox', { name: /Inviter à créer un compte/ })).toBeEnabled();
  });

  it('signale un contact déjà enregistré avec cet email, et propose de le rattacher', async () => {
    const user = userEvent.setup();
    setup([{ id: 'party-1', fullName: 'Jane Doe', email: 'jane.doe@example.com', phone: null }]);

    await user.type(screen.getByLabelText('Email (optionnel)'), 'jane.doe@example.com');

    expect(await screen.findByText(/Un contact existe déjà avec cet email : Jane Doe/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Rattacher au contact existant' })).toBeInTheDocument();
  });

  it('signale aussi un contact reconnu à son téléphone', async () => {
    // Le second filet demandé : le syndic ressaisit la même personne sous une
    // autre adresse, et seul le numéro trahit le doublon.
    const user = userEvent.setup();
    setup([{ id: 'party-1', fullName: 'Jane Doe', email: 'jane.doe@example.com', phone: '+212612345678' }]);

    await user.type(screen.getByLabelText('Téléphone'), '612345678');

    expect(await screen.findByText(/Un contact existe déjà avec ce numéro de téléphone/)).toBeInTheDocument();
  });

  it('envoie la saisie avec le type, le téléphone international et l’invitation', async () => {
    const user = userEvent.setup();
    setup();

    await user.type(screen.getByLabelText('Nom complet'), 'Jane Doe');
    await user.type(screen.getByLabelText('Email (optionnel)'), 'jane.doe@example.com');
    await user.type(screen.getByLabelText('Téléphone'), '0612345678');
    await user.clear(screen.getByLabelText('Part de propriété (%)'));
    await user.type(screen.getByLabelText('Part de propriété (%)'), '50');
    await user.click(screen.getByRole('button', { name: 'Rattacher au lot' }));

    await waitFor(() =>
      expect(mutate.mock.calls[0][0]).toEqual({
        fullName: 'Jane Doe',
        partyType: 'INDIVIDUAL',
        email: 'jane.doe@example.com',
        // Le 0 de tête n'existe pas en format international : c'est sur cette
        // valeur-là que le serveur reconnaît un contact déjà enregistré.
        phone: '+212612345678',
        ownershipShare: 50,
        invite: true,
      }),
    );
  });

  it('laisse décocher l’invitation', async () => {
    const user = userEvent.setup();
    setup();

    await user.type(screen.getByLabelText('Nom complet'), 'Jane Doe');
    await user.type(screen.getByLabelText('Email (optionnel)'), 'jane.doe@example.com');
    await user.click(screen.getByRole('checkbox', { name: /Inviter à créer un compte/ }));
    await user.click(screen.getByRole('button', { name: 'Rattacher au lot' }));

    await waitFor(() => expect(mutate.mock.calls[0][0]).toMatchObject({ invite: false }));
  });

  it('rattache un copropriétaire qui n’a qu’un téléphone', async () => {
    // L'email n'est plus exigé : sans lui, la fiche se crée et rien ne part.
    const user = userEvent.setup();
    setup();

    await user.type(screen.getByLabelText('Nom complet'), 'Jane Doe');
    await user.type(screen.getByLabelText('Téléphone'), '0612345678');
    await user.click(screen.getByRole('button', { name: 'Rattacher au lot' }));

    await waitFor(() =>
      expect(mutate.mock.calls[0][0]).toMatchObject({
        fullName: 'Jane Doe',
        phone: '+212612345678',
        email: undefined,
      }),
    );
  });

  it('n’offre pas d’invitation tant qu’il n’y a pas d’adresse', () => {
    setup();

    expect(screen.getByRole('checkbox', { name: /Inviter à créer un compte/ })).toBeDisabled();
    expect(screen.getByText('Renseignez un email pour pouvoir envoyer une invitation.')).toBeInTheDocument();
  });
});
