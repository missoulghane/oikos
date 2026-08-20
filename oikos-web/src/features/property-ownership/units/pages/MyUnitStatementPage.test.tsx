import { describe, expect, it, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

const THIS_LOT = 'unit-1';

const myUnits: OwnedUnit[] = [
  { unitId: THIS_LOT, unitNumber: 'A1', buildingId: 'b1', buildingName: 'Bât A', propertyId: 'p1', propertyName: 'Al Amal', ownershipShare: 60 },
];

// Relative to today: a hard-coded future date would quietly stop being in the
// future and turn the "not yet due" assertion vacuous.
function isoInAYear(): string {
  const date = new Date();
  date.setFullYear(date.getFullYear() + 1);
  return date.toLocaleDateString('sv-SE');
}
const NOT_YET_DUE = isoInAYear();

const installmentsByUnit: Record<string, unknown[]> = {
  [THIS_LOT]: [
    { id: 'i1', unitId: THIS_LOT, dueDate: '2026-06-01', amount: 200, outstandingAmount: 0, status: 'SETTLED', period: null },
    { id: 'i2', unitId: THIS_LOT, dueDate: '2026-07-01', amount: 200, outstandingAmount: 0, status: 'SETTLED', period: null },
    { id: 'i3', unitId: THIS_LOT, dueDate: '2026-08-01', amount: 200, outstandingAmount: 0, status: 'SETTLED', period: null },
    // Ni échue ni impayée : réglée d'avance. C'est sa date qui la tient dehors.
    { id: 'i4', unitId: THIS_LOT, dueDate: NOT_YET_DUE, amount: 9500, outstandingAmount: 0, status: 'SETTLED', period: null },
  ],
};

const paymentsByUnit: Record<string, unknown[]> = {
  [THIS_LOT]: [
    { id: 'p1', propertyId: 'p1', unitId: THIS_LOT, mode: 'CASH', valueDate: '2026-06-01', amount: 700, journalEntryId: 'j1' },
    { id: 'p2', propertyId: 'p1', unitId: THIS_LOT, mode: 'CHECK', valueDate: '2026-07-01', amount: 300, journalEntryId: 'j2' },
    { id: 'p3', propertyId: 'p1', unitId: THIS_LOT, mode: 'BANK_TRANSFER', valueDate: '2026-08-01', amount: 1000, journalEntryId: 'j3' },
  ],
};

vi.mock('@/features/property-mngt/installments/hooks/useUnitInstallments', () => ({
  useUnitInstallments: (unitId: string) => ({
    data: installmentsByUnit[unitId] ?? [],
    isLoading: false,
    isError: false,
    error: null,
  }),
}));
vi.mock('@/features/property-mngt/installments/hooks/useUnitPayments', () => ({
  useUnitPayments: (unitId: string) => ({
    data: paymentsByUnit[unitId] ?? [],
    isLoading: false,
    isError: false,
    error: null,
  }),
}));
vi.mock('@/features/property-ownership/units/hooks/useMyUnits', () => ({
  useMyUnits: () => ({ data: myUnits, isLoading: false, error: null }),
}));

const { MyUnitStatementPage } = await import('@/features/property-ownership/units/pages/MyUnitStatementPage');

function renderPage() {
  return render(
    <MemoryRouter initialEntries={[`/property-ownership/units/p1/${THIS_LOT}/statement`]}>
      <Routes>
        <Route path="/property-ownership/units/:propertyId/:unitId/statement" element={<MyUnitStatementPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

function bodyRows() {
  const rowGroups = screen.getAllByRole('rowgroup');
  // rowgroups are thead / tbody / tfoot in DOM order
  return within(rowGroups[1]).getAllByRole('row');
}

describe('MyUnitStatementPage', () => {
  it('lists the operations that moved the account, in date order', () => {
    renderPage();

    const rows = bodyRows();
    expect(rows).toHaveLength(6);
    // Same date: the debit is listed before the credit that answers it.
    expect(rows[0]).toHaveTextContent('01/06/2026');
    expect(rows[0]).toHaveTextContent('200 MAD');
    expect(rows[1]).toHaveTextContent('700 MAD');
  });

  it('names the nature of each operation and opens its detail screen', () => {
    renderPage();

    const rows = bodyRows();
    expect(within(rows[0]).getByRole('link', { name: 'Appel de fonds' })).toHaveAttribute(
      'href',
      '/property-ownership/installments/i1',
    );
    expect(within(rows[1]).getByRole('link', { name: 'Règlement — Espèces' })).toHaveAttribute(
      'href',
      '/property-ownership/payments/p1',
    );
  });

  it('names the lot the statement covers', () => {
    renderPage();

    // En tête du relevé, et sur chacune de ses lignes.
    expect(screen.getAllByText('Al Amal — Bât A — Lot A1')).toHaveLength(1 + bodyRows().length);
    expect(bodyRows()[0]).toHaveTextContent('Al Amal — Bât A — Lot A1');
  });

  // Le cœur de la demande : rien n'a encore été débité pour cet appel.
  it('leaves out a call that has not fallen due, settled or not', () => {
    renderPage();

    expect(screen.queryByText(/9 500/)).not.toBeInTheDocument();
  });

  it('totals each column and states the balance', () => {
    renderPage();

    const footer = screen.getAllByRole('rowgroup')[2];
    expect(within(footer).getByText('600 MAD')).toBeInTheDocument();
    expect(within(footer).getByText('2 000 MAD')).toBeInTheDocument();
    expect(within(footer).getByText('1 400 MAD')).toBeInTheDocument();
  });

  it('paints a credit balance as such', () => {
    renderPage();

    expect(screen.getByText('1 400 MAD').className).toMatch(/success/);
  });

  it('goes back to the lot it belongs to', () => {
    renderPage();

    expect(screen.getByRole('link', { name: /Retour au lot/ })).toHaveAttribute(
      'href',
      `/property-ownership/units/p1/${THIS_LOT}`,
    );
  });
});
