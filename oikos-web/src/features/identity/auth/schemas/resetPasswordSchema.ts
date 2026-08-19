import { z } from 'zod';
import { refinePasswordsMatch } from '@/features/identity/register/schemas/passwordConfirmation';

// Mirrors the bean validation constraints on ResetPasswordRequest (oikos-api),
// avec une confirmation en plus, qui n'existe pas côté API : une faute de frappe
// ici enfermerait dehors celui qui vient justement de s'y retrouver, et le champ
// est masqué - il n'a aucun moyen de la voir. Les champs s'appellent password /
// confirmPassword pour réutiliser refinePasswordsMatch tel quel ; la page les
// remet sous le nom attendu par l'API (newPassword) à l'envoi.
export const resetPasswordSchema = refinePasswordsMatch(
  z.object({
    password: z.string().min(10, 'Le mot de passe doit contenir au moins 10 caractères'),
    confirmPassword: z.string().min(1, 'Merci de confirmer le mot de passe'),
  }),
);

export type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;
