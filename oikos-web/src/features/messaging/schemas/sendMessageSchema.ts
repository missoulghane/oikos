import { z } from 'zod';

// Mirrors the MessageBody value object / @Size(max = 4000) constraint (oikos-api).
export const sendMessageSchema = z.object({
  body: z.string().trim().min(1, 'Le message ne peut pas être vide').max(4000, '4000 caractères maximum'),
});

export type SendMessageFormValues = z.infer<typeof sendMessageSchema>;
