import { z } from 'zod';

// Mirrors the bean validation constraints on UpdatePropertyRequest (oikos-api).
export const updatePropertySchema = z.object({
  name: z.string().trim().min(1, 'Le nom est requis').max(100, '100 caractères maximum'),
  address: z.string().trim().min(1, "L'adresse est requise").max(250, '250 caractères maximum'),
});

export type UpdatePropertyFormValues = z.infer<typeof updatePropertySchema>;
