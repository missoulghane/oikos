import { z } from 'zod';

// Mirrors the bean validation constraints on CreatePropertyRequest (oikos-api).
export const createPropertySchema = z.object({
  name: z.string().trim().min(1, 'Le nom est requis').max(100, '100 caractères maximum'),
  address: z.string().trim().min(1, "L'adresse est requise").max(250, '250 caractères maximum'),
  firstBuildingName: z
    .string()
    .trim()
    .min(1, 'Le nom du premier immeuble est requis')
    .max(100, '100 caractères maximum'),
  firstBuildingFloorCount: z
    .number({ error: 'Le nombre d’étages est requis' })
    .int('Doit être un nombre entier')
    .min(0, 'Ne peut pas être négatif'),
});

export type CreatePropertyFormValues = z.infer<typeof createPropertySchema>;
