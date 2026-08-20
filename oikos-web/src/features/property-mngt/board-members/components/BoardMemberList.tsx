import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { BoardMemberRow } from '@/features/property-mngt/board-members/components/BoardMemberRow';
import type { BoardMember } from '@/features/property-mngt/board-members/types/boardMember.types';

interface BoardMemberListProps {
  propertyId: string;
  members: BoardMember[];
  /** Les adresses qui ont une invitation active en attente, en minuscules. */
  invitedEmails: Set<string>;
  onInvite: (member: BoardMember) => void;
}

export function BoardMemberList({ propertyId, members, invitedEmails, onInvite }: BoardMemberListProps) {
  if (members.length === 0) {
    return (
      <EmptyState title="Aucun membre du conseil syndical pour le moment">
        Ajoutez un membre ou envoyez une invitation pour constituer le conseil syndical.
      </EmptyState>
    );
  }

  return (
    <ul className="flex flex-col divide-y divide-gray-200 dark:divide-gray-800 rounded-lg border border-gray-200 dark:border-gray-800">
      {members.map((member) => (
        <BoardMemberRow
          key={member.id}
          propertyId={propertyId}
          member={member}
          hasPendingInvitation={member.partyEmail !== null && invitedEmails.has(member.partyEmail.toLowerCase())}
          onInvite={onInvite}
        />
      ))}
    </ul>
  );
}
