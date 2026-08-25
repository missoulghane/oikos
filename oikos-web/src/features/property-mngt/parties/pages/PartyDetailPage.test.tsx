import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { PartyDetailPage } from '@/features/property-mngt/parties/pages/PartyDetailPage';
import { useParty } from '@/features/property-mngt/parties/hooks/useParty';
import { usePartyLots } from '@/features/property-mngt/parties/hooks/usePartyLots';
import { useCurrentUser } from '@/features/identity/me';
import type { PartyAccountStatus, PartyDetail } from '@/features/property-mngt/parties/types/party.types';

vi.mock('@/features/property-mngt/parties/hooks/useParty', () => ({ useParty: vi.fn() }));
vi.mock('@/features/property-mngt/parties/hooks/usePartyLots', () => ({ usePartyLots: vi.fn() }));
// L'envoi d'une invitation a ses propres appels et son propre test : ce qui se
// vérifie ici est quand la fiche l'offre, pas ce qu'elle envoie.
vi.mock('@/features/property-mngt/invitations', () => ({
  InviteContactForLotModal: ({ isOpen }: { isOpen: boolean }) =>
    isOpen ? <div>Modale d'invitation</div> : null,
}));
// Le formulaire complet a ses propres appels (mise à jour, annuaire) : ce qui
// se teste ici est de savoir quand il s'ouvre, pas ce qu'il envoie.
vi.mock('@/features/property-mngt/parties/components/EditPartyForm', () => ({
  EditPartyForm: () => <div>Formulaire de fiche</div>,
}));
vi.mock('@/features/identity/me', async () => {
  const actual = await vi.importActual<typeof import('@/features/identity/me')>('@/features/identity/me');
  return { ...actual, useCurrentUser: vi.fn() };
});

function party(accountStatus: PartyAccountStatus): PartyDetail {
  return {
    id: 'party-1',
    fullName: 'Jane Doe',
    partyType: 'INDIVIDUAL',
    email: 'jane.doe@example.com',
    phone: '+212600000000',
    accountStatus,
  };
}

function renderPage(accountStatus: PartyAccountStatus, { asSyndic = true } = {}) {
  vi.mocked(useParty).mockReturnValue({ data: party(accountStatus), isLoading: false, isError: false } as never);
  vi.mocked(usePartyLots).mockReturnValue({ data: [], isLoading: false, isError: false } as never);
  vi.mocked(useCurrentUser).mockReturnValue({
    data: {
      id: 'user-1',
      fullName: 'Nadia Berrada',
      roles: asSyndic ? ['ROLE_ADMIN'] : ['ROLE_USER'],
      roleByProperty: asSyndic ? { 'property-1': ['PROPERTY_MANAGER_ADMIN'] } : { 'property-1': ['PROPERTY_OWNER'] },
    },
    isLoading: false,
  } as never);

  render(
    <MemoryRouter initialEntries={['/parties/property-1/party-1']}>
      <Routes>
        <Route element={<PartyDetailPage />} path="/parties/:propertyId/:partyId" />
      </Routes>
    </MemoryRouter>,
  );
}

describe('PartyDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('laisse le syndic modifier toute la fiche quand aucun compte n’est associé', async () => {
    const user = userEvent.setup();
    renderPage('NONE');

    await user.click(screen.getByRole('button', { name: 'Modifier' }));

    expect(screen.getByText('Formulaire de fiche')).toBeInTheDocument();
    expect(screen.queryByText(/invitation est en cours/i)).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Inviter pour un lot' })).toBeInTheDocument();
  });

  // Le bandeau se lit sans ouvrir le formulaire : c'est l'état du contact.
  // Sans lui, le syndic réinvite quelqu'un qui a déjà un lien qui dort dans sa
  // boîte mail, ou s'étonne de ne voir arriver aucun compte.
  it('annonce l’invitation en cours dès l’ouverture de la fiche', () => {
    renderPage('INVITED');

    expect(screen.getByText(/Une invitation est en cours pour ce contact/)).toBeInTheDocument();
    // Et le bouton dit ce qu'il fera : remplacer, pas doubler.
    expect(screen.getByRole('button', { name: "Renvoyer l'invitation" })).toBeInTheDocument();
  });

  it('avertit qu’une adresse corrigée demande un renvoi de l’invitation', async () => {
    const user = userEvent.setup();
    renderPage('INVITED');

    await user.click(screen.getByRole('button', { name: 'Modifier' }));

    expect(screen.getByText(/renvoyez l'invitation après enregistrement/)).toBeInTheDocument();
    expect(screen.getByText('Formulaire de fiche')).toBeInTheDocument();
  });

  it('n’annonce aucune invitation en cours quand il n’y en a pas', () => {
    renderPage('NONE');

    expect(screen.queryByText(/Une invitation est en cours pour ce contact/)).not.toBeInTheDocument();
  });

  // Une invitation privée porte sur un lot précis : c'est depuis la fiche du
  // contact, et de nulle part ailleurs, qu'elle part.
  it("ouvre l'envoi d'invitation depuis la fiche du contact", async () => {
    const user = userEvent.setup();
    renderPage('NONE');

    expect(screen.queryByText("Modale d'invitation")).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Inviter pour un lot' }));

    expect(screen.getByText("Modale d'invitation")).toBeInTheDocument();
  });

  it('ferme la modification dès qu’un compte est rattaché au contact', () => {
    renderPage('ACTIVE');

    expect(screen.queryByRole('button', { name: 'Modifier' })).not.toBeInTheDocument();
    expect(screen.getByText(/tenues à jour par son titulaire/)).toBeInTheDocument();
    // Ni invitation à envoyer : le compte existe déjà.
    expect(screen.queryByRole('button', { name: /invitation|Inviter/ })).not.toBeInTheDocument();
  });

  it('garde au titulaire du compte la modification de son propre téléphone', () => {
    renderPage('ACTIVE', { asSyndic: false });

    expect(screen.getByRole('button', { name: 'Modifier le téléphone' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Modifier' })).not.toBeInTheDocument();
  });
});
