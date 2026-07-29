import { z } from 'zod';

// Mirrors the bean validation constraints on AssignPropertyManagerRequest (oikos-api).
export const addPropertyManagerSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, "L'email est requis")
    .max(150, '150 caractères maximum')
    .email('Email invalide'),
});

export type AddPropertyManagerFormValues = z.infer<typeof addPropertyManagerSchema>;
