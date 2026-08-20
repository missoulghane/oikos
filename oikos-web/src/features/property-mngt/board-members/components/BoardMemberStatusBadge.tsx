import { Badge } from '@/shared/components/Badge/Badge';
import type { BoardMember } from '@/features/property-mngt/board-members/types/boardMember.types';

export type BoardMemberStatusKind = 'TO_VALIDATE' | 'ACCOUNT_ACTIVE' | 'INVITED' | 'NOT_INVITED';

const STATUS: Record<BoardMemberStatusKind, { label: string; color: 'warning' | 'success' | 'info' | 'light' }> = {
  TO_VALIDATE: { label: 'À valider', color: 'warning' },
  ACCOUNT_ACTIVE: { label: 'Compte actif', color: 'success' },
  INVITED: { label: 'Invitation envoyée', color: 'info' },
  NOT_INVITED: { label: 'Sans invitation', color: 'light' },
};

/**
 * Un seul état par membre, là où la ligne en empilait deux de natures
 * différentes - le siège au conseil (à valider) et la liaison de compte - et où
 * le statut d'invitation en aurait fait un troisième. Trois badges sur une ligne
 * ne se lisent plus.
 *
 * <p>L'ordre de priorité n'est pas cosmétique : la validation du siège passe
 * devant tout le reste, c'est la seule qui bloque un droit. Vient ensuite ce que
 * le membre a déjà (un compte), puis ce qui est en cours (une invitation), puis
 * ce qui reste à faire.
 */
export function statusOf(member: BoardMember, hasPendingInvitation: boolean): BoardMemberStatusKind {
  if (member.status === 'PENDING_VALIDATION') {
    return 'TO_VALIDATE';
  }
  if (member.hasLinkedAccount) {
    return 'ACCOUNT_ACTIVE';
  }
  return hasPendingInvitation ? 'INVITED' : 'NOT_INVITED';
}

export function BoardMemberStatusBadge({ kind }: { kind: BoardMemberStatusKind }) {
  return <Badge color={STATUS[kind].color}>{STATUS[kind].label}</Badge>;
}
