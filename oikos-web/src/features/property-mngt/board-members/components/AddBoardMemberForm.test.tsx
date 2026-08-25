import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AddBoardMemberForm } from '@/features/property-mngt/board-members/components/AddBoardMemberForm';
import { useAddBoardMember } from '@/features/property-mngt/board-members/hooks/useAddBoardMember';
import { useBoardMembers } from '@/features/property-mngt/board-members/hooks/useBoardMembers';
import { useContactMatch, type ContactMatch } from '@/features/property-mngt/parties/hooks/useContactMatch';
import type { BoardMember, BoardRole } from '@/features/property-mngt/board-members/types/boardMember.types';

vi.mock('@/features/property-mngt/board-members/hooks/useAddBoardMember', () => ({ useAddBoardMember: vi.fn() }));
vi.mock('@/features/property-mngt/board-members/hooks/useBoardMembers', () => ({ useBoardMembers: vi.fn() }));
vi.mock('@/features/property-mngt/parties/hooks/useContactMatch', async () => {
  const actual = await vi.importActual<typeof import('@/features/property-mngt/parties/hooks/useContactMatch')>(
    '@/features/property-mngt/parties/hooks/useContactMatch',
  );
  // Le rapprochement a son propre test (useContactMatch.test) : ici on vérifie
  // ce que le formulaire en fait. Le libellé, lui, reste le vrai - c'est ce que
  // le syndic lit.
  return { ...actual, useContactMatch: vi.fn() };
});

const mutate = vi.fn();

function seat(partyId: string, partyFullName: string, boardRole: BoardRole): BoardMember {
  return {
    id: `seat-${boardRole}`,
    propertyId: 'p-1',
    partyId,
    partyFullName,
    partyEmail: null,
    boardRole,
    status: 'ACTIVE',
    hasLinkedAccount: false,
  };
}

function setup(match?: ContactMatch, boardMembers: BoardMember[] = []) {
  mutate.mockReset();
  vi.mocked(useAddBoardMember).mockReturnValue({ mutate, isPending: false, error: null } as never);
  vi.mocked(useBoardMembers).mockReturnValue({
    data: boardMembers,
    isLoading: false,
    isError: false,
    error: null,
  } as never);
  vi.mocked(useContactMatch).mockReturnValue(match);

  render(<AddBoardMemberForm propertyId="p-1" onSuccess={vi.fn()} onCancel={vi.fn()} />);
}

describe('AddBoardMemberForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // Le formulaire donnait l'impression de créer un contact à chaque fois, alors
  // que le serveur rapproche depuis toujours sur l'email puis le téléphone.
  it('signale un contact déjà enregistré avec cet email, et propose de le rattacher', () => {
    setup({ partyId: 'party-1', fullName: 'Jane Doe', contactEmail: 'jane.doe@example.com', matchedOn: 'EMAIL' });

    expect(screen.getByText(/Un contact existe déjà avec cet email : Jane Doe/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Rattacher au contact existant' })).toBeInTheDocument();
  });

  it('rapproche aussi sur le téléphone', () => {
    setup({ partyId: 'party-1', fullName: 'Jane Doe', contactEmail: null, matchedOn: 'PHONE' });

    expect(screen.getByText(/Un contact existe déjà avec ce numéro de téléphone : Jane Doe/)).toBeInTheDocument();
  });

  /**
   * Le cas qui fabriquait des doublons : le syndic tape l'adresse avec laquelle
   * la personne se connecte, qui n'est pas celle inscrite sur sa fiche. Le
   * message nomme les deux, sans quoi le syndic croit s'être trompé de contact.
   */
  it("reconnaît une personne à son adresse de connexion, et nomme la fiche correspondante", () => {
    setup({
      partyId: 'party-1',
      fullName: 'Karim Benali',
      contactEmail: 'user1.party@oikos.com',
      matchedOn: 'ACCOUNT_EMAIL',
    });

    expect(screen.getByText(/Karim Benali se connecte déjà avec cette adresse/)).toBeInTheDocument();
    expect(screen.getByText(/user1\.party@oikos\.com/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Rattacher au contact existant' })).toBeInTheDocument();
  });

  // Un contact déjà au conseil sous une autre fonction reste ajoutable : le
  // président peut aussi être trésorier. Le dire évite de croire à un doublon.
  it('annonce les fonctions déjà occupées par ce contact sans bloquer', () => {
    setup({ partyId: 'party-1', fullName: 'Jane Doe', contactEmail: 'jane.doe@example.com', matchedOn: 'EMAIL' }, [
      seat('party-1', 'Jane Doe', 'PRESIDENT'),
    ]);

    expect(screen.getByText(/Il siège déjà au conseil comme Président/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Rattacher au contact existant' })).toBeEnabled();
  });

  // Le refus porte sur le couple (contact, fonction) : deux fois trésorier n'a
  // pas de sens, et le serveur le refuse (PartyAlreadyHasRoleException).
  it('ferme la porte quand le contact occupe déjà la fonction choisie', async () => {
    const user = userEvent.setup();
    setup({ partyId: 'party-1', fullName: 'Jane Doe', contactEmail: 'jane.doe@example.com', matchedOn: 'EMAIL' }, [
      seat('party-1', 'Jane Doe', 'TREASURER'),
    ]);

    await user.selectOptions(screen.getByLabelText('Rôle'), 'TREASURER');

    expect(await screen.findByText(/occupe déjà la fonction de Trésorier au conseil/)).toBeInTheDocument();
    // Désactivé plutôt que refusé après coup : le message dit pourquoi.
    expect(screen.getByRole('button', { name: 'Rattacher au contact existant' })).toBeDisabled();
  });

  it('reste une création quand aucune coordonnée ne correspond', async () => {
    const user = userEvent.setup();
    setup();

    await user.type(screen.getByLabelText('Nom complet'), 'Karim Alami');
    await user.type(screen.getByLabelText('Email'), 'karim@example.com');
    await user.click(screen.getByRole('button', { name: 'Ajouter' }));

    expect(screen.queryByText(/Un contact existe déjà/)).not.toBeInTheDocument();
    expect(mutate).toHaveBeenCalledWith(
      expect.objectContaining({ fullName: 'Karim Alami', email: 'karim@example.com', boardRole: 'MEMBER' }),
      expect.anything(),
    );
  });
});
