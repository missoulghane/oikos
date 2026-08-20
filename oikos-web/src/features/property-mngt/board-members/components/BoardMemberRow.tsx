import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { useRemoveBoardMember } from '@/features/property-mngt/board-members/hooks/useRemoveBoardMember';
import { useValidateBoardMember } from '@/features/property-mngt/board-members/hooks/useValidateBoardMember';
import {
  BoardMemberStatusBadge,
  statusOf,
} from '@/features/property-mngt/board-members/components/BoardMemberStatusBadge';
import { BOARD_ROLE_LABELS, type BoardMember } from '@/features/property-mngt/board-members/types/boardMember.types';

interface BoardMemberRowProps {
  propertyId: string;
  member: BoardMember;
  /** Une invitation active attend déjà cette personne : le bloc « Invitations en cours » a disparu, l'état vit ici. */
  hasPendingInvitation: boolean;
  onInvite: (member: BoardMember) => void;
}

export function BoardMemberRow({ propertyId, member, hasPendingInvitation, onInvite }: BoardMemberRowProps) {
  const removeBoardMember = useRemoveBoardMember(propertyId);
  const validateBoardMember = useValidateBoardMember(propertyId);
  const status = statusOf(member, hasPendingInvitation);
  const isPending = status === 'TO_VALIDATE';

  return (
    <li className="flex flex-col gap-2 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div className="min-w-0">
        <p className="flex items-center gap-2 text-sm font-medium text-gray-900 dark:text-white/90">
          {member.partyFullName}
          <Badge color="primary">{BOARD_ROLE_LABELS[member.boardRole]}</Badge>
          <BoardMemberStatusBadge kind={status} />
        </p>
        {/* Un membre peut n'avoir qu'un téléphone : la ligne ne montre alors
            aucune coordonnée plutôt qu'une adresse vide. */}
        {member.partyEmail && <p className="truncate text-sm text-gray-500 dark:text-gray-400">{member.partyEmail}</p>}
      </div>
      <div className="flex shrink-0 flex-wrap gap-2">
        {isPending && (
          <Button
            type="button"
            isLoading={validateBoardMember.isPending}
            onClick={() => validateBoardMember.mutate(member.id)}
          >
            Valider
          </Button>
        )}
        {/* L'action découle du statut : relancer une invitation en cours,
            l'envoyer sinon, et rien du tout sans adresse où l'envoyer. */}
        {status !== 'ACCOUNT_ACTIVE' && member.partyEmail && (
          <Button type="button" variant="secondary" onClick={() => onInvite(member)}>
            {status === 'INVITED' ? 'Relancer' : 'Inviter'}
          </Button>
        )}
        <Button
          type="button"
          variant="secondary"
          isLoading={removeBoardMember.isPending}
          onClick={() => removeBoardMember.mutate(member.id)}
        >
          Retirer
        </Button>
      </div>
    </li>
  );
}
