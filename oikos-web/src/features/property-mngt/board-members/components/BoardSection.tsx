import { useState } from 'react';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useBoardMembers } from '@/features/property-mngt/board-members/hooks/useBoardMembers';
import { useBoardInvitations } from '@/features/property-mngt/board-members/hooks/useBoardInvitations';
import { BoardMemberList } from '@/features/property-mngt/board-members/components/BoardMemberList';
import { AddBoardMemberForm } from '@/features/property-mngt/board-members/components/AddBoardMemberForm';
import { CreateBoardInvitationForm } from '@/features/property-mngt/board-members/components/CreateBoardInvitationForm';
import type { BoardMember } from '@/features/property-mngt/board-members/types/boardMember.types';

interface InviteFormState {
  targetEmail?: string;
  boardRole?: BoardMember['boardRole'];
}

/**
 * Le conseil syndical, en bloc sous les informations générales de la
 * copropriété. Il occupait un onglet à lui seul, ce qui obligeait à changer
 * d'écran pour lire deux choses qui se consultent ensemble : qui gère la
 * copropriété, et quelle copropriété.
 *
 * <p>Le bloc « Invitations en cours » qui l'accompagnait a disparu : l'onglet
 * Invitations liste déjà toutes celles de la copropriété. Ce qu'il apportait
 * vraiment - savoir qui attend une réponse - est remonté sur la ligne de chaque
 * membre, sous forme de statut.
 */
export function BoardSection({ propertyId }: { propertyId: string }) {
  const [isAdding, setIsAdding] = useState(false);
  const [inviteFormState, setInviteFormState] = useState<InviteFormState | null>(null);

  const boardMembers = useBoardMembers(propertyId);
  const boardInvitations = useBoardInvitations(propertyId);

  // Les invitations restent lues, mais ne s'affichent plus : elles ne servent
  // qu'à dire lesquels des membres attendent encore une réponse.
  const invitedEmails = new Set(
    (boardInvitations.data ?? [])
      .filter((invitation) => invitation.status === 'ACTIVE' && invitation.targetEmail)
      .map((invitation) => invitation.targetEmail!.toLowerCase()),
  );

  function inviteMember(member: BoardMember) {
    setIsAdding(false);
    setInviteFormState({ targetEmail: member.partyEmail ?? undefined, boardRole: member.boardRole });
  }

  return (
    <Card className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white/90">Conseil syndical</h2>
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
          propertyId={propertyId}
          onSuccess={() => setIsAdding(false)}
          onCancel={() => setIsAdding(false)}
        />
      )}
      {inviteFormState && (
        <CreateBoardInvitationForm
          propertyId={propertyId}
          defaultTargetEmail={inviteFormState.targetEmail}
          defaultBoardRole={inviteFormState.boardRole}
          onSuccess={() => setInviteFormState(null)}
          onCancel={() => setInviteFormState(null)}
        />
      )}

      {boardMembers.isLoading && <Loader label="Chargement du conseil syndical…" />}
      {boardMembers.isError && <Alert message={getErrorMessage(boardMembers.error)} />}
      {boardInvitations.isError && <Alert message={getErrorMessage(boardInvitations.error)} />}
      {boardMembers.data && (
        <BoardMemberList
          propertyId={propertyId}
          members={boardMembers.data}
          invitedEmails={invitedEmails}
          onInvite={inviteMember}
        />
      )}
    </Card>
  );
}
