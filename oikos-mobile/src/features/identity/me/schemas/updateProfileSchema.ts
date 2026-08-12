import { z } from 'zod';

// Mirrors the bean validation constraints on UpdateProfileRequest (oikos-api).
export const updateProfileSchema = z.object({
  fullName: z.string().trim().min(1, 'Le nom complet est requis').max(200, '200 caractères maximum'),
  email: z
    .string()
    .trim()
    .min(1, "L'email est requis")
    .max(150, '150 caractères maximum')
    .email('Email invalide'),
  phone: z.string().trim().max(20, '20 caractères maximum').optional(),
});

export type UpdateProfileFormValues = z.infer<typeof updateProfileSchema>;
