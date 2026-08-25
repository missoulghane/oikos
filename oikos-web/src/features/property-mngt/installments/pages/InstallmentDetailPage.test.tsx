import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const installment = {
  id: 'i1',
  unitId: 'u1',
  // Null on the single-echeance endpoint, which is exactly why the page resolves
  // the lot through useUnit rather than reading it off the installment.
  unitNumber: null,
  dueDate: '2026-01-15',
  amount: 1000,
  outstandingAmount: 400,
  status: 'PARTIALLY_SETTLED' as const,
  period: '2026-01',
};

vi.mock('@/features/property-mngt/installments/api/getInstallment', () => ({
  getInstallment: () => Promise.resolve(installment),
}));
vi.mock('@/features/property-mngt/properties/hooks/useUnit', () => ({
  useUnit: () => ({
    data: { id: 'u1', unitNumber: 'A12', unitTypeName: 'Appartement', ownerFullNames: ['Karim Alaoui'] },
  }),
}));
vi.mock('@/features/property-mngt/installments/components/UnitPaymentsSection', () => ({
  UnitPaymentsSection: () => <div>Paiements du lot (section)</div>,
}));

const { InstallmentDetailPage } = await import(
  '@/features/property-mngt/installments/pages/InstallmentDetailPage'
);

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/property-mngt/properties/p1/installments/i1']}>
        <Routes>
          <Route
            path="/property-mngt/properties/:propertyId/installments/:installmentId"
            element={<InstallmentDetailPage />}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('InstallmentDetailPage', () => {
  it('shows the echeance, what is left on it, and what has already been settled', async () => {
    renderPage();

    expect(await screen.findByText('Échéance du 15/01/2026')).toBeInTheDocument();
    expect(screen.getByText('1 000 MAD')).toBeInTheDocument();
    expect(screen.getByText('600 MAD')).toBeInTheDocument();
    expect(screen.getByText('400 MAD')).toBeInTheDocument();
    expect(screen.getByText('Partiellement soldée')).toBeInTheDocument();
  });

  /** The lot and its owners are the first thing looked up on an unpaid line. */
  it('resolves the lot and links to it', async () => {
    renderPage();

    const lotLink = await screen.findByRole('link', { name: 'A12 — Appartement' });
    expect(lotLink).toHaveAttribute('href', '/property-mngt/properties/p1/units/u1');
    expect(screen.getByText('Karim Alaoui')).toBeInTheDocument();
  });
});
