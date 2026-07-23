import { z } from 'zod';

// Mirrors the bean validation constraint on AddUnitTypeDefinitionRequest (oikos-api).
export const unitTypeNameSchema = z.object({
  name: z.string().trim().min(1, 'Le nom est requis').max(50, '50 caractères maximum'),
});

export type UnitTypeNameFormValues = z.infer<typeof unitTypeNameSchema>;
