import { z } from 'zod';

/**
 * Applied at the leaf schema (after any .extend()), never on the shared base
 * object itself: .refine() returns a ZodEffects-like wrapper that no longer
 * exposes .extend(), so registerPropertyAdminSchema couldn't build on top of
 * registerUserSchema if the base already carried this refinement.
 */
export function refinePasswordsMatch<T extends z.ZodType<{ password: string; confirmPassword: string }>>(schema: T) {
  return schema.refine((data) => data.password === data.confirmPassword, {
    message: 'Les mots de passe ne correspondent pas',
    path: ['confirmPassword'],
  });
}
