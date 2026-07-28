import { describe, expect, it } from 'vitest';
import { addUnitOwnershipSchema } from '@/features/property-mngt/properties/schemas/addUnitOwnershipSchema';

describe('addUnitOwnershipSchema', () => {
  it('accepts a valid payload', () => {
    const result = addUnitOwnershipSchema.safeParse({
      partyId: 'a1b2c3d4-0000-0000-0000-000000000001',
      ownershipShare: 50,
    });

    expect(result.success).toBe(true);
  });

  it('rejects a blank partyId', () => {
    const result = addUnitOwnershipSchema.safeParse({
      partyId: '',
      ownershipShare: 50,
    });

    expect(result.success).toBe(false);
  });

  it('rejects an ownership share above 100', () => {
    const result = addUnitOwnershipSchema.safeParse({
      partyId: 'a1b2c3d4-0000-0000-0000-000000000001',
      ownershipShare: 150,
    });

    expect(result.success).toBe(false);
  });

  it('rejects a negative ownership share', () => {
    const result = addUnitOwnershipSchema.safeParse({
      partyId: 'a1b2c3d4-0000-0000-0000-000000000001',
      ownershipShare: -1,
    });

    expect(result.success).toBe(false);
  });
});
