import { z } from 'zod';

// Mirrors the bean validation constraints on ActivateAccountRequest (oikos-api).
export const activateAccountSchema = z.object({
  newPassword: z.string().min(10, 'Le mot de passe doit contenir au moins 10 caractères'),
});

export type ActivateAccountFormValues = z.infer<typeof activateAccountSchema>;
