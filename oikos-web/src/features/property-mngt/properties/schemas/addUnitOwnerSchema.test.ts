import { describe, expect, it } from 'vitest';
import { addUnitOwnerSchema } from '@/features/property-mngt/properties/schemas/addUnitOwnerSchema';

describe('addUnitOwnerSchema', () => {
  it('accepts a valid payload', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'INDIVIDUAL',
      email: 'jane.doe@example.com',
      ownershipShare: 50,
      invite: true,
    });

    expect(result.success).toBe(true);
  });

  it('rejects a blank full name', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: '',
      partyType: 'INDIVIDUAL',
      email: 'jane.doe@example.com',
      ownershipShare: 50,
      invite: true,
    });

    expect(result.success).toBe(false);
  });

  it('rejects an invalid party type', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'NOT_A_TYPE',
      email: 'jane.doe@example.com',
      ownershipShare: 50,
      invite: true,
    });

    expect(result.success).toBe(false);
  });

  it('rejects an invalid email', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'INDIVIDUAL',
      email: 'not-an-email',
      ownershipShare: 50,
      invite: true,
    });

    expect(result.success).toBe(false);
  });

  it('rejects an ownership share above 100', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'INDIVIDUAL',
      email: 'jane.doe@example.com',
      ownershipShare: 150,
      invite: true,
    });

    expect(result.success).toBe(false);
  });

  it('accepts a valid payload with an international phone number', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'INDIVIDUAL',
      email: 'jane.doe@example.com',
      phone: '+212612345678',
      ownershipShare: 50,
      invite: true,
    });

    expect(result.success).toBe(true);
  });

  it('accepts an empty phone', () => {
    // Le champ reste facultatif : PhoneField rend '' tant que rien n'est saisi.
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'INDIVIDUAL',
      email: 'jane.doe@example.com',
      phone: '',
      ownershipShare: 50,
      invite: true,
    });

    expect(result.success).toBe(true);
  });

  it('rejects a national phone number', () => {
    // C'est sur la valeur stockée, au format international, que le serveur
    // reconnaît un contact déjà enregistré : « 0612345678 » ne matcherait rien.
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'INDIVIDUAL',
      email: 'jane.doe@example.com',
      phone: '0612345678',
      ownershipShare: 50,
      invite: true,
    });

    expect(result.success).toBe(false);
  });

  it('rejects a negative ownership share', () => {
    const result = addUnitOwnerSchema.safeParse({
      fullName: 'Jane Doe',
      partyType: 'INDIVIDUAL',
      email: 'jane.doe@example.com',
      ownershipShare: -1,
      invite: true,
    });

    expect(result.success).toBe(false);
  });
});
