import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';
import type { OwnedInstallment } from '@/features/property-ownership/installments/types/ownedInstallment.types';
import type { Payment } from '@/features/property-mngt/installments/types/payment.types';
import type { CurrentUser } from '@/features/identity/me/types/me.types';
import type { OwnedMembershipRequest } from '@/features/property-ownership/membership-requests/types/membershipRequest.types';

const owner: CurrentUser = {
  id: 'user-5',
  fullName: 'Rachid Tazi',
  email: 'user5@oikos.com',
  phone: null,
  roles: [],
  roleByProperty: {},
  verified: true,
  enabled: true,
  hasAvatar: false,
};

const units: OwnedUnit[] = [
  { unitId: 'unit-1', unitNumber: 'A1', buildingId: 'b1', buildingName: 'Bat A', propertyId: 'p1', propertyName: 'Al Amal', ownershipShare: 100 },
  { unitId: 'unit-2', unitNumber: 'B2', buildingId: 'b2', buildingName: 'Bat B', propertyId: 'p2', propertyName: 'Nour', ownershipShare: 50 },
  // No echeance at all - covers the zero branch of the balance badge.
  { unitId: 'unit-3', unitNumber: 'C3', buildingId: 'b3', buildingName: 'Bat C', propertyId: 'p3', propertyName: 'Yasmine', ownershipShare: 25 },
];

const installments: OwnedInstallment[] = [
  { id: 'a', unitId: 'unit-1', dueDate: '2026-01-15', amount: 1000, outstandingAmount: 1000, status: 'NOT_SETTLED' },
  { id: 'b', unitId: 'unit-2', dueDate: '2026-03-10', amount: 500, outstandingAmount: 200, status: 'PARTIALLY_SETTLED' },
  { id: 'c', unitId: 'unit-1', dueDate: '2026-06-01', amount: 800, outstandingAmount: 0, status: 'SETTLED' },
];

// The other side of the account, and consistent with the outstanding amounts
// above: unit-1 has been called 1 800 and paid 800, unit-2 called 500 and paid
// 300. unit-3 has neither.
const payments: Payment[] = [
  { id: 'pay-1', propertyId: 'p1', unitId: 'unit-1', mode: 'CASH', valueDate: '2026-06-02', amount: 800, journalEntryId: 'j1' },
  { id: 'pay-2', propertyId: 'p2', unitId: 'unit-2', mode: 'CHECK', valueDate: '2026-03-11', amount: 300, journalEntryId: 'j2' },
];

// Mutable so one test can give the owner a board mandate; the factory only
// reads it when the component calls it, well after this module has initialised.
let mandateIds: string[] = [];

// Idem : les lots validés et les demandes en attente varient d'un cas à
// l'autre - un compte tout neuf n'a que la seconde, un copropriétaire déjà
// installé a les deux.
let ownedUnits: OwnedUnit[] = units;
let membershipRequests: OwnedMembershipRequest[] = [];

const pendingRequest: OwnedMembershipRequest = {
  id: 'req-1',
  propertyName: 'Les Jardins',
  unitNumber: 'D4',
  unitTypeName: 'Appartement',
  status: 'PENDING',
  decidedAt: null,
  rejectionReason: null,
};

vi.mock('@/features/identity/me', () => ({
  useCurrentUser: () => ({ data: owner, isLoading: false }),
  boardPropertyIds: () => mandateIds,
  hasCopro: () => true,
}));
vi.mock('@/shared/hooks/useEffectiveSpace', () => ({
  useEffectiveSpace: () => ({ kind: 'owner' }),
}));
vi.mock('@/features/property-ownership/units/hooks/useMyUnits', () => ({
  useMyUnits: () => ({ data: ownedUnits, isLoading: false, isError: false, error: null }),
}));
vi.mock('@/features/property-ownership/membership-requests/hooks/useMyMembershipRequests', () => ({
  useMyMembershipRequests: () => ({ data: membershipRequests, isLoading: false, isError: false, error: null }),
}));
vi.mock('@/features/property-ownership/installments/hooks/useMyInstallments', () => ({
  useMyInstallments: () => ({ data: installments, isLoading: false, error: null }),
}));
vi.mock('@/features/property-ownership/payments/hooks/useMyPayments', () => ({
  useMyPayments: () => ({ data: payments, isLoading: false, error: null }),
}));
vi.mock('@/shared/hooks/useMandateProperties', () => ({
  useMandateProperties: () => ({ isLoading: false, byId: new Map() }),
}));
vi.mock('@/features/property-mngt/properties/hooks/useProperty', () => ({ useProperty: () => ({}) }));
vi.mock('@/features/property-mngt/properties/hooks/useProperties', () => ({ useProperties: () => ({}) }));
vi.mock('@/features/identity/onboarding/components/ResumeOnboardingBanner', () => ({
  ResumeOnboardingBanner: () => null,
}));

const { DashboardPage } = await import('@/shared/pages/DashboardPage');

function renderDashboard() {
  return render(
    <MemoryRouter initialEntries={['/dashboard']}>
      <Routes>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/property-ownership/units" element={<h2>Écran mes lots</h2>} />
        <Route path="/property-ownership/installments" element={<h2>Écran mes échéances</h2>} />
      </Routes>
    </MemoryRouter>,
  );
}

/** La carte d'un lot : elle est cliquable en entier, et mène à sa fiche. */
function cardOf(propertyName: string) {
  return screen.getByRole('link', { name: new RegExp(propertyName) });
}

describe('DashboardPage (owner space)', () => {
  beforeEach(() => {
    mandateIds = [];
    ownedUnits = units;
    membershipRequests = [];
  });

  // "Mes lots" was folded into the dashboard - it is no longer a screen of its own.
  it('lists the owner lots, each linking to its detail', () => {
    renderDashboard();

    expect(screen.getByText('Al Amal')).toBeInTheDocument();
    expect(screen.getByText('Nour')).toBeInTheDocument();
    expect(cardOf('Al Amal')).toHaveAttribute('href', '/property-ownership/units/p1/unit-1');
  });

  // Le tableau de bord annonce le solde, il ne l'ouvre pas : c'est le badge de
  // la fiche du lot qui mène au relevé.
  it('leads to the lot itself, never straight to its statement', () => {
    renderDashboard();

    expect(screen.queryByRole('link', { name: /Relevé/ })).not.toBeInTheDocument();
    expect(cardOf('Al Amal')).toHaveAttribute('href', '/property-ownership/units/p1/unit-1');
  });

  it('shows the balance of each lot account, on the lot itself', () => {
    renderDashboard();

    // unit-1: 1 800 appelés et échus, 800 versés. unit-2: 500 contre 300.
    expect(cardOf('Al Amal')).toHaveTextContent('-1 000 MAD');
    expect(cardOf('Nour')).toHaveTextContent('-200 MAD');
    // unit-3 has no echeance at all: it still states its balance, in figures -
    // "À jour" alone left the copropriétaire without the number that proves it.
    expect(cardOf('Yasmine')).toHaveTextContent('0 MAD');
    expect(cardOf('Yasmine')).toHaveTextContent('À jour');
  });

  it('no longer shows the summary badges it used to carry', () => {
    renderDashboard();

    expect(screen.queryByRole('link', { name: /^Mes lots/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Mes échéances · total à régler/ })).not.toBeInTheDocument();
  });

  // Le titre nomme désormais la personne puis l'espace : un même compte peut
  // être copropriétaire ici et au conseil syndical là, et rien ne le disait.
  // « Mes lots » n'a pas disparu, il est redevenu l'intitulé de la liste.
  it('salue le copropriétaire et nomme son espace', () => {
    renderDashboard();

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Rachid');
    expect(screen.getByText('Bienvenue dans votre espace copropriétaire')).toBeInTheDocument();
    expect(screen.getByRole('heading', { level: 2, name: 'Mes lots' })).toBeInTheDocument();
  });

  it('no longer repeats the ownership share on each lot card', () => {
    renderDashboard();

    expect(screen.queryByText(/des tantièmes/)).not.toBeInTheDocument();
  });

  it('shows a debtor lot as a signed balance, worded and sized like the lot page', () => {
    renderDashboard();

    const card = cardOf('Al Amal');
    expect(card).toHaveTextContent('Solde du compte');
    expect(card).toHaveTextContent('-1 000 MAD');
    expect(card).toHaveTextContent('1 échéance à régler');
    // Same type scale as MyUnitDetailPage's balance: one figure read twice at
    // two sizes is a figure people go and check somewhere else.
    expect(screen.getByText('-1 000 MAD').className).toMatch(/text-2xl/);
  });

  it('colours the balance on the card as a debt, not as a healthy balance', () => {
    renderDashboard();

    expect(screen.getByText('-1 000 MAD').className).toMatch(/error/);
    expect(screen.getByText('0 MAD').className).toMatch(/success/);
  });

  /**
   * Le lot demandé via un lien d'invitation public, que le syndic n'a pas
   * encore validé. Il n'apparaissait nulle part : « Mes lots » ne lit que les
   * affectations validées, et la demande ne vivait que sur « Mes invitations »,
   * un écran sans entrée de menu.
   */
  it('affiche le lot en attente à côté des lots déjà validés', () => {
    membershipRequests = [pendingRequest];
    renderDashboard();

    expect(screen.getByText('Les Jardins')).toBeInTheDocument();
    expect(screen.getByText('En attente de validation')).toBeInTheDocument();
  });

  // Non cliquable, délibérément : le rôle n'est posé qu'à la validation, une
  // carte cliquable mènerait à un 403.
  it("ne rend pas le lot en attente cliquable", () => {
    membershipRequests = [pendingRequest];
    renderDashboard();

    expect(screen.queryByRole('link', { name: /Les Jardins/ })).not.toBeInTheDocument();
  });

  // Le cas d'un compte qui vient d'accepter l'invitation : aucun lot validé.
  // Lui annoncer « vous n'êtes propriétaire d'aucun lot » contredirait la
  // demande qu'il vient de déposer.
  it("remplace l'état vide par le lot en attente quand c'est tout ce qu'il y a", () => {
    ownedUnits = [];
    membershipRequests = [pendingRequest];
    renderDashboard();

    expect(screen.queryByText('Aucun lot')).not.toBeInTheDocument();
    expect(screen.getByText('En attente de validation')).toBeInTheDocument();
  });

  // Une demande refusée ne mènera à aucun lot : elle n'a plus rien à faire sur
  // le tableau de bord.
  it('ne montre aucun badge pour une demande refusée', () => {
    ownedUnits = [];
    membershipRequests = [{ ...pendingRequest, status: 'REJECTED', rejectionReason: 'Lot déjà attribué' }];
    renderDashboard();

    expect(screen.queryByText('En attente de validation')).not.toBeInTheDocument();
    expect(screen.getByText('Aucun lot')).toBeInTheDocument();
  });

  // La bascule vers l'espace conseil syndical vit dans le sélecteur d'espace
  // de l'en-tête (SpaceSwitcher), présent sur tous les écrans : le tableau de
  // bord n'en affiche plus de rappel, mandat ou pas.
  it('carries no board-mandate card, even for an owner who also sits on a bureau', () => {
    mandateIds = ['p1'];
    renderDashboard();

    expect(screen.queryByRole('link', { name: /bureau/i })).not.toBeInTheDocument();
  });
});
