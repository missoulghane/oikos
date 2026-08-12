import { z } from 'zod';

// Mirrors the bean validation constraints on ResetPasswordRequest (oikos-api).
export const resetPasswordSchema = z.object({
  newPassword: z.string().min(10, 'Le mot de passe doit contenir au moins 10 caractères'),
});

export type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;
