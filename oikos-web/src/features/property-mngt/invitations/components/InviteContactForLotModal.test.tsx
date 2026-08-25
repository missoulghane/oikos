import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { PartyLot } from '@/features/property-mngt/parties/types/party.types';

const createMutate = vi.fn();
let createdInvitationLink: string | null = null;

vi.mock('@/features/property-mngt/invitations/hooks/useCreateInvitation', () => ({
  useCreateInvitation: () => ({ mutate: createMutate, isPending: false, isError: false, reset: vi.fn() }),
}));
vi.mock('@/features/property-mngt/invitations/hooks/useInvitation', () => ({
  useInvitation: () => ({
    data: createdInvitationLink ? { id: 'inv-1', link: createdInvitationLink } : undefined,
  }),
}));

const { InviteContactForLotModal } = await import(
  '@/features/property-mngt/invitations/components/InviteContactForLotModal'
);

const lot: PartyLot = {
  id: 'ownership-1',
  unitId: 'unit-1',
  unitNumber: 'A-12',
  buildingId: 'building-1',
  buildingName: 'Bâtiment A',
  propertyId: 'p-1',
  propertyName: 'Résidence Les Oliviers',
  ownershipShare: 100,
};

function renderModal(
  overrides: { lots?: PartyLot[]; contactEmail?: string | null; hasOutstandingInvitation?: boolean } = {},
) {
  render(
    <InviteContactForLotModal
      isOpen
      propertyId="p-1"
      partyId="party-1"
      contactFullName="Jane Doe"
      contactEmail={overrides.contactEmail === undefined ? 'jane.doe@example.com' : overrides.contactEmail}
      lots={overrides.lots ?? [lot]}
      hasOutstandingInvitation={overrides.hasOutstandingInvitation ?? false}
      onClose={vi.fn()}
    />,
  );
}

describe('InviteContactForLotModal', () => {
  beforeEach(() => {
    createMutate.mockClear();
    createdInvitationLink = null;
  });

  it('envoie une invitation privée pour le lot choisi', async () => {
    renderModal();

    await userEvent.selectOptions(screen.getByLabelText('Lot concerné'), 'unit-1');
    await userEvent.click(screen.getByRole('button', { name: "Envoyer l'invitation" }));

    // Le contact, pas son adresse : le serveur lit l'email sur sa fiche, une
    // adresse venue du client pouvant ne pas être la sienne.
    expect(createMutate).toHaveBeenCalledWith(
      { propertyId: 'p-1', type: 'PRIVATE', partyId: 'party-1', unitId: 'unit-1' },
      expect.anything(),
    );
  });

  // Inviter « pour un lot » suppose que le syndic a déjà rattaché le contact à
  // ce lot : mieux vaut le dire qu'ouvrir une liste vide.
  it("explique quoi faire quand le contact n'a aucun lot", () => {
    renderModal({ lots: [] });

    expect(screen.getByText('Aucun lot rattaché à ce contact')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: "Envoyer l'invitation" })).not.toBeInTheDocument();
  });

  // Il n'y a nulle part où envoyer le lien.
  it("bloque l'envoi pour un contact sans adresse email", async () => {
    renderModal({ contactEmail: null });

    await userEvent.selectOptions(screen.getByLabelText('Lot concerné'), 'unit-1');

    expect(screen.getByText(/n'a pas d'adresse email/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: "Envoyer l'invitation" })).toBeDisabled();
  });

  // Renvoyer ferme l'invitation qui courait : deux liens vivants pour un même
  // contact, c'est un lien mort qui circule sans que personne ne le sache.
  it("prévient qu'un renvoi annule le lien précédent", () => {
    renderModal({ hasOutstandingInvitation: true });

    expect(screen.getByText(/annulera le lien précédent/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: "Renvoyer l'invitation" })).toBeInTheDocument();
  });

  // Un email se perd dans les indésirables : le syndic finit souvent par
  // renvoyer le lien par WhatsApp, en séance.
  it("affiche le lien après l'envoi, pour une transmission de la main à la main", async () => {
    createdInvitationLink = 'https://app.example.com/invitations?token=tok';
    createMutate.mockImplementation((_payload, options) => options.onSuccess({ id: 'inv-1' }));
    renderModal();

    await userEvent.selectOptions(screen.getByLabelText('Lot concerné'), 'unit-1');
    await userEvent.click(screen.getByRole('button', { name: "Envoyer l'invitation" }));

    expect(screen.getByText(/Invitation envoyée à jane.doe@example.com pour le lot A-12/)).toBeInTheDocument();
    // Deux fois : en clair, et sous le QR code.
    expect(screen.getAllByText('https://app.example.com/invitations?token=tok')).toHaveLength(2);
  });
});
