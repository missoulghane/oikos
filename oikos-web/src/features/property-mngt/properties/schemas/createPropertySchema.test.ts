import { describe, expect, it } from 'vitest';
import { createPropertySchema } from '@/features/property-mngt/properties/schemas/createPropertySchema';

describe('createPropertySchema', () => {
  it('accepts a valid payload', () => {
    const result = createPropertySchema.safeParse({
      name: 'Résidence Les Oliviers',
      address: '12 rue de la Paix',
      city: 'Casablanca',
    });

    expect(result.success).toBe(true);
  });

  it('accepts an empty city', () => {
    // Facultative de bout en bout : les copropriétés créées avant l'existence du
    // champ n'en ont pas, et l'exiger bloquerait la correction d'un simple nom.
    const result = createPropertySchema.safeParse({
      name: 'Résidence Les Oliviers',
      address: '12 rue de la Paix',
      city: '',
    });

    expect(result.success).toBe(true);
  });

  it('rejects a blank name', () => {
    const result = createPropertySchema.safeParse({
      name: '',
      address: '12 rue de la Paix',
      city: 'Casablanca',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a blank address', () => {
    const result = createPropertySchema.safeParse({
      name: 'Résidence Les Oliviers',
      address: '',
      city: 'Casablanca',
    });

    expect(result.success).toBe(false);
  });
});
