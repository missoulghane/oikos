import { describe, expect, it, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import type { OwnedInstallment } from '@/features/property-ownership/installments/types/ownedInstallment.types';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

const units: OwnedUnit[] = [
  { unitId: 'unit-1', unitNumber: 'A1', buildingId: 'b1', buildingName: 'Bat A', propertyId: 'p1', propertyName: 'Al Amal', ownershipShare: 100 },
  { unitId: 'unit-2', unitNumber: 'B2', buildingId: 'b2', buildingName: 'Bat B', propertyId: 'p1', propertyName: 'Al Amal', ownershipShare: 50 },
];

const installments: OwnedInstallment[] = [
  { id: 'a', unitId: 'unit-1', dueDate: '2026-01-15', amount: 1000, outstandingAmount: 1000, status: 'NOT_SETTLED' },
  { id: 'b', unitId: 'unit-2', dueDate: '2026-03-10', amount: 500, outstandingAmount: 200, status: 'PARTIALLY_SETTLED' },
  { id: 'c', unitId: 'unit-1', dueDate: '2026-06-01', amount: 800, outstandingAmount: 0, status: 'SETTLED' },
];

vi.mock('@/features/property-ownership/installments/hooks/useMyInstallments', () => ({
  useMyInstallments: () => ({ data: installments, isLoading: false, error: null }),
}));
vi.mock('@/features/property-ownership/units/hooks/useMyUnits', () => ({
  useMyUnits: () => ({ data: units, isLoading: false, error: null }),
}));

const { MyInstallmentsPage } = await import('@/features/property-ownership/installments/pages/MyInstallmentsPage');

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/property-ownership/installments']}>
      <Routes>
        <Route path="/property-ownership/installments" element={<MyInstallmentsPage />} />
        <Route path="/property-ownership/installments/:installmentId" element={<h1>Détail échéance</h1>} />
      </Routes>
    </MemoryRouter>,
  );
}

function bodyRowCount() {
  const rowGroups = screen.getAllByRole('rowgroup');
  // rowgroups are thead / tbody / tfoot in DOM order
  return within(rowGroups[1]).getAllByRole('row').length;
}

describe('MyInstallmentsPage', () => {
  it('shows the total still owed, ignoring settled echeances', () => {
    renderPage();

    // 1000 (NOT_SETTLED) + 200 (PARTIALLY_SETTLED), the settled one contributes 0
    expect(screen.getByRole('button', { name: /Total à régler/ })).toHaveTextContent('1 200 MAD');
  });

  it('lists every echeance by default', () => {
    renderPage();

    expect(bodyRowCount()).toBe(3);
  });

  it('narrows the table to what is still owed when the total badge is clicked', async () => {
    renderPage();
    await userEvent.click(screen.getByRole('button', { name: /Total à régler/ }));

    expect(bodyRowCount()).toBe(2);
    expect(screen.getByRole('button', { name: /Total à régler/ })).toHaveAttribute('aria-pressed', 'true');
  });

  it('restores the full list when the badge is clicked a second time', async () => {
    renderPage();
    const badge = screen.getByRole('button', { name: /Total à régler/ });
    await userEvent.click(badge);
    await userEvent.click(badge);

    expect(bodyRowCount()).toBe(3);
    expect(badge).toHaveAttribute('aria-pressed', 'false');
  });

  it('filters by lot', async () => {
    renderPage();
    await userEvent.selectOptions(screen.getByLabelText('Lot'), 'unit-1');

    expect(bodyRowCount()).toBe(2);
  });

  it('combines the lot filter with the badge filter', async () => {
    renderPage();
    await userEvent.selectOptions(screen.getByLabelText('Lot'), 'unit-1');
    await userEvent.click(screen.getByRole('button', { name: /Total à régler/ }));

    // unit-1 has one unsettled ('a') and one settled ('c') echeance
    expect(bodyRowCount()).toBe(1);
  });

  it('tells the user when the filters exclude everything', async () => {
    renderPage();
    await userEvent.selectOptions(screen.getByLabelText('Lot'), 'unit-2');
    await userEvent.selectOptions(screen.getByLabelText('Statut'), 'SETTLED');

    expect(screen.getByText('Aucune échéance ne correspond à ces filtres.')).toBeInTheDocument();
  });

  it('narrows the list from the search box by lot', async () => {
    renderPage();
    await userEvent.type(screen.getByRole('searchbox'), 'B2');

    // Lot B2 is unit-2, which carries a single echeance
    expect(bodyRowCount()).toBe(1);
  });

  it('matches labels typed without their accents', async () => {
    renderPage();
    await userEvent.type(screen.getByRole('searchbox'), 'partiellement soldee');

    // "Partiellement soldée" - the row is found despite the missing accent
    expect(bodyRowCount()).toBe(1);
  });

  it('searches on the amount as well as the lot', async () => {
    renderPage();
    await userEvent.type(screen.getByRole('searchbox'), '1000');

    expect(bodyRowCount()).toBe(1);
  });

  it('counts an active search among the filters, and clears it on reset', async () => {
    renderPage();
    await userEvent.type(screen.getByRole('searchbox'), 'B2');
    expect(screen.getByRole('button', { name: /^Filtres/ })).toHaveTextContent('1');

    await userEvent.click(screen.getByRole('button', { name: /Réinitialiser/ }));

    expect(screen.getByRole('searchbox')).toHaveValue('');
    expect(bodyRowCount()).toBe(3);
  });

  it('says so when the search matches nothing', async () => {
    renderPage();
    await userEvent.type(screen.getByRole('searchbox'), 'introuvable');

    expect(screen.getByText('Aucune échéance ne correspond à ces filtres.')).toBeInTheDocument();
  });

  it('offers no clear action until a filter is applied', () => {
    renderPage();

    expect(screen.queryByRole('button', { name: /Réinitialiser/ })).not.toBeInTheDocument();
  });

  it('restores the full list from "Réinitialiser"', async () => {
    renderPage();
    await userEvent.selectOptions(screen.getByLabelText('Lot'), 'unit-1');
    expect(bodyRowCount()).toBe(2);

    await userEvent.click(screen.getByRole('button', { name: /Réinitialiser/ }));

    expect(bodyRowCount()).toBe(3);
    expect(screen.getByLabelText('Lot')).toHaveValue('');
  });

  it('counts the badge filter among the active filters', async () => {
    renderPage();
    await userEvent.click(screen.getByRole('button', { name: /Total à régler/ }));

    expect(screen.getByRole('button', { name: /^Filtres/ })).toHaveTextContent('1');
  });

  it('no longer offers sorting as filter fields', () => {
    renderPage();

    expect(screen.queryByLabelText('Trier par')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Ordre')).not.toBeInTheDocument();
  });

  it('marks the column the table is sorted on, and only that one', () => {
    renderPage();

    // Default sort: due date, most recent first
    expect(screen.getByRole('columnheader', { name: /Échéance/ })).toHaveAttribute('aria-sort', 'descending');
    expect(screen.getByRole('columnheader', { name: /Montant/ })).toHaveAttribute('aria-sort', 'none');
  });

  // Rows are identified by their date: amounts render with a narrow no-break
  // space (U+202F) from toLocaleString('fr-FR'), which is a trap to assert on.
  function firstRowText() {
    const rowGroups = screen.getAllByRole('rowgroup');
    return within(rowGroups[1]).getAllByRole('row')[0].textContent;
  }

  it('flips the direction when the active column is clicked again', async () => {
    renderPage();
    expect(firstRowText()).toContain('01/06/2026');

    await userEvent.click(screen.getByRole('button', { name: /Échéance/ }));

    expect(screen.getByRole('columnheader', { name: /Échéance/ })).toHaveAttribute('aria-sort', 'ascending');
    expect(firstRowText()).toContain('15/01/2026');
  });

  it('starts a newly picked column at descending', async () => {
    renderPage();
    await userEvent.click(screen.getByRole('button', { name: /Montant/ }));

    expect(screen.getByRole('columnheader', { name: /Montant/ })).toHaveAttribute('aria-sort', 'descending');
    expect(screen.getByRole('columnheader', { name: /Échéance/ })).toHaveAttribute('aria-sort', 'none');
    // The 1000 MAD echeance, largest of the three, comes first
    expect(firstRowText()).toContain('15/01/2026');
  });

  it('opens the detail page when a row is clicked', async () => {
    renderPage();
    const rowGroups = screen.getAllByRole('rowgroup');
    await userEvent.click(within(rowGroups[1]).getAllByRole('row')[0]);

    expect(screen.getByRole('heading', { name: 'Détail échéance' })).toBeInTheDocument();
  });
});
