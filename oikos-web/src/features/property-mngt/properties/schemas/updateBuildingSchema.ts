import { z } from 'zod';

// Mirrors the bean validation constraints on UpdateBuildingRequest (oikos-api).
// Le nom est requis ici alors qu'il est facultatif dans le wizard : à
// l'inscription, un bâtiment sans nom prend « Bâtiment 1 » par défaut, mais
// l'effacer depuis la fiche laisserait la liste des lots sans en-tête lisible.
export const updateBuildingSchema = z.object({
  name: z.string().trim().min(1, "Le nom de l'immeuble est requis").max(100, '100 caractères maximum'),
  floorCount: z
    .number({ error: 'Le nombre d’étages est requis' })
    .int('Doit être un nombre entier')
    .min(0, 'Ne peut pas être négatif'),
});

export type UpdateBuildingFormValues = z.infer<typeof updateBuildingSchema>;
