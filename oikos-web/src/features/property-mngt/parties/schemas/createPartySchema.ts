import { z } from 'zod';
import { PARTY_TYPES } from '@/features/property-mngt/properties/types/property.types';

// Mirrors the bean validation constraints on CreatePartyRequest (oikos-api).
export const createPartySchema = z.object({
  fullName: z.string().trim().min(1, 'Le nom complet est requis').max(200, '200 caractères maximum'),
  partyType: z.enum(PARTY_TYPES, { error: 'Le type est requis' }),
  email: z.string().trim().min(1, "L'email est requis").max(150, '150 caractères maximum').email('Email invalide'),
  phone: z.string().trim().max(20, '20 caractères maximum').optional(),
});

export type CreatePartyFormValues = z.infer<typeof createPartySchema>;
