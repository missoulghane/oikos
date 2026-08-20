import { z } from 'zod';
import { registerUserObjectSchema } from '@/features/identity/register/schemas/registerUserSchema';
import { refinePasswordsMatch } from '@/features/identity/register/schemas/passwordConfirmation';

// Mirrors the bean validation constraints shared by RegisterPropertyBoardAdminRequest
// and RegisterPropertyManagerAdminRequest (oikos-api) - same fields, only the target
// endpoint (and the role granted server-side) differs between the two registration flows.
// Le téléphone vient du schéma de base (registerUserObjectSchema) : il alimente
// ici la Party créée pour l'admin en plus du compte lui-même.
export const registerPropertyAdminSchema = refinePasswordsMatch(
  registerUserObjectSchema.extend({
    propertyName: z.string().trim().min(1, 'Le nom est requis').max(100, '100 caractères maximum'),
    propertyAddress: z.string().trim().min(1, "L'adresse est requise").max(250, '250 caractères maximum'),
    // Exigée ici alors que l'API l'accepte vide : ce formulaire est le pendant
    // court du wizard, qui la demande - une copropriété créée par l'un ne doit
    // pas arriver moins renseignée que par l'autre.
    propertyCity: z.string().trim().min(1, 'La ville est requise').max(100, '100 caractères maximum'),
  }),
);

export type RegisterPropertyAdminFormValues = z.infer<typeof registerPropertyAdminSchema>;
