import {
  useCurrentUser,
  boardPropertyIds,
  hasCopro,
  isManagerTier,
  isBoardTierOnProperty,
  isManagerTierOnProperty,
  type CurrentUser,
} from '@/features/identity/me';
import { useActiveSpace } from '@/shared/hooks/useActiveSpace';

export type EffectiveSpace =
  | { kind: 'owner' }
  | { kind: 'board'; propertyId: string }
  | { kind: 'manager' }
  | { kind: 'unresolved' };

/**
 * Espace par défaut quand l'URL n'en impose aucun (landing "/dashboard" sans
 * ?space=, ou navigation vers une propriété que le compte ne gère pas) :
 * copropriétaire dès qu'un lot existe - un mandat s'assume, il ne s'impose
 * pas -, sinon le premier mandat de bureau, dans un ordre stable, sinon
 * l'espace gérant professionnel, sinon copropriétaire par défaut (compte
 * tout neuf sans rien).
 */
function defaultSpace(user: CurrentUser): EffectiveSpace {
  if (hasCopro(user)) {
    return { kind: 'owner' };
  }
  const mandateIds = boardPropertyIds(user);
  if (mandateIds.length > 0) {
    return { kind: 'board', propertyId: mandateIds[0] };
  }
  if (isManagerTier(user)) {
    return { kind: 'manager' };
  }
  return { kind: 'owner' };
}

/**
 * The single source of truth for "which space is actually showing right
 * now", combining the URL (useActiveSpace, source of truth when it says
 * something) with the account's default when it doesn't. Every screen that
 * needs to know the current space - the dashboard, the sidebar's contextual
 * menu, the space switcher's highlighted entry - must read this, never
 * re-derive its own fallback: two independent fallbacks is exactly how the
 * sidebar and the dashboard drifted apart (dashboard defaulted to owner,
 * sidebar defaulted to the first mandate) on a plain, param-less landing.
 */
export function useEffectiveSpace(): EffectiveSpace {
  const currentUser = useCurrentUser();
  const activeSpace = useActiveSpace();
  const user = currentUser.data;

  if (!user) {
    return { kind: 'unresolved' };
  }
  if (activeSpace.kind === 'owner') {
    return { kind: 'owner' };
  }
  if (activeSpace.kind === 'board' && activeSpace.propertyId) {
    const staffed =
      isBoardTierOnProperty(user, activeSpace.propertyId) || isManagerTierOnProperty(user, activeSpace.propertyId);
    if (staffed) {
      return { kind: 'board', propertyId: activeSpace.propertyId };
    }
    // URL names a property this account has no staff role on - never guess
    // it, fall through to the same default as a neutral route.
  }
  return defaultSpace(user);
}

/**
 * The "?space=…" query string that carries the given (resolved) space along
 * a link into a transverse route (Messagerie, Notifications, Profil…) -
 * those routes have no property id in their own path, so without this any
 * link built as a bare "/messages/…" is read back as neutral and
 * useEffectiveSpace's landing default (owner-first) silently swaps the
 * viewer into the owner space instead of keeping them where they were. Every
 * "Nouveau message"-style link must build its href through this, not a
 * literal string - see AppSidebar's messagingTabs, MessagingLayout,
 * DraftsListPage, DraftListItem.
 */
export function spaceQuerySuffix(space: EffectiveSpace): string {
  if (space.kind === 'owner') {
    return '?space=owner';
  }
  if (space.kind === 'board') {
    return `?space=board&propertyId=${space.propertyId}`;
  }
  return '';
}
