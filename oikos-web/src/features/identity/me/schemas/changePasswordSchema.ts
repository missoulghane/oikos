import { z } from 'zod';

// Mirrors the bean validation constraints on ChangePasswordRequest / RawPassword.MIN_LENGTH (oikos-api).
export const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Le mot de passe actuel est requis'),
    newPassword: z.string().min(10, 'Le mot de passe doit contenir au moins 10 caractères'),
    confirmPassword: z.string().min(1, 'Merci de confirmer le mot de passe'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'Les mots de passe ne correspondent pas',
    path: ['confirmPassword'],
  });

export type ChangePasswordFormValues = z.infer<typeof changePasswordSchema>;
