import { Badge } from '@/shared/components/Badge/Badge';
import { Button } from '@/shared/components/Button/Button';
import { CheckCircleIcon } from '@/shared/icons';
import { useRemoveBoardMember } from '@/features/property-mngt/board-members/hooks/useRemoveBoardMember';
import { useValidateBoardMember } from '@/features/property-mngt/board-members/hooks/useValidateBoardMember';
import { BOARD_ROLE_LABELS, type BoardMember } from '@/features/property-mngt/board-members/types/boardMember.types';

interface BoardMemberRowProps {
  propertyId: string;
  member: BoardMember;
  onInvite: (member: BoardMember) => void;
}

export function BoardMemberRow({ propertyId, member, onInvite }: BoardMemberRowProps) {
  const removeBoardMember = useRemoveBoardMember(propertyId);
  const validateBoardMember = useValidateBoardMember(propertyId);
  const isPending = member.status === 'PENDING_VALIDATION';

  return (
    <li className="flex flex-col gap-2 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
      <div className="min-w-0">
        <p className="flex items-center gap-2 text-sm font-medium text-gray-900 dark:text-white/90">
          {member.partyFullName}
          <Badge color="primary">{BOARD_ROLE_LABELS[member.boardRole]}</Badge>
          {isPending && <Badge color="warning">En attente de validation</Badge>}
        </p>
        {member.partyEmail && <p className="truncate text-sm text-gray-500 dark:text-gray-400">{member.partyEmail}</p>}
        {member.hasLinkedAccount && (
          <span className="flex shrink-0 items-center gap-1 text-xs font-medium text-success-600 dark:text-success-500">
            <CheckCircleIcon className="h-4 w-4" />
            <span>Compte actif</span>
          </span>
        )}
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
        {!member.hasLinkedAccount && member.partyEmail && (
          <Button type="button" variant="secondary" onClick={() => onInvite(member)}>
            Inviter
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
