import { describe, expect, it } from 'vitest';
import {
  buildAccountStatement,
  statementTotals,
} from '@/features/property-ownership/units/utils/accountStatement';
import { unitAccountBalance } from '@/features/property-ownership/units/utils/unitBalance';

const TODAY = '2026-08-31';

// L'exemple du métier : 2 000 versés en trois fois, 200 appelés par mois.
const installments = [
  { id: 'i-june', unitId: 'lot', dueDate: '2026-06-01', amount: 200, status: 'SETTLED' as const },
  { id: 'i-july', unitId: 'lot', dueDate: '2026-07-01', amount: 200, status: 'SETTLED' as const },
  { id: 'i-august', unitId: 'lot', dueDate: '2026-08-01', amount: 200, status: 'SETTLED' as const },
  { id: 'i-september', unitId: 'lot', dueDate: '2026-09-01', amount: 200, status: 'NOT_SETTLED' as const },
  { id: 'i-other', unitId: 'other-lot', dueDate: '2026-06-01', amount: 999, status: 'NOT_SETTLED' as const },
];

const payments = [
  { id: 'p-june', unitId: 'lot', valueDate: '2026-06-01', amount: 700, mode: 'CASH' as const },
  { id: 'p-july', unitId: 'lot', valueDate: '2026-07-01', amount: 300, mode: 'CHECK' as const },
  { id: 'p-august', unitId: 'lot', valueDate: '2026-08-01', amount: 1000, mode: 'BANK_TRANSFER' as const },
  { id: 'p-other', unitId: 'other-lot', valueDate: '2026-06-02', amount: 555, mode: 'CASH' as const },
];

describe('buildAccountStatement', () => {
  it('carries the calls at the debit and the payments at the credit, in date order', () => {
    const lines = buildAccountStatement(installments, payments, 'lot', TODAY);

    expect(lines.map((line) => line.id)).toEqual([
      'i-june',
      'p-june',
      'i-july',
      'p-july',
      'i-august',
      'p-august',
    ]);
    expect(lines[0]).toMatchObject({ date: '2026-06-01', debit: 200, credit: 0 });
    expect(lines[1]).toMatchObject({ date: '2026-06-01', debit: 0, credit: 700 });
  });

  it('names the nature of each operation, payment mode included', () => {
    const lines = buildAccountStatement(installments, payments, 'lot', TODAY);

    expect(lines[0].label).toBe('Appel de fonds');
    // Ce qui distingue deux règlements du même jour sur un relevé.
    expect(lines[1].label).toBe('Règlement — Espèces');
    expect(lines[3].label).toBe('Règlement — Chèque');
  });

  it('points each line at the detail screen of its own piece', () => {
    const lines = buildAccountStatement(installments, payments, 'lot', TODAY);

    expect(lines[0].to).toBe('/property-ownership/installments/i-june');
    expect(lines[1].to).toBe('/property-ownership/payments/p-june');
  });

  // Le point de la demande : le compte n'a pas encore été débité.
  it('leaves out a call that has not fallen due, and takes it in on its date', () => {
    expect(buildAccountStatement(installments, payments, 'lot', TODAY).map((line) => line.id)).not.toContain(
      'i-september',
    );
    expect(buildAccountStatement(installments, payments, 'lot', '2026-09-01').map((line) => line.id)).toContain(
      'i-september',
    );
  });

  // Un relevé est celui d'un lot, pas du copropriétaire.
  it('holds nothing from another lot', () => {
    const ids = buildAccountStatement(installments, payments, 'lot', TODAY).map((line) => line.id);

    expect(ids).not.toContain('i-other');
    expect(ids).not.toContain('p-other');
  });

  it('is empty for a lot with neither call nor payment', () => {
    expect(buildAccountStatement(installments, payments, 'unknown-lot', TODAY)).toEqual([]);
  });
});

describe('statementTotals', () => {
  it('totals each column and leaves the difference as the balance', () => {
    const totals = statementTotals(buildAccountStatement(installments, payments, 'lot', TODAY));

    expect(totals.debit).toBe(600);
    expect(totals.credit).toBe(2000);
    expect(totals.balance).toBe(1400);
  });

  // L'invariant qui compte : le pied du relevé doit retomber sur le badge qui
  // y mène, sinon l'un des deux se recompte à la main.
  it('lands on the very balance the lot badge shows, before and after a call falls due', () => {
    for (const asOf of [TODAY, '2026-09-01']) {
      const totals = statementTotals(buildAccountStatement(installments, payments, 'lot', asOf));

      expect(totals.balance).toBe(unitAccountBalance(installments, payments, 'lot', asOf));
    }
  });
});
