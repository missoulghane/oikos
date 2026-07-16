import { z } from 'zod';
import { registerUserSchema } from '@/features/register/schemas/registerUserSchema';

// Mirrors the bean validation constraints on RegisterPropertyManagerRequest (oikos-api).
export const registerPropertyManagerSchema = registerUserSchema.extend({
  propertyName: z.string().trim().min(1, 'Le nom est requis').max(100, '100 caractères maximum'),
  propertyAddress: z.string().trim().min(1, "L'adresse est requise").max(250, '250 caractères maximum'),
});

export type RegisterPropertyManagerFormValues = z.infer<typeof registerPropertyManagerSchema>;
