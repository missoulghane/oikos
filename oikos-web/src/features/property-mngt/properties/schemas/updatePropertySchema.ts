import { z } from 'zod';

// Mirrors the bean validation constraints on UpdatePropertyRequest (oikos-api).
export const updatePropertySchema = z.object({
  name: z.string().trim().min(1, 'Le nom est requis').max(100, '100 caractères maximum'),
  address: z.string().trim().min(1, "L'adresse est requise").max(250, '250 caractères maximum'),
  // Obligatoire à la modification, comme côté API (UpdatePropertyRequest) : une
  // copropriété créée sans ville la reçoit à sa première modification.
  city: z.string().trim().min(1, 'La ville est requise').max(100, '100 caractères maximum'),
});

export type UpdatePropertyFormValues = z.infer<typeof updatePropertySchema>;
