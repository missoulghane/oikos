import { describe, expect, it } from 'vitest';
import { generateInstallmentCallSchema } from '@/features/property-mngt/installments/schemas/generateInstallmentCallSchema';

describe('generateInstallmentCallSchema', () => {
  it('accepte une échéance postérieure à la période, même sur l’exercice suivant', () => {
    expect(generateInstallmentCallSchema.safeParse({ period: '2026-12', dueDate: '2027-01-05' }).success).toBe(true);
  });

  it('accepte une échéance le premier jour de la période', () => {
    expect(generateInstallmentCallSchema.safeParse({ period: '2026-03', dueDate: '2026-03-01' }).success).toBe(true);
  });

  it('refuse une échéance antérieure au début de la période', () => {
    const result = generateInstallmentCallSchema.safeParse({ period: '2026-03', dueDate: '2026-02-28' });

    expect(result.success).toBe(false);
    expect(result.error?.issues[0]?.path).toEqual(['dueDate']);
  });
});
