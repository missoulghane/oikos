import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import type { CurrentUser } from '@/features/identity/me/types/me.types';

const syndic: CurrentUser = {
  id: 'user-1',
  fullName: 'Nadia Berrada',
  email: 'nadia@oikos.com',
  phone: null,
  roles: [],
  roleByProperty: {},
  verified: true,
  enabled: true,
  hasAvatar: false,
};

// Mutable so one test can take the accounting permission away without a second
// mock factory; the getters only read them when the component renders.
let canWrite = true;
let ownsAlso = false;

vi.mock('@/features/identity/me', () => ({
  useCurrentUser: () => ({ data: syndic, isLoading: false }),
  boardPropertyIds: () => ['property-1'],
  hasCopro: () => ownsAlso,
  canWriteAccounting: () => canWrite,
}));
vi.mock('@/shared/hooks/useEffectiveSpace', () => ({
  useEffectiveSpace: () => ({ kind: 'board', propertyId: 'property-1' }),
}));
vi.mock('@/shared/hooks/useMandateProperties', () => ({
  useMandateProperties: () => ({ isLoading: false, byId: new Map() }),
}));
vi.mock('@/features/property-mngt/properties/hooks/useProperty', () => ({
  useProperty: () => ({
    data: { id: 'property-1', name: 'Résidence Al Amal', address: '12 rue des Orangers' },
    isLoading: false,
    isError: false,
  }),
}));
vi.mock('@/features/property-mngt/properties/hooks/useProperties', () => ({ useProperties: () => ({}) }));
vi.mock('@/features/property-mngt/properties/hooks/usePropertyUnitCount', () => ({
  usePropertyUnitCount: () => ({ data: 24 }),
}));
vi.mock('@/features/property-mngt/accounting/hooks/useLedgerAccounts', () => ({
  useLedgerAccounts: () => ({
    data: [
      { id: 'acc-1', accountNumber: '5161', label: 'Caisse', role: 'CASH', balance: 12500 },
      { id: 'acc-2', accountNumber: '5141', label: 'Banque Populaire', role: 'BANK', balance: 88000 },
      // Not a treasury account: it must not appear as a balance badge.
      { id: 'acc-3', accountNumber: '6110', label: 'Charges', role: null, balance: 400 },
    ],
    isError: false,
  }),
}));
vi.mock('@/features/property-mngt/installments', () => ({
  useInstallmentCollectionSummary: () => ({ data: { count: 7, amount: 21000 }, isError: false }),
}));
vi.mock('@/features/identity/onboarding/components/ResumeOnboardingBanner', () => ({
  ResumeOnboardingBanner: () => null,
}));

const { DashboardPage } = await import('@/shared/pages/DashboardPage');

function renderDashboard() {
  return render(
    <MemoryRouter initialEntries={['/dashboard']}>
      <Routes>
        <Route path="/dashboard" element={<DashboardPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('DashboardPage (board space)', () => {
  beforeEach(() => {
    canWrite = true;
    ownsAlso = false;
  });

  it('names the copropriété, its address and its lots, and opens its general information', () => {
    renderDashboard();

    const card = screen.getByRole('link', { name: /Résidence Al Amal/ });
    expect(card).toHaveAttribute('href', '/property-mngt/properties/property-1/property');
    expect(screen.getByText('12 rue des Orangers')).toBeInTheDocument();
    expect(screen.getByText('24 lots')).toBeInTheDocument();
  });

  it('shows the treasury balances, and only those', () => {
    // The same component as the accounting overview: a balance read differently on two
    // screens is a balance the syndic goes and checks somewhere else.
    renderDashboard();

    expect(screen.getByText(/5161 — Caisse/)).toBeInTheDocument();
    expect(screen.getByText(/5141 — Banque Populaire/)).toBeInTheDocument();
    expect(screen.queryByText(/6110 — Charges/)).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Caisse/ })).toHaveAttribute(
      'href',
      '/property-mngt/properties/property-1/accounting/treasury-accounts/acc-1',
    );
  });

  it('opens the tracking list on exactly the set it counted', () => {
    // "à venir" is this screen's default-off, so the link only has to name the status -
    // and the 7 it announces is the number of rows that come up.
    renderDashboard();

    const link = screen.getByRole('link', { name: /À collecter/ });
    expect(link).toHaveAttribute('href', '/property-mngt/properties/property-1/installments?status=NOT_SETTLED');
    expect(screen.getByText('21 000 MAD')).toBeInTheDocument();
    expect(screen.getByText(/7 échéances non soldées et échues/)).toBeInTheDocument();
  });

  // Le nom accessible du lien porte désormais son sous-titre (« Appel de fonds,
  // règlement… ») : la recherche se fait donc sur le début du libellé.
  it('offers the two entries a syndic makes daily', () => {
    renderDashboard();

    expect(screen.getByRole('link', { name: /Saisir une recette/ })).toHaveAttribute(
      'href',
      '/property-mngt/properties/property-1/accounting/receipts/new',
    );
    expect(screen.getByRole('link', { name: /Saisir une dépense/ })).toHaveAttribute(
      'href',
      '/property-mngt/properties/property-1/accounting/supplier-payments/new',
    );
  });

  it('nomme l’espace conseil syndical sous la salutation', () => {
    // Le même compte peut être copropriétaire d'un côté et au conseil de
    // l'autre : « Bonjour X » seul ne disait pas où l'on venait d'arriver.
    renderDashboard();

    expect(screen.getByText('Bienvenue dans votre espace conseil syndical')).toBeInTheDocument();
  });

  it('annonce ce qu’on va saisir, pas seulement le geste', () => {
    // Ce que la carte apporte sur le bouton : la place d'un sous-titre.
    renderDashboard();

    expect(screen.getByText('Appel de fonds, règlement…')).toBeInTheDocument();
    expect(screen.getByText('Facture, fournisseur, paiement…')).toBeInTheDocument();
  });

  it('place les deux saisies avant les chiffres qu’elles alimentent', () => {
    // Elles étaient sous les soldes, c'est-à-dire après ce qu'elles servent à
    // remplir : on les cherchait. L'ordre du DOM est ce qui se lit en premier.
    renderDashboard();

    const receipt = screen.getByRole('link', { name: /Saisir une recette/ });
    const collection = screen.getByText('À collecter');

    expect(receipt.compareDocumentPosition(collection) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
  });

  it('hides both entries from whoever may not write the accounts', () => {
    // A button leading to a form that will refuse is not a shortcut.
    canWrite = false;
    renderDashboard();

    expect(screen.queryByRole('link', { name: /Saisir une recette/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Saisir une dépense/ })).not.toBeInTheDocument();
  });

  // La bascule d'espace appartient au sélecteur de l'en-tête (SpaceSwitcher),
  // présent sur tous les écrans : le tableau de bord n'en porte plus de copie.
  it('carries no space switch of its own, even for a syndic who owns a lot here', () => {
    ownsAlso = true;
    renderDashboard();

    expect(screen.queryByText(/espace copropriétaire/i)).not.toBeInTheDocument();
  });
});
