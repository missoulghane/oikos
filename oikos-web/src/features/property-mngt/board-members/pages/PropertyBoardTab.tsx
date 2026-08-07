import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useBoardMembers } from '@/features/property-mngt/board-members/hooks/useBoardMembers';
import { useBoardInvitations } from '@/features/property-mngt/board-members/hooks/useBoardInvitations';
import { BoardMemberList } from '@/features/property-mngt/board-members/components/BoardMemberList';
import { BoardInvitationList } from '@/features/property-mngt/board-members/components/BoardInvitationList';
import { AddBoardMemberForm } from '@/features/property-mngt/board-members/components/AddBoardMemberForm';
import { CreateBoardInvitationForm } from '@/features/property-mngt/board-members/components/CreateBoardInvitationForm';
import type { BoardMember } from '@/features/property-mngt/board-members/types/boardMember.types';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

interface InviteFormState {
  targetEmail?: string;
  boardRole?: BoardMember['boardRole'];
}

export function PropertyBoardTab() {
  const { property } = useOutletContext<{ property: Property }>();
  const [isAdding, setIsAdding] = useState(false);
  const [inviteFormState, setInviteFormState] = useState<InviteFormState | null>(null);

  const boardMembers = useBoardMembers(property.id);
  const boardInvitations = useBoardInvitations(property.id);

  function inviteMember(member: BoardMember) {
    setIsAdding(false);
    setInviteFormState({ targetEmail: member.partyEmail ?? undefined, boardRole: member.boardRole });
  }

  return (
    <div className="flex flex-col gap-4">
      <Card className="flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h2 className="text-base font-semibold text-gray-900">Bureau de syndic</h2>
          {!isAdding && !inviteFormState && (
            <div className="flex gap-2">
              <Button type="button" variant="secondary" onClick={() => setInviteFormState({})}>
                Envoyer une invitation
              </Button>
              <Button type="button" onClick={() => setIsAdding(true)}>
                Ajouter un membre
              </Button>
            </div>
          )}
        </div>

        {isAdding && (
          <AddBoardMemberForm
            propertyId={property.id}
            onSuccess={() => setIsAdding(false)}
            onCancel={() => setIsAdding(false)}
          />
        )}
        {inviteFormState && (
          <CreateBoardInvitationForm
            propertyId={property.id}
            defaultTargetEmail={inviteFormState.targetEmail}
            defaultBoardRole={inviteFormState.boardRole}
            onSuccess={() => setInviteFormState(null)}
            onCancel={() => setInviteFormState(null)}
          />
        )}

        {boardMembers.isLoading && <Loader label="Chargement du bureau de syndic…" />}
        {boardMembers.isError && <Alert message={getErrorMessage(boardMembers.error)} />}
        {boardMembers.data && (
          <BoardMemberList propertyId={property.id} members={boardMembers.data} onInvite={inviteMember} />
        )}
      </Card>

      <Card className="flex flex-col gap-4">
        <h2 className="text-base font-semibold text-gray-900">Invitations en cours</h2>
        {boardInvitations.isLoading && <Loader label="Chargement des invitations…" />}
        {boardInvitations.isError && <Alert message={getErrorMessage(boardInvitations.error)} />}
        {boardInvitations.data && <BoardInvitationList propertyId={property.id} invitations={boardInvitations.data} />}
      </Card>
    </div>
  );
}
