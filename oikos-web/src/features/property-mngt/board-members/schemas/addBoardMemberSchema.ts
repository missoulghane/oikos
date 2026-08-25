import { z } from 'zod';
import { phoneSchema } from '@/features/identity/register/schemas/phoneSchema';
import { BOARD_ROLES } from '@/features/property-mngt/board-members/types/boardMember.types';

export const addBoardMemberSchema = z.object({
  fullName: z.string().trim().min(1, 'Le nom complet est requis').max(200, '200 caractères maximum'),
  email: z.union([z.literal(''), z.string().trim().max(150, '150 caractères maximum').email('Email invalide')]),
  // Au format international quand il est là, comme pour un copropriétaire : c'est
  // sur cette valeur que le serveur reconnaît un contact déjà enregistré
  // (uk_party_property_phone). Un numéro saisi en format national ne serait
  // rapproché de rien, et créerait une seconde fiche pour la même personne.
  phone: z.union([z.literal(''), phoneSchema]).optional(),
  boardRole: z.enum(BOARD_ROLES),
});

export type AddBoardMemberFormValues = z.infer<typeof addBoardMemberSchema>;
