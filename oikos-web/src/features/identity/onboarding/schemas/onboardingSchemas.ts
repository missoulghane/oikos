import { z } from 'zod';
import { refinePasswordsMatch } from '@/features/identity/register/schemas/passwordConfirmation';
import { phoneSchema } from '@/features/identity/register/schemas/phoneSchema';

/**
 * Unique règle appliquée par l'API (RawPassword.MIN_LENGTH) : l'étape 1 se
 * contente de la signaler quand elle n'est pas tenue. Y ajouter une contrainte
 * ici sans la poser côté serveur créerait une divergence, et l'y poser côté
 * serveur invaliderait les mots de passe déjà en base.
 */
export const PASSWORD_MIN_LENGTH = 10;

export const accountStepSchema = refinePasswordsMatch(
  z.object({
    fullName: z.string().trim().min(1, 'Le nom complet est requis').max(200, '200 caractères maximum'),
    email: z.string().trim().min(1, "L'email est requis").email('Email invalide').max(150, '150 caractères maximum'),
    phone: phoneSchema,
    password: z.string().min(PASSWORD_MIN_LENGTH, `Au moins ${PASSWORD_MIN_LENGTH} caractères`),
    confirmPassword: z.string().min(1, 'La confirmation est requise'),
  }),
);

export type AccountStepValues = z.infer<typeof accountStepSchema>;

// Adresse et ville partent maintenant dans deux champs distincts, chacun avec
// sa propre limite côté API (250 et 100) : plus rien à vérifier sur une
// concaténation, et l'adresse récupère la marge que la ville lui prenait.
export const propertyStepSchema = z.object({
  name: z.string().trim().min(1, 'Le nom est requis').max(100, '100 caractères maximum'),
  address: z.string().trim().min(1, "L'adresse est requise").max(250, '250 caractères maximum'),
  city: z.string().trim().min(1, 'La ville est requise').max(100, '100 caractères maximum'),
});

export type PropertyStepValues = z.infer<typeof propertyStepSchema>;
