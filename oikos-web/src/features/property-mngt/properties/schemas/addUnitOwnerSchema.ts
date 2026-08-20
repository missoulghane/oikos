import { z } from 'zod';
import { PARTY_TYPES } from '@/features/property-mngt/properties/types/property.types';
import { phoneSchema } from '@/features/identity/register/schemas/phoneSchema';

// Mirrors the bean validation constraints on AddUnitOwnerRequest (oikos-api).
export const addUnitOwnerSchema = z.object({
  fullName: z.string().trim().min(1, 'Le nom complet est requis').max(200, '200 caractères maximum'),
  partyType: z.enum(PARTY_TYPES, { error: 'Le type est requis' }),
  // Facultatif depuis qu'un copropriétaire peut n'avoir qu'un téléphone. Sans
  // adresse, aucune invitation ne part - c'est dit sous la case correspondante.
  email: z.union([z.literal(''), z.string().trim().max(150, '150 caractères maximum').email('Email invalide')]),
  // Facultatif, mais au format international quand il est là : c'est sur cette
  // valeur que le serveur reconnaît un contact déjà enregistré (uk_party_property_phone).
  phone: z.union([z.literal(''), phoneSchema]).optional(),
  ownershipShare: z
    .number({ error: 'La part de propriété est requise' })
    .min(0, 'Ne peut pas être négatif')
    .max(100, 'Ne peut pas dépasser 100'),
  /** Envoyer le lien de création de compte. Coché par défaut côté formulaire. */
  invite: z.boolean(),
});

export type AddUnitOwnerFormValues = z.infer<typeof addUnitOwnerSchema>;
