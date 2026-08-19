import { z } from 'zod';
import { refinePasswordsMatch } from '@/features/identity/register/schemas/passwordConfirmation';
import { phoneSchema } from '@/features/identity/register/schemas/phoneSchema';

// Mirrors the bean validation constraints on RegisterUserRequest (oikos-api). Le
// téléphone est exigé ici alors qu'il reste facultatif côté API : les comptes déjà
// créés n'en ont pas, et le rendre obligatoire au serveur les rendrait invalides.
// Kept as a plain (unrefined) object so registerPropertyAdminSchema can .extend()
// it - the password-match check is applied separately, see refinePasswordsMatch.
export const registerUserObjectSchema = z.object({
  fullName: z.string().trim().min(1, 'Le nom complet est requis').max(200, '200 caractères maximum'),
  email: z
    .string()
    .trim()
    .min(1, "L'email est requis")
    .max(150, '150 caractères maximum')
    .email('Email invalide'),
  phone: phoneSchema,
  password: z.string().min(10, 'Le mot de passe doit contenir au moins 10 caractères'),
  confirmPassword: z.string().min(1, 'Merci de confirmer le mot de passe'),
});

export const registerUserSchema = refinePasswordsMatch(registerUserObjectSchema);

export type RegisterUserFormValues = z.infer<typeof registerUserSchema>;
