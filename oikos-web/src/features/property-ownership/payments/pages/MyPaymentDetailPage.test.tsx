import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import type { Payment } from '@/features/property-mngt/installments/types/payment.types';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

const PAYMENT_ID = 'pay-1';

const payments: Payment[] = [
  { id: PAYMENT_ID, propertyId: 'p1', unitId: 'unit-1', mode: 'CHECK', valueDate: '2026-03-15', amount: 2500, journalEntryId: 'j1' },
];

const units: OwnedUnit[] = [
  { unitId: 'unit-1', unitNumber: 'A12', buildingId: 'b1', buildingName: 'Bât A', propertyId: 'p1', propertyName: 'Al Amal', ownershipShare: 100 },
];

const downloadMutate = vi.fn();
let downloadState: { isPending: boolean; isError: boolean; error: unknown } = {
  isPending: false,
  isError: false,
  error: null,
};

vi.mock('@/features/property-ownership/payments/hooks/useMyPayments', () => ({
  useMyPayments: () => ({ data: payments, isLoading: false, isError: false, error: null }),
}));
vi.mock('@/features/property-ownership/units/hooks/useMyUnits', () => ({
  useMyUnits: () => ({ data: units, isLoading: false, isError: false, error: null }),
}));
vi.mock('@/features/property-mngt/installments/hooks/useDownloadPaymentReceipt', () => ({
  useDownloadPaymentReceipt: () => ({ mutate: downloadMutate, ...downloadState }),
}));

const { MyPaymentDetailPage } = await import('@/features/property-ownership/payments/pages/MyPaymentDetailPage');

function renderPage() {
  return render(
    <MemoryRouter initialEntries={[`/property-ownership/payments/${PAYMENT_ID}`]}>
      <Routes>
        <Route path="/property-ownership/payments/:paymentId" element={<MyPaymentDetailPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('MyPaymentDetailPage receipt', () => {
  it('offers the receipt of this payment', async () => {
    downloadState = { isPending: false, isError: false, error: null };
    renderPage();

    await userEvent.click(screen.getByRole('button', { name: 'Télécharger le reçu' }));

    expect(downloadMutate).toHaveBeenCalledWith({ paymentId: PAYMENT_ID, fileName: `recu-${PAYMENT_ID}.pdf` });
  });

  // A payment predating receipts, or one whose generation failed, has none. The
  // endpoint answers 404 and the page has to say so rather than echo a raw error.
  it('explains a missing receipt instead of surfacing the raw failure', () => {
    downloadState = {
      isPending: false,
      isError: true,
      error: { isAxiosError: true, response: { status: 404 }, message: 'Request failed with status code 404' },
    };
    renderPage();

    expect(screen.getByText(/Aucun reçu n'est disponible/)).toBeInTheDocument();
    expect(screen.queryByText(/status code 404/)).not.toBeInTheDocument();
  });

  it('surfaces any other failure as-is', () => {
    downloadState = {
      isPending: false,
      isError: true,
      error: { isAxiosError: true, response: { status: 500, data: { message: 'Boom' } }, message: 'Boom' },
    };
    renderPage();

    expect(screen.getByText('Boom')).toBeInTheDocument();
  });
});
