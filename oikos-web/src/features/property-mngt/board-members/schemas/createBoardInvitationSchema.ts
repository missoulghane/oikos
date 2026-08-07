import { z } from 'zod';
import { BOARD_ROLES } from '@/features/property-mngt/board-members/types/boardMember.types';

export const createBoardInvitationSchema = z.object({
  targetEmail: z.string().trim().min(1, "L'email est requis").max(150, '150 caractères maximum').email('Email invalide'),
  boardRole: z.enum(BOARD_ROLES),
});

export type CreateBoardInvitationFormValues = z.infer<typeof createBoardInvitationSchema>;
