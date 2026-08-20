import { describe, expect, it } from 'vitest';
import {
  getUnitBalanceColorClass,
  unitAccountBalance,
  unitStatementLink,
  unitDueCount,
} from '@/features/property-ownership/units/utils/unitBalance';

const PAST = '2026-01-15';
const FUTURE = '2026-12-01';
const TODAY = '2026-06-15';

const installments = [
  { unitId: 'unit-1', status: 'NOT_SETTLED' as const, dueDate: PAST, amount: 1000, outstandingAmount: 1000 },
  { unitId: 'unit-1', status: 'PARTIALLY_SETTLED' as const, dueDate: PAST, amount: 500, outstandingAmount: 200 },
  { unitId: 'unit-1', status: 'SETTLED' as const, dueDate: PAST, amount: 800, outstandingAmount: 0 },
  { unitId: 'unit-2', status: 'NOT_SETTLED' as const, dueDate: PAST, amount: 750, outstandingAmount: 750 },
  // Not owed yet - excluded from every balance below.
  { unitId: 'unit-1', status: 'NOT_SETTLED' as const, dueDate: FUTURE, amount: 5000, outstandingAmount: 5000 },
];

// unit-1 has been called 2 300 MAD so far (1000 + 500 + 800) and has paid 1 100.
const payments = [
  { unitId: 'unit-1', amount: 800 },
  { unitId: 'unit-1', amount: 300 },
  { unitId: 'unit-2', amount: 750 },
];

describe('unitAccountBalance', () => {
  it('is what has been paid minus what has fallen due, on the lot alone', () => {
    expect(unitAccountBalance(installments, payments, 'unit-1', TODAY)).toBe(1100 - 2300);
    expect(unitAccountBalance(installments, payments, 'unit-2', TODAY)).toBe(0);
  });

  it('is 0 for a lot with neither call nor payment', () => {
    expect(unitAccountBalance(installments, payments, 'unit-3', TODAY)).toBe(0);
  });

  // Le cas décrit par le métier : 2 000 versés, 600 appelés et échus au
  // 31 août → 1 400 ; l'appel de septembre ne pèse qu'une fois échu → 1 200.
  it('leaves a lot paying ahead in credit, and debits the next call only once due', () => {
    const calls = [
      { unitId: 'lot', status: 'SETTLED' as const, dueDate: '2026-06-01', amount: 200 },
      { unitId: 'lot', status: 'SETTLED' as const, dueDate: '2026-07-01', amount: 200 },
      { unitId: 'lot', status: 'SETTLED' as const, dueDate: '2026-08-01', amount: 200 },
      { unitId: 'lot', status: 'NOT_SETTLED' as const, dueDate: '2026-09-01', amount: 200 },
    ];
    const paid = [
      { unitId: 'lot', amount: 700 },
      { unitId: 'lot', amount: 300 },
      { unitId: 'lot', amount: 1000 },
    ];

    expect(unitAccountBalance(calls, paid, 'lot', '2026-08-31')).toBe(1400);
    expect(unitAccountBalance(calls, paid, 'lot', '2026-09-01')).toBe(1200);
  });

  // The regression the "reste à payer" figure could not express: someone who
  // has overpaid is not "à jour", they are in credit, and the figure has to
  // say so rather than bottom out at zero.
  it('goes above zero, unlike the outstanding total it replaces', () => {
    const calls = [{ unitId: 'lot', status: 'SETTLED' as const, dueDate: PAST, amount: 200 }];

    expect(unitAccountBalance(calls, [{ unitId: 'lot', amount: 900 }], 'lot', TODAY)).toBe(700);
  });

  // outstandingAmount is already net of the payments imputed on the line;
  // summing both sides would count a payment twice.
  it('debits the amount called, not what is left to pay on it', () => {
    const calls = [{ unitId: 'lot', status: 'PARTIALLY_SETTLED' as const, dueDate: PAST, amount: 500, outstandingAmount: 200 }];

    expect(unitAccountBalance(calls, [{ unitId: 'lot', amount: 300 }], 'lot', TODAY)).toBe(-200);
  });
});

describe('unitDueCount', () => {
  it('counts the echeances of the lot still unsettled and already due', () => {
    expect(unitDueCount(installments, 'unit-1', TODAY)).toBe(2);
    expect(unitDueCount(installments, 'unit-2', TODAY)).toBe(1);
  });

  it('is 0 for a lot with nothing owed', () => {
    expect(unitDueCount(installments, 'unit-3', TODAY)).toBe(0);
  });

  it('leaves out an echeance not fallen due yet, and counts it once it has', () => {
    expect(unitDueCount(installments, 'unit-1', TODAY)).toBe(2);
    expect(unitDueCount(installments, 'unit-1', FUTURE)).toBe(3);
  });
});

describe('unitStatementLink', () => {
  // Quel que soit le signe : le relevé porte les deux côtés du compte, là où
  // une liste d'échéances ou de versements n'en montre qu'un.
  it('points at the statement of that very lot', () => {
    expect(unitStatementLink('p1', 'unit-1')).toBe('/property-ownership/units/p1/unit-1/statement');
  });
});

describe('getUnitBalanceColorClass', () => {
  it('paints a lot in the red as a debt', () => {
    expect(getUnitBalanceColorClass(-5000)).toMatch(/error/);
    expect(getUnitBalanceColorClass(-1)).toMatch(/error/);
  });

  // Being ahead is not a warning, and neither is owing exactly nothing.
  it('paints a settled or credit balance green', () => {
    expect(getUnitBalanceColorClass(0)).toMatch(/success/);
    expect(getUnitBalanceColorClass(1400)).toMatch(/success/);
  });
});
