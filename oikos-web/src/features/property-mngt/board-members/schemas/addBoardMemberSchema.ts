import { z } from 'zod';
import { BOARD_ROLES } from '@/features/property-mngt/board-members/types/boardMember.types';

export const addBoardMemberSchema = z.object({
  fullName: z.string().trim().min(1, 'Le nom est requis').max(150, '150 caractères maximum'),
  email: z.string().trim().max(150, '150 caractères maximum').email('Email invalide').optional().or(z.literal('')),
  phone: z.string().trim().max(30, '30 caractères maximum').optional().or(z.literal('')),
  boardRole: z.enum(BOARD_ROLES),
});

export type AddBoardMemberFormValues = z.infer<typeof addBoardMemberSchema>;
