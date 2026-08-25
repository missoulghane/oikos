import { z } from 'zod';
import { PARTY_TYPES } from '@/features/property-mngt/properties/types/property.types';
import { phoneSchema } from '@/features/identity/register/schemas/phoneSchema';

// Mirrors the bean validation constraints on UpdatePartyRequest (oikos-api) -
// mêmes règles qu'à la création (createPartySchema), sans la copropriété ni
// l'invitation, qui ne se rejouent pas sur une fiche existante.
export const updatePartySchema = z.object({
  fullName: z.string().trim().min(1, 'Le nom complet est requis').max(200, '200 caractères maximum'),
  partyType: z.enum(PARTY_TYPES, { error: 'Le type est requis' }),
  email: z.union([z.literal(''), z.string().trim().max(150, '150 caractères maximum').email('Email invalide')]),
  phone: z.union([z.literal(''), phoneSchema]).optional(),
});

export type UpdatePartyFormValues = z.infer<typeof updatePartySchema>;
