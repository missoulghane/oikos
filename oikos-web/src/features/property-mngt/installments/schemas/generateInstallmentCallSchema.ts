import { z } from 'zod';

// Mirrors the bean validation constraints on GenerateInstallmentCallRequest (oikos-api), plus the
// service-level rule that a call cannot fall due before the period it bills (DueDateBeforePeriodException).
export const generateInstallmentCallSchema = z
  .object({
    period: z
      .string()
      .regex(/^\d{4}-\d{2}$/, 'La période doit être un mois (yyyy-MM)'),
    dueDate: z.string().min(1, "La date d'échéance est requise"),
  })
  .refine((values) => !values.period || !values.dueDate || values.dueDate >= `${values.period}-01`, {
    message: "L'échéance ne peut pas précéder le début de la période appelée",
    path: ['dueDate'],
  });

export type GenerateInstallmentCallFormValues = z.infer<typeof generateInstallmentCallSchema>;
