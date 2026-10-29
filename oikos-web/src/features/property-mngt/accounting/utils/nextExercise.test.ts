import { describe, expect, it } from 'vitest';
import { nextExerciseDefaults, oldestOpenExercise } from '@/features/property-mngt/accounting/utils/nextExercise';
import type { AccountingExercise } from '@/features/property-mngt/accounting/types/accounting.types';

function exercise(overrides: Partial<AccountingExercise>): AccountingExercise {
  return {
    id: 'e-1',
    propertyId: 'p-1',
    label: 'Exercice 2026',
    startDate: '2026-01-01',
    endDate: '2026-12-31',
    status: 'OPEN',
    closedAt: null,
    closedByUserId: null,
    comment: null,
    ...overrides,
  };
}

describe('nextExerciseDefaults', () => {
  it("enchaîne un exercice civil sur l'année suivante", () => {
    expect(nextExerciseDefaults(exercise({}))).toEqual({
      label: 'Exercice 2027',
      startDate: '2027-01-01',
      endDate: '2027-12-31',
      comment: '',
    });
  });

  it('enchaîne un exercice décalé sur douze mois, à cheval sur deux années', () => {
    expect(nextExerciseDefaults(exercise({ startDate: '2025-07-01', endDate: '2026-06-30' }))).toMatchObject({
      label: 'Exercice 2026-2027',
      startDate: '2026-07-01',
      endDate: '2027-06-30',
    });
  });
});

describe('oldestOpenExercise', () => {
  it('ignore les exercices clôturés et retient le plus ancien des ouverts', () => {
    const exercises = [
      exercise({ id: '2025', status: 'CLOSED' }),
      exercise({ id: '2026' }),
      exercise({ id: '2027' }),
    ];

    expect(oldestOpenExercise(exercises)?.id).toBe('2026');
  });

  it("ne renvoie rien quand aucun exercice n'est ouvert", () => {
    expect(oldestOpenExercise([exercise({ status: 'CLOSED' })])).toBeUndefined();
  });
});
