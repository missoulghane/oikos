import { z } from 'zod';

// Mirrors the bean validation constraints on RegisterUserRequest (oikos-api).
export const registerUserSchema = z.object({
  lastName: z.string().trim().min(1, 'Le nom est requis').max(100, '100 caractères maximum'),
  firstName: z.string().trim().min(1, 'Le prénom est requis').max(100, '100 caractères maximum'),
  email: z
    .string()
    .trim()
    .min(1, "L'email est requis")
    .max(150, '150 caractères maximum')
    .email('Email invalide'),
  phone: z.string().trim().max(20, '20 caractères maximum').optional().or(z.literal('')),
  password: z.string().min(10, 'Le mot de passe doit contenir au moins 10 caractères'),
});

export type RegisterUserFormValues = z.infer<typeof registerUserSchema>;
