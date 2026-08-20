import { describe, expect, it, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';
import type { Payment } from '@/features/property-mngt/installments/types/payment.types';

const units: OwnedUnit[] = [
  { unitId: 'unit-1', unitNumber: 'A1', buildingId: 'b1', buildingName: 'Bat A', propertyId: 'p1', propertyName: 'Al Amal', ownershipShare: 100 },
  { unitId: 'unit-2', unitNumber: 'B2', buildingId: 'b2', buildingName: 'Bat B', propertyId: 'p1', propertyName: 'Al Amal', ownershipShare: 50 },
];

const payments: Payment[] = [
  { id: 'a', propertyId: 'p1', unitId: 'unit-1', mode: 'BANK_TRANSFER', valueDate: '2026-01-20', amount: 1000, journalEntryId: 'j1' },
  { id: 'b', propertyId: 'p1', unitId: 'unit-2', mode: 'CASH', valueDate: '2026-04-05', amount: 250, journalEntryId: 'j2' },
];

vi.mock('@/features/property-ownership/payments/hooks/useMyPayments', () => ({
  useMyPayments: () => ({ data: payments, isLoading: false, error: null }),
}));
vi.mock('@/features/property-ownership/units/hooks/useMyUnits', () => ({
  useMyUnits: () => ({ data: units, isLoading: false, error: null }),
}));

const { MyPaymentsPage } = await import('@/features/property-ownership/payments/pages/MyPaymentsPage');

function renderPage(entry = '/property-ownership/payments') {
  return render(
    <MemoryRouter initialEntries={[entry]}>
      <Routes>
        <Route path="/property-ownership/payments" element={<MyPaymentsPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

function bodyRowCount() {
  const rowGroups = screen.getAllByRole('rowgroup');
  // rowgroups are thead / tbody in DOM order
  return within(rowGroups[1]).getAllByRole('row').length;
}

describe('MyPaymentsPage', () => {
  it('lists every payment when opened plainly', () => {
    renderPage();

    expect(bodyRowCount()).toBe(2);
  });

  // C'est la destination d'un solde à jour ou créditeur sur la fiche d'un lot :
  // ouvrir la page sur tous les versements de l'utilisateur répondrait à côté.
  it('opens on a single lot when the URL names one', () => {
    renderPage('/property-ownership/payments?unitId=unit-2');

    // Scoped to the body: the total in the footer repeats the same figure.
    const body = screen.getAllByRole('rowgroup')[1];
    expect(bodyRowCount()).toBe(1);
    expect(within(body).getByText(/250/)).toBeInTheDocument();
    expect(within(body).queryByText(/1 000/)).not.toBeInTheDocument();
  });

  // Seeded, jamais synchronisé : à partir de là les filtres appartiennent à
  // l'utilisateur, sans que la query string ne les lui reprenne.
  it('lets the lot filter go once the user clears it', async () => {
    renderPage('/property-ownership/payments?unitId=unit-2');

    await userEvent.click(screen.getByRole('button', { name: /Réinitialiser|Effacer/ }));

    expect(bodyRowCount()).toBe(2);
  });
});
