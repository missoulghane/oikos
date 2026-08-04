import { z } from 'zod';

export const invitationSignupSchema = z.object({
  fullName: z.string().trim().min(1, 'Le nom complet est requis').max(200, '200 caractères maximum'),
  email: z.string().trim().min(1, "L'email est requis").max(150, '150 caractères maximum').email('Email invalide'),
  password: z.string().min(10, 'Le mot de passe doit contenir au moins 10 caractères'),
});

export type InvitationSignupFormValues = z.infer<typeof invitationSignupSchema>;
