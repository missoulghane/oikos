import { describe, expect, it } from 'vitest';
import { createPropertySchema } from '@/features/property-mngt/properties/schemas/createPropertySchema';

describe('createPropertySchema', () => {
  it('accepts a valid payload', () => {
    const result = createPropertySchema.safeParse({
      name: 'Résidence Les Oliviers',
      address: '12 rue de la Paix, Casablanca',
    });

    expect(result.success).toBe(true);
  });

  it('rejects a blank name', () => {
    const result = createPropertySchema.safeParse({
      name: '',
      address: '12 rue de la Paix, Casablanca',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a blank address', () => {
    const result = createPropertySchema.safeParse({
      name: 'Résidence Les Oliviers',
      address: '',
    });

    expect(result.success).toBe(false);
  });
});
