import { z } from 'zod';

// Mirrors AcceptInvitationRequest (oikos-api): password is required only when
// the invited email has no existing account yet, so it stays optional here -
// left blank simply links the invitation to the existing account.
export const acceptInvitationSchema = z.object({
  password: z
    .string()
    .refine((value) => value === '' || value.length >= 10, 'Le mot de passe doit contenir au moins 10 caractères'),
});

export type AcceptInvitationFormValues = z.infer<typeof acceptInvitationSchema>;
