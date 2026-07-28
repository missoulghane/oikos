import { z } from 'zod';

// Mirrors the bean validation constraints on AddUnitOwnershipRequest (oikos-api).
export const addUnitOwnershipSchema = z.object({
  partyId: z.string().trim().min(1, 'La party est requise'),
  ownershipShare: z
    .number({ error: 'La part de propriété est requise' })
    .min(0, 'Ne peut pas être négatif')
    .max(100, 'Ne peut pas dépasser 100'),
});

export type AddUnitOwnershipFormValues = z.infer<typeof addUnitOwnershipSchema>;
