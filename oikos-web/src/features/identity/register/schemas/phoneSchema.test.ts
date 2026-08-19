import { describe, expect, it } from 'vitest';
import { phoneSchema } from '@/features/identity/register/schemas/phoneSchema';

describe('phoneSchema', () => {
  it('accepte un numéro international', () => {
    expect(phoneSchema.safeParse('+212612345678').success).toBe(true);
  });

  it('exige le numéro', () => {
    expect(phoneSchema.safeParse('').success).toBe(false);
  });

  it('refuse un numéro national sans indicatif', () => {
    expect(phoneSchema.safeParse('0612345678').success).toBe(false);
  });

  it('refuse un indicatif seul', () => {
    expect(phoneSchema.safeParse('+212').success).toBe(false);
  });

  it('refuse plus de chiffres que la norme E.164 n’en admet', () => {
    expect(phoneSchema.safeParse('+2126123456789012').success).toBe(false);
  });
});
