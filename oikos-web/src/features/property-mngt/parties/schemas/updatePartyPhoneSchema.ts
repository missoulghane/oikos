import { z } from 'zod';

// Mirrors the bean validation constraints on UpdatePartyPhoneRequest (oikos-api).
export const updatePartyPhoneSchema = z.object({
  phone: z.string().trim().max(20, '20 caractères maximum').optional(),
});

export type UpdatePartyPhoneFormValues = z.infer<typeof updatePartyPhoneSchema>;
