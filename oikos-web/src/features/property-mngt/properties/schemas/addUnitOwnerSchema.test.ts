import { describe, expect, it } from 'vitest';
import { addUnitOwnerSchema } from '@/features/property-mngt/properties/schemas/addUnitOwnerSchema';

describe('addUnitOwnerSchema', () => {
  it('accepts a valid payload', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'INDIVIDUAL',
      email: 'jane.doe@example.com',
      ownershipShare: 50,
    });

    expect(result.success).toBe(true);
  });

  it('rejects a blank full name', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: '',
      partyType: 'INDIVIDUAL',
      email: 'jane.doe@example.com',
      ownershipShare: 50,
    });

    expect(result.success).toBe(false);
  });

  it('rejects an invalid party type', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'NOT_A_TYPE',
      email: 'jane.doe@example.com',
      ownershipShare: 50,
    });

    expect(result.success).toBe(false);
  });

  it('rejects an invalid email', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'INDIVIDUAL',
      email: 'not-an-email',
      ownershipShare: 50,
    });

    expect(result.success).toBe(false);
  });

  it('rejects an ownership share above 100', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'INDIVIDUAL',
      email: 'jane.doe@example.com',
      ownershipShare: 150,
    });

    expect(result.success).toBe(false);
  });

  it('rejects a negative ownership share', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'INDIVIDUAL',
      email: 'jane.doe@example.com',
      ownershipShare: -1,
    });

    expect(result.success).toBe(false);
  });
});
