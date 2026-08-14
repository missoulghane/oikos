import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';
import type { OwnedInstallment } from '@/features/property-ownership/installments/types/ownedInstallment.types';
import type { CurrentUser } from '@/features/identity/me/types/me.types';

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
  // No echeance at all - covers the "À jour" branch of the balance badge.
  { unitId: 'unit-3', unitNumber: 'C3', buildingId: 'b3', buildingName: 'Bat C', propertyId: 'p3', propertyName: 'Yasmine', ownershipShare: 25 },
];

const installments: OwnedInstallment[] = [
  { id: 'a', unitId: 'unit-1', dueDate: '2026-01-15', amount: 1000, outstandingAmount: 1000, status: 'NOT_SETTLED' },
  { id: 'b', unitId: 'unit-2', dueDate: '2026-03-10', amount: 500, outstandingAmount: 200, status: 'PARTIALLY_SETTLED' },
  { id: 'c', unitId: 'unit-1', dueDate: '2026-06-01', amount: 800, outstandingAmount: 0, status: 'SETTLED' },
];

// Mutable so one test can give the owner a board mandate; the factory only
// reads it when the component calls it, well after this module has initialised.
let mandateIds: string[] = [];

vi.mock('@/features/identity/me', () => ({
  useCurrentUser: () => ({ data: owner, isLoading: false }),
  boardPropertyIds: () => mandateIds,
  hasCopro: () => true,
}));
vi.mock('@/shared/hooks/useEffectiveSpace', () => ({
  useEffectiveSpace: () => ({ kind: 'owner' }),
}));
vi.mock('@/features/property-ownership/units/hooks/useMyUnits', () => ({
  useMyUnits: () => ({ data: units, isLoading: false, isError: false, error: null }),
}));
vi.mock('@/features/property-ownership/installments/hooks/useMyInstallments', () => ({
  useMyInstallments: () => ({ data: installments, isLoading: false, error: null }),
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

describe('DashboardPage (owner space)', () => {
  beforeEach(() => {
    mandateIds = [];
  });

  // "Mes lots" was folded into the dashboard - it is no longer a screen of its own.
  it('lists the owner lots, each linking to its detail', () => {
    renderDashboard();

    expect(screen.getByText('Al Amal')).toBeInTheDocument();
    expect(screen.getByText('Nour')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Al Amal/ })).toHaveAttribute(
      'href',
      '/property-ownership/units/p1/unit-1',
    );
  });

  it('shows what each lot still owes, on the lot itself', () => {
    renderDashboard();

    // unit-1 owes 1000 (its other echeance is settled), unit-2 owes 200
    expect(screen.getByRole('link', { name: /Al Amal/ })).toHaveTextContent('1 000 MAD');
    expect(screen.getByRole('link', { name: /Nour/ })).toHaveTextContent('200 MAD');
    // unit-3 has no echeance at all
    expect(screen.getByRole('link', { name: /Yasmine/ })).toHaveTextContent('À jour');
  });

  it('no longer shows the summary badges it used to carry', () => {
    renderDashboard();

    expect(screen.queryByRole('link', { name: /^Mes lots/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Mes échéances · total à régler/ })).not.toBeInTheDocument();
  });

  it('still greets the owner by first name', () => {
    renderDashboard();

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Rachid');
  });

  // Explicitly kept while the rest of the dashboard was stripped: it is the
  // only way for a mixed account to reach its board space from here.
  it('keeps the switch to the board space for an owner who also sits on a bureau', () => {
    mandateIds = ['p1'];
    renderDashboard();

    expect(screen.getByRole('link', { name: /bureau/i })).toHaveAttribute(
      'href',
      '/dashboard?space=board&propertyId=p1',
    );
  });

  it('shows no such switch for a plain owner', () => {
    renderDashboard();

    expect(screen.queryByRole('link', { name: /bureau/i })).not.toBeInTheDocument();
  });
});
