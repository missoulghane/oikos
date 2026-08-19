import { z } from 'zod';
import { refinePasswordsMatch } from '@/features/identity/register/schemas/passwordConfirmation';

// Mirrors the bean validation constraints on ResetPasswordRequest (oikos-api),
// avec une confirmation en plus, qui n'existe pas côté API : une faute de frappe
// dans un champ masqué enfermerait dehors celui qui vient justement de s'y
// retrouver. Champs nommés password / confirmPassword pour réutiliser
// refinePasswordsMatch ; l'écran les remet sous le nom attendu par l'API
// (newPassword) à l'envoi. Identique à oikos-web's resetPasswordSchema.ts.
export const resetPasswordSchema = refinePasswordsMatch(
  z.object({
    password: z.string().min(10, 'Le mot de passe doit contenir au moins 10 caractères'),
    confirmPassword: z.string().min(1, 'Merci de confirmer le mot de passe'),
  }),
);

export type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;
