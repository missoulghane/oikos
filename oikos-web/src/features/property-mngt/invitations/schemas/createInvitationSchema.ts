import { z } from 'zod';
import { INVITATION_TYPES } from '@/features/property-mngt/invitations/types/invitation.types';

export const createInvitationSchema = z
  .object({
    type: z.enum(INVITATION_TYPES),
    unitId: z.string().optional(),
    targetEmail: z.string().trim().optional(),
  })
  .superRefine((data, ctx) => {
    if (data.type === 'PRIVATE_WITH_UNIT' && !data.unitId) {
      ctx.addIssue({ code: 'custom', path: ['unitId'], message: 'Sélectionnez un lot' });
    }
    if (data.type !== 'PUBLIC') {
      if (!data.targetEmail) {
        ctx.addIssue({ code: 'custom', path: ['targetEmail'], message: "L'email du destinataire est requis" });
      } else if (!z.string().email().safeParse(data.targetEmail).success) {
        ctx.addIssue({ code: 'custom', path: ['targetEmail'], message: 'Email invalide' });
      }
    }
  });

export type CreateInvitationFormValues = z.infer<typeof createInvitationSchema>;
