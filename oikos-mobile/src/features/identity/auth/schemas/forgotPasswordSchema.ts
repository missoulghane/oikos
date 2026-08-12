import { z } from 'zod';

// Mirrors the bean validation constraints on ForgotPasswordRequest (oikos-api).
export const forgotPasswordSchema = z.object({
  email: z.string().trim().min(1, "L'email est requis").email('Email invalide'),
});

export type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>;
