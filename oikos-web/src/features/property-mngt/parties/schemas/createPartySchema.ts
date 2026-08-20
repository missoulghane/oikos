import { z } from 'zod';
import { PARTY_TYPES } from '@/features/property-mngt/properties/types/property.types';
import { phoneSchema } from '@/features/identity/register/schemas/phoneSchema';

// Mirrors the bean validation constraints on CreatePartyRequest (oikos-api).
export const createPartySchema = z.object({
  fullName: z.string().trim().min(1, 'Le nom complet est requis').max(200, '200 caractères maximum'),
  partyType: z.enum(PARTY_TYPES, { error: 'Le type est requis' }),
  // Facultatif depuis qu'un contact peut n'avoir qu'un téléphone. Sans adresse,
  // aucune invitation ne part - c'est dit sous la case correspondante.
  email: z.union([z.literal(''), z.string().trim().max(150, '150 caractères maximum').email('Email invalide')]),
  phone: z.union([z.literal(''), phoneSchema]).optional(),
  /** Envoyer le lien de création de compte. Cochée par défaut côté formulaire. */
  invite: z.boolean(),
});

export type CreatePartyFormValues = z.infer<typeof createPartySchema>;
