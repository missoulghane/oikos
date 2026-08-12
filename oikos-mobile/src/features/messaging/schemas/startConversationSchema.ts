import { z } from 'zod';

// Mirrors StartConversationRequest's Bean Validation (oikos-api): subject
// max 200 chars (ConversationSubject VO), body max 4000 (MessageBody VO).
export const startConversationSchema = z.object({
  subject: z.string().trim().min(1, 'Le titre ne peut pas être vide').max(200, '200 caractères maximum'),
  body: z.string().trim().min(1, 'Le message ne peut pas être vide').max(4000, '4000 caractères maximum'),
});

export type StartConversationFormValues = z.infer<typeof startConversationSchema>;
