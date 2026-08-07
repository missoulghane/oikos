import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { BoardMemberRow } from '@/features/property-mngt/board-members/components/BoardMemberRow';
import type { BoardMember } from '@/features/property-mngt/board-members/types/boardMember.types';

interface BoardMemberListProps {
  propertyId: string;
  members: BoardMember[];
  onInvite: (member: BoardMember) => void;
}

export function BoardMemberList({ propertyId, members, onInvite }: BoardMemberListProps) {
  if (members.length === 0) {
    return (
      <EmptyState title="Aucun membre du bureau pour le moment">
        Ajoutez un membre ou envoyez une invitation pour constituer le bureau de syndic.
      </EmptyState>
    );
  }

  return (
    <ul className="flex flex-col divide-y divide-gray-200 rounded-lg border border-gray-200">
      {members.map((member) => (
        <BoardMemberRow key={member.id} propertyId={propertyId} member={member} onInvite={onInvite} />
      ))}
    </ul>
  );
}
