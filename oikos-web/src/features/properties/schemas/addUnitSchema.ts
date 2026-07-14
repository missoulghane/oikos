import { z } from 'zod';
import { UNIT_TYPES } from '@/features/properties/types/property.types';

// Mirrors the bean validation constraints on AddUnitRequest (oikos-api).
export const addUnitSchema = z.object({
  unitNumber: z.string().trim().min(1, 'Le numéro de lot est requis').max(20, '20 caractères maximum'),
  unitType: z.enum(UNIT_TYPES, { error: 'Le type de lot est requis' }),
  shares: z
    .number({ error: 'Les tantièmes sont requis' })
    .min(0, 'Ne peut pas être négatif'),
});

export type AddUnitFormValues = z.infer<typeof addUnitSchema>;
