import { z } from 'zod';

// Mirrors the bean validation constraints on CreatePropertyRequest (oikos-api).
export const createPropertySchema = z.object({
  name: z.string().trim().min(1, 'Le nom est requis').max(100, '100 caractères maximum'),
  address: z.string().trim().min(1, "L'adresse est requise").max(250, '250 caractères maximum'),
  // Facultative, comme côté API : les copropriétés d'avant le champ n'en ont pas,
  // et rien ne justifie de bloquer la modification du nom pour l'exiger.
  city: z.string().trim().max(100, '100 caractères maximum'),
});

export type CreatePropertyFormValues = z.infer<typeof createPropertySchema>;
