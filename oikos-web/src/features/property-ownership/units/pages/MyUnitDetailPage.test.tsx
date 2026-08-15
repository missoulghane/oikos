import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

const THIS_LOT = 'unit-1';

const myUnits: OwnedUnit[] = [
  { unitId: THIS_LOT, unitNumber: 'A1', buildingId: 'b1', buildingName: 'Bât A', propertyId: 'p1', propertyName: 'Al Amal', ownershipShare: 60 },
  { unitId: 'unit-2', unitNumber: 'B2', buildingId: 'b2', buildingName: 'Bât B', propertyId: 'p1', propertyName: 'Nour', ownershipShare: 40 },
];

// Relative to today: a hard-coded future date would quietly stop being in the
// future and turn the "not yet due" assertions vacuous.
function isoInAYear(): string {
  const date = new Date();
  date.setFullYear(date.getFullYear() + 1);
  return date.toLocaleDateString('sv-SE');
}
const NOT_YET_DUE = isoInAYear();

// Only this lot's rows; the other lot's would be a bug if they showed up.
const installmentsByUnit: Record<string, unknown[]> = {
  // Seven rows so the top-5 cap is actually exercised; only i1 and i2 are still
  // owed, which keeps the balance assertions at 1 200 MAD / 2 échéances.
  [THIS_LOT]: [
    { id: 'i1', unitId: THIS_LOT, dueDate: '2026-01-15', amount: 1000, outstandingAmount: 1000, status: 'NOT_SETTLED', period: null },
    { id: 'i2', unitId: THIS_LOT, dueDate: '2026-03-10', amount: 500, outstandingAmount: 200, status: 'PARTIALLY_SETTLED', period: null },
    { id: 'i3', unitId: THIS_LOT, dueDate: '2026-06-01', amount: 800, outstandingAmount: 0, status: 'SETTLED', period: null },
    { id: 'i4', unitId: THIS_LOT, dueDate: '2026-08-01', amount: 800, outstandingAmount: 0, status: 'SETTLED', period: null },
    { id: 'i5', unitId: THIS_LOT, dueDate: '2026-02-01', amount: 800, outstandingAmount: 0, status: 'SETTLED', period: null },
    // the two oldest - cut off by the top-5
    { id: 'i6', unitId: THIS_LOT, dueDate: '2026-01-10', amount: 800, outstandingAmount: 0, status: 'SETTLED', period: null },
    { id: 'i7', unitId: THIS_LOT, dueDate: '2026-01-05', amount: 800, outstandingAmount: 0, status: 'SETTLED', period: null },
    // Latest date of all, so it would head the top-5 if it were not excluded.
    { id: 'i8', unitId: THIS_LOT, dueDate: NOT_YET_DUE, amount: 9500, outstandingAmount: 9500, status: 'NOT_SETTLED', period: null },
  ],
  'unit-2': [
    { id: 'other', unitId: 'unit-2', dueDate: '2026-02-02', amount: 4242, outstandingAmount: 4242, status: 'NOT_SETTLED', period: null },
  ],
};

const paymentsByUnit: Record<string, unknown[]> = {
  [THIS_LOT]: [{ id: 'p1', propertyId: 'p1', unitId: THIS_LOT, mode: 'CHECK', valueDate: '2026-02-01', amount: 300, journalEntryId: 'j1' }],
  'unit-2': [{ id: 'pother', propertyId: 'p1', unitId: 'unit-2', mode: 'CASH', valueDate: '2026-02-05', amount: 9999, journalEntryId: 'j2' }],
};

const useUnitInstallmentsSpy = vi.fn();
const useUnitPaymentsSpy = vi.fn();

vi.mock('@/features/property-mngt/properties/hooks/useUnit', () => ({
  useUnit: () => ({
    data: {
      id: THIS_LOT,
      buildingId: 'b1',
      unitNumber: 'A1',
      unitTypeId: 't1',
      unitTypeName: 'Appartement',
      shares: 120,
      ownershipStatus: 'AFFECTED',
      ownerFullNames: ['Rachid Tazi'],
    },
    isLoading: false,
    isError: false,
    error: null,
  }),
}));
vi.mock('@/features/property-ownership/units/hooks/useMyUnits', () => ({
  useMyUnits: () => ({ data: myUnits, isLoading: false, error: null }),
}));
vi.mock('@/features/property-mngt/installments/hooks/useUnitInstallments', () => ({
  useUnitInstallments: (unitId: string) => {
    useUnitInstallmentsSpy(unitId);
    return { data: installmentsByUnit[unitId] ?? [], isLoading: false, isError: false, error: null };
  },
}));
vi.mock('@/features/property-mngt/installments/hooks/useUnitPayments', () => ({
  useUnitPayments: (unitId: string) => {
    useUnitPaymentsSpy(unitId);
    return { data: paymentsByUnit[unitId] ?? [], isLoading: false, isError: false, error: null };
  },
}));
vi.mock('@/features/property-mngt/properties/hooks/useUnitOwners', () => ({
  useUnitOwners: () => ({
    data: [
      { id: 'o1', partyId: 'party-1', partyFullName: 'Rachid Tazi', partyType: 'INDIVIDUAL', partyEmail: 'user5@oikos.com', ownershipShare: 60 },
    ],
    isLoading: false,
    isError: false,
    error: null,
  }),
}));
vi.mock('@/features/property-mngt/installments/hooks/useRegularizeUnitInstallments', () => ({
  useRegularizeUnitInstallments: () => ({ mutate: vi.fn(), isPending: false, isError: false, isSuccess: false }),
}));
vi.mock('@/features/identity/me', () => ({
  useCurrentUser: () => ({ data: { id: 'user-5' } }),
  canWriteAccounting: () => false,
}));
// Never rendered here (canManage={false}), but UnitOwnersSection imports them
// statically and they drag in httpClient -> env.ts, which has no VITE_API_URL
// under Vitest. Factory form for the same reason, see UnitList.test.tsx.
vi.mock('@/features/property-mngt/properties/components/AddUnitOwnerForm', () => ({
  AddUnitOwnerForm: () => null,
}));
vi.mock('@/features/property-mngt/properties/components/AddExistingUnitOwnerForm', () => ({
  AddExistingUnitOwnerForm: () => null,
}));

const { MyUnitDetailPage } = await import('@/features/property-ownership/units/pages/MyUnitDetailPage');

function renderPage() {
  return render(
    <MemoryRouter initialEntries={[`/property-ownership/units/p1/${THIS_LOT}`]}>
      <Routes>
        <Route path="/property-ownership/units/:propertyId/:unitId" element={<MyUnitDetailPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('MyUnitDetailPage', () => {
  it('shows what the lot owes as a signed balance, and how many echeances that covers', () => {
    renderPage();

    // 1000 + 200; the settled one contributes nothing
    expect(screen.getByText('-1 200 MAD')).toBeInTheDocument();
    expect(screen.getByText('2 échéances à régler')).toBeInTheDocument();
  });

  it('colours an amount owed as a debt, not as a healthy balance', () => {
    renderPage();

    expect(screen.getByText('-1 200 MAD').className).toMatch(/error/);
  });

  // A balance should lead to what makes it up - and to this lot's share of it,
  // not to every lot's echeances beside a figure that excludes them.
  it('opens the unpaid echeances of this lot from the balance', () => {
    renderPage();

    expect(screen.getByRole('link', { name: /Solde à régler/ })).toHaveAttribute(
      'href',
      `/property-ownership/installments?status=DUE&unitId=${THIS_LOT}`,
    );
  });

  it('replaces the owners block with "Informations générales", keeping the owner listed', () => {
    renderPage();

    expect(screen.getByRole('heading', { name: 'Informations générales' })).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Propriétaires' })).not.toBeInTheDocument();
    // the owner themselves is still shown, inside the new block
    expect(screen.getByText('Rachid Tazi')).toBeInTheDocument();
  });

  it('lists the lot characteristics that actually exist', () => {
    renderPage();

    expect(screen.getByText('Tantièmes')).toBeInTheDocument();
    expect(screen.getByText('120')).toBeInTheDocument();
    expect(screen.getByText('Votre quote-part')).toBeInTheDocument();
    expect(screen.getByText('60 %')).toBeInTheDocument();
    expect(screen.getByText('Bât A')).toBeInTheDocument();
  });

  // The whole point of the tabs rework: these must be the lot's own rows.
  it('asks the API for this lot only, never for the whole account', () => {
    renderPage();

    expect(useUnitInstallmentsSpy).toHaveBeenCalledWith(THIS_LOT);
    expect(useUnitInstallmentsSpy).not.toHaveBeenCalledWith('unit-2');
  });

  it('shows this lot\'s echeances and none from another lot', () => {
    renderPage();

    expect(screen.getByText(/Échéance du 15\/01\/2026/)).toBeInTheDocument();
    // 4 242 MAD belongs to unit-2
    expect(screen.queryByText(/4 242/)).not.toBeInTheDocument();
  });

  it('keeps the echeances not yet fallen due out of "Dernières échéances"', () => {
    renderPage();

    // Sorted by date descending, i8 would take the first of the five slots and
    // push out an echeance actually to pay - and it is not owed yet anyway,
    // which the balance above already reflects by ignoring its 9 500 MAD.
    expect(screen.queryByText(/9 500/)).not.toBeInTheDocument();
    expect(screen.getByText('-1 200 MAD')).toBeInTheDocument();
    expect(screen.getByText('2 échéances à régler')).toBeInTheDocument();
  });

  it('shows both recent-operation blocks at once, no tabs to switch', () => {
    renderPage();

    expect(screen.getByRole('heading', { name: 'Dernières échéances' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Derniers paiements' })).toBeInTheDocument();
    expect(screen.queryAllByRole('tab')).toHaveLength(0);
  });

  it("lists this lot's payments, and only this lot's", () => {
    renderPage();

    expect(screen.getByText(/Paiement du 01\/02\/2026/)).toBeInTheDocument();
    expect(useUnitPaymentsSpy).toHaveBeenCalledWith(THIS_LOT);
    // 9 999 MAD belongs to unit-2
    expect(screen.queryByText(/9 999/)).not.toBeInTheDocument();
  });

  it('links each echeance and each payment to its own detail screen', () => {
    renderPage();

    expect(screen.getByRole('link', { name: /Échéance du 15\/01\/2026/ })).toHaveAttribute(
      'href',
      '/property-ownership/installments/i1',
    );
    expect(screen.getByRole('link', { name: /Paiement du 01\/02\/2026/ })).toHaveAttribute(
      'href',
      '/property-ownership/payments/p1',
    );
  });

  it('caps each block at the five most recent, newest first', () => {
    renderPage();

    const rows = screen.getAllByRole('link', { name: /Échéance du/ });
    expect(rows).toHaveLength(5);
    // 2026-08-01 is the most recent of the seven
    expect(rows[0]).toHaveTextContent('01/08/2026');
    // the two oldest fall off the end
    expect(screen.queryByText(/Échéance du 10\/01\/2026/)).not.toBeInTheDocument();
    expect(screen.queryByText(/Échéance du 05\/01\/2026/)).not.toBeInTheDocument();
  });

  it('sends the reader to the full list from each block', () => {
    renderPage();

    const seeAll = screen.getAllByRole('link', { name: 'Tout voir' });
    expect(seeAll[0]).toHaveAttribute('href', '/property-ownership/installments');
    expect(seeAll[1]).toHaveAttribute('href', '/property-ownership/payments');
  });

  it('no longer shows the "Affecté" status in the header', () => {
    renderPage();

    expect(screen.queryByText('Affecté')).not.toBeInTheDocument();
  });
});
