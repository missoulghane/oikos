import { create } from 'zustand';

interface PendingInvitation {
  token: string;
  /** Nul pour un siège au conseil syndical, qui ne porte sur aucun lot. */
  unitId: string | null;
  /**
   * Ce qui décide de la route de finalisation, et non plus le type public /
   * privé du lien : les deux invitations de copropriétaire déposent la même
   * demande d'adhésion, seul un siège au conseil s'accepte encore directement.
   */
  isBoardSeat: boolean;
}

interface PendingInvitationState {
  pending: PendingInvitation | null;
  setPending: (pending: PendingInvitation) => void;
  clear: () => void;
}

/**
 * oikos-web survives the "go log in / register, then come back" trip via a
 * `returnTo` URL param that bounces the browser back to InvitationLandingPage.
 * React Navigation has no equivalent: logging in flips isAuthenticated, which
 * makes RootNavigator swap AuthNavigator (and InvitationLandingScreen's own
 * state) out for MainNavigator entirely - there's no "page" to return to.
 * This in-memory (not persisted) store is the mobile substitute: the landing
 * screen stashes the invitation here before navigating to Login/RegisterUser,
 * and MainNavigator's pending-invitation consumer (mounted for the Main
 * stack's whole lifetime) picks it up once authenticated and clears it.
 */
export const usePendingInvitationStore = create<PendingInvitationState>((set) => ({
  pending: null,
  setPending: (pending) => set({ pending }),
  clear: () => set({ pending: null }),
}));
