import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom';
import type { MembershipRequest } from '@/features/property-mngt/invitations/types/invitation.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

const acceptMutate = vi.fn();
const rejectMutate = vi.fn();
let requests: MembershipRequest[] = [];

vi.mock('@/features/property-mngt/invitations/hooks/useMembershipRequests', () => ({
  useMembershipRequests: () => ({
    data: { content: requests, pageNumber: 0, pageSize: 20, totalElements: requests.length, totalPages: 1 },
    isLoading: false,
    isError: false,
  }),
}));
vi.mock('@/features/property-mngt/invitations/hooks/useAcceptMembershipRequest', () => ({
  useAcceptMembershipRequest: () => ({ mutate: acceptMutate, isPending: false, isError: false, variables: undefined }),
}));
vi.mock('@/features/property-mngt/invitations/hooks/useRejectMembershipRequest', () => ({
  useRejectMembershipRequest: () => ({ mutate: rejectMutate, isPending: false, isError: false, reset: vi.fn() }),
}));

const { MembershipRequestsTab } = await import(
  '@/features/property-mngt/invitations/pages/MembershipRequestsTab'
);

const property: Property = {
  id: 'p-1',
  name: 'Résidence Les Oliviers',
  address: '12 rue de la Paix',
  city: 'Casablanca',
  duesCalculationMode: 'FLAT_RATE',
  projectedBudget: null,
};

function pendingRequest(override: Partial<MembershipRequest> = {}): MembershipRequest {
  return {
    id: 'req-1',
    invitationId: 'inv-1',
    propertyId: 'p-1',
    unitId: 'unit-1',
    partyId: 'party-1',
    userId: 'user-1',
    requesterFullName: 'Jane Doe',
    requesterEmail: 'jane.doe@example.com',
    requesterAccountVerified: true,
    unitNumber: 'A-12',
    unitTypeName: 'Appartement',
    status: 'PENDING',
    submittedAt: '2026-08-20T10:00:00Z',
    decidedAt: null,
    decidedByUserId: null,
    decidedByFullName: null,
    rejectionReason: null,
    ...override,
  };
}

/** L'onglet lit sa copropriété via useOutletContext : la route parente est donc montée pour de vrai. */
function renderTab(search = '') {
  render(
    <MemoryRouter initialEntries={[`/requests${search}`]}>
      <Routes>
        <Route path="/" element={<Outlet context={{ property }} />}>
          <Route path="requests" element={<MembershipRequestsTab />} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('MembershipRequestsTab', () => {
  beforeEach(() => {
    requests = [pendingRequest()];
    acceptMutate.mockClear();
    rejectMutate.mockClear();
  });

  // Le tableau affichait des identifiants bruts et allait chercher chaque
  // contact et chaque lot depuis le navigateur.
  it('affiche le demandeur, son lot et la date de dépôt', () => {
    renderTab();

    expect(screen.getByText('Jane Doe')).toBeInTheDocument();
    expect(screen.getByText('jane.doe@example.com')).toBeInTheDocument();
    expect(screen.getByText(/A-12/)).toBeInTheDocument();
    // Dans le tableau, et non dans le filtre de statut qui porte le même mot.
    expect(within(screen.getByRole('table')).getByText('En attente')).toBeInTheDocument();
  });

  // Attribuer un lot à une adresse jamais confirmée est une décision, pas un
  // détail : une demande déposée pendant l'inscription arrive avant sa
  // vérification.
  it("signale un demandeur dont l'adresse email n'est pas vérifiée", () => {
    requests = [pendingRequest({ requesterAccountVerified: false })];
    renderTab();

    expect(screen.getByText('Email non vérifié')).toBeInTheDocument();
  });

  it('valide une demande en attente', async () => {
    renderTab();

    await userEvent.click(screen.getByRole('button', { name: 'Accepter' }));

    expect(acceptMutate).toHaveBeenCalledWith('req-1');
  });

  // Le motif part au demandeur sur ses quatre canaux : il se saisit avant que
  // le refus ne soit confirmé, pas après.
  it('demande un motif avant de confirmer un refus', async () => {
    renderTab();

    await userEvent.click(screen.getByRole('button', { name: 'Refuser' }));
    await userEvent.type(screen.getByLabelText('Motif du refus'), 'Lot déjà attribué');
    await userEvent.click(screen.getByRole('button', { name: 'Confirmer le refus' }));

    expect(rejectMutate).toHaveBeenCalledWith(
      { id: 'req-1', reason: 'Lot déjà attribué' },
      expect.anything(),
    );
  });

  // Une demande tranchée n'a plus de décision à prendre : laisser les boutons
  // invite à un clic qui ne peut que renvoyer une erreur.
  it("n'offre aucune action sur une demande déjà tranchée", () => {
    requests = [
      pendingRequest({ status: 'ACCEPTED', decidedByFullName: 'Karim Syndic', decidedAt: '2026-08-21T10:00:00Z' }),
    ];
    renderTab();

    expect(screen.queryByRole('button', { name: 'Accepter' })).not.toBeInTheDocument();
    expect(screen.getByText(/Par Karim Syndic/)).toBeInTheDocument();
  });
});
