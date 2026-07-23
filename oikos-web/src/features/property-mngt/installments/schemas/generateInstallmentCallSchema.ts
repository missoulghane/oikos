import { z } from 'zod';

// Mirrors the bean validation constraints on GenerateInstallmentCallRequest (oikos-api).
export const generateInstallmentCallSchema = z.object({
  period: z
    .string()
    .regex(/^\d{4}-\d{2}$/, 'La période doit être un mois (yyyy-MM)'),
  dueDate: z.string().min(1, "La date d'échéance est requise"),
});

export type GenerateInstallmentCallFormValues = z.infer<typeof generateInstallmentCallSchema>;
