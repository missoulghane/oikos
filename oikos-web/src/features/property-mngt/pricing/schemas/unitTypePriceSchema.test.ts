import { describe, expect, it } from 'vitest';
import { unitTypePriceSchema } from '@/features/property-mngt/pricing/schemas/unitTypePriceSchema';

describe('unitTypePriceSchema', () => {
  it('accepts a valid price', () => {
    const result = unitTypePriceSchema.safeParse({ price: 300 });

    expect(result.success).toBe(true);
  });

  it('accepts zero', () => {
    const result = unitTypePriceSchema.safeParse({ price: 0 });

    expect(result.success).toBe(true);
  });

  it('rejects a negative price', () => {
    const result = unitTypePriceSchema.safeParse({ price: -1 });

    expect(result.success).toBe(false);
  });

  it('rejects a missing price', () => {
    const result = unitTypePriceSchema.safeParse({ price: NaN });

    expect(result.success).toBe(false);
  });
});
