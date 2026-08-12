import { z } from 'zod';
import { refinePasswordsMatch } from '@/features/identity/register/schemas/passwordConfirmation';

/**
 * Unique règle appliquée par l'API (RawPassword.MIN_LENGTH) : la checklist de
 * l'étape 1 se contente de la rendre lisible pendant la frappe. Y ajouter une
 * contrainte ici sans la poser côté serveur créerait une divergence, et l'y
 * poser côté serveur invaliderait les mots de passe déjà en base.
 */
export const PASSWORD_MIN_LENGTH = 10;

export const accountStepSchema = refinePasswordsMatch(
  z.object({
    firstName: z.string().trim().min(1, 'Le prénom est requis').max(100, '100 caractères maximum'),
    lastName: z.string().trim().min(1, 'Le nom est requis').max(100, '100 caractères maximum'),
    email: z.string().trim().min(1, "L'email est requis").email('Email invalide').max(150, '150 caractères maximum'),
    password: z.string().min(PASSWORD_MIN_LENGTH, `Au moins ${PASSWORD_MIN_LENGTH} caractères`),
    confirmPassword: z.string().min(1, 'La confirmation est requise'),
  }),
);

export type AccountStepValues = z.infer<typeof accountStepSchema>;

// L'API ne stocke qu'un champ adresse de 250 caractères : la contrainte porte
// donc sur la concaténation, vérifiée à la soumission (voir PropertyStepScreen).
export const propertyStepSchema = z.object({
  name: z.string().trim().min(1, 'Le nom est requis').max(100, '100 caractères maximum'),
  address: z.string().trim().min(1, "L'adresse est requise").max(150, '150 caractères maximum'),
  addressComplement: z.string().trim().max(100, '100 caractères maximum').optional().or(z.literal('')),
  postalCode: z.string().trim().max(20, '20 caractères maximum').optional().or(z.literal('')),
  city: z.string().trim().min(1, 'La ville est requise').max(100, '100 caractères maximum'),
});

export type PropertyStepValues = z.infer<typeof propertyStepSchema>;

export const PROPERTY_ADDRESS_MAX_LENGTH = 250;
