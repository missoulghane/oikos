import { z } from 'zod';

// Mirrors the bean validation constraints on AddBuildingRequest (oikos-api).
export const addBuildingSchema = z.object({
  name: z.string().trim().min(1, "Le nom de l'immeuble est requis").max(100, '100 caractères maximum'),
  floorCount: z
    .number({ error: 'Le nombre d’étages est requis' })
    .int('Doit être un nombre entier')
    .min(0, 'Ne peut pas être négatif'),
});

export type AddBuildingFormValues = z.infer<typeof addBuildingSchema>;
