import { useEffect, useRef } from 'react';
import { usePendingInvitationStore } from '@/features/identity/invitations/state/pendingInvitationStore';
import { useAcceptInvitation } from '@/features/identity/invitations/hooks/useAcceptInvitation';
import { useSubmitMembershipRequest } from '@/features/identity/invitations/hooks/useSubmitMembershipRequest';

/**
 * Mounted once at the root of MainNavigator (see PendingInvitationConsumer),
 * so it survives whichever Main screen is currently showing. Finalizes an
 * invitation stashed by InvitationLandingScreen before the user went off to
 * log in or create an account - see pendingInvitationStore for why this
 * exists instead of a `returnTo` redirect.
 */
export function usePendingInvitationConsumer() {
  const pending = usePendingInvitationStore((state) => state.pending);
  const clear = usePendingInvitationStore((state) => state.clear);
  const acceptInvitation = useAcceptInvitation();
  const submitMembershipRequest = useSubmitMembershipRequest();
  const hasConsumed = useRef(false);

  useEffect(() => {
    if (!pending || hasConsumed.current) {
      return;
    }
    hasConsumed.current = true;
    // Un siège au conseil s'accepte directement (il n'attribue rien, un
    // administrateur valide ensuite) ; tout le reste dépose une demande
    // d'adhésion que le syndic valide - lien public comme lien privé.
    const mutation = pending.isBoardSeat ? acceptInvitation : submitMembershipRequest;
    mutation.mutate(
      { token: pending.token, unitId: pending.unitId },
      {
        onSettled: () => clear(),
      },
    );
    // Deliberately depends only on `pending`, not on the mutation objects
    // (TanStack Query recreates those every render) - mirrors the mutationRef
    // pattern in oikos-web's own InvitationLandingPage auto-confirm effect.
  }, [pending]);
}
