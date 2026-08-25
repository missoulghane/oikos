import type { CurrentUser } from '@/features/identity/me/types/me.types';

export function isAdmin(user: CurrentUser): boolean {
  return user.roles.includes('ROLE_ADMIN') || user.roles.includes('ROLE_MASTER');
}

/**
 * Any STAFF role at all on at least one property (board or manager-firm, admin or member
 * tier) - a bare PROPERTY_OWNER grant must not satisfy this (mirrors the backend's
 * UserAccessView.managesProperty - see oikos-api). Gates the whole /property-mngt route
 * subtree; getting this wrong lets a plain unit owner into the gérant/syndic UI shell.
 */
export function canManageProperties(user: CurrentUser): boolean {
  return (
    isAdmin(user) ||
    Object.values(user.roleByProperty).some((roles) => roles.some((role) => role !== 'PROPERTY_OWNER'))
  );
}

/**
 * Only a manager-firm admin (uncapped) - or the platform admin - may create an
 * additional property. A board admin can also hold several properties today
 * (the old one-property cap has been removed), so this is no longer their
 * gate - board admins create properties through the board-admin registration
 * flow, not here.
 */
export function canCreateProperty(user: CurrentUser): boolean {
  return isAdmin(user) || Object.values(user.roleByProperty).some((roles) => roles.includes('PROPERTY_MANAGER_ADMIN'));
}

/** The single property this account is staff on, or null if it manages none or several. */
export function singleManagedPropertyId(user: CurrentUser): string | null {
  const managedPropertyIds = Object.entries(user.roleByProperty)
    .filter(([, roles]) => roles.some((role) => role !== 'PROPERTY_OWNER'))
    .map(([propertyId]) => propertyId);
  return managedPropertyIds.length === 1 ? managedPropertyIds[0] : null;
}

/** True if the caller holds an ADMIN-tier role (board or manager-firm) on this specific property. */
export function isAdminTierOnProperty(user: CurrentUser, propertyId: string): boolean {
  const roles = user.roleByProperty[propertyId] ?? [];
  return isAdmin(user) || roles.includes('PROPERTY_BOARD_ADMIN') || roles.includes('PROPERTY_MANAGER_ADMIN');
}

/**
 * All properties this account holds a volunteer board mandate (admin or
 * member) on, in map iteration order. A caller can hold more than one
 * simultaneous mandate on different properties.
 */
export function boardPropertyIds(user: CurrentUser): string[] {
  return Object.entries(user.roleByProperty)
    .filter(([, roles]) => roles.includes('PROPERTY_BOARD_ADMIN') || roles.includes('PROPERTY_BOARD_MEMBER'))
    .map(([propertyId]) => propertyId);
}

/**
 * The id of the first property this account is a volunteer board
 * member/admin of, or null. Kept only for the current single-mandate UI
 * (dashboard, sidebar, onboarding banner) - callers that need to show every
 * mandate a multi-mandate account holds should use boardPropertyIds
 * instead, which this delegates to.
 */
export function boardPropertyId(user: CurrentUser): string | null {
  return boardPropertyIds(user)[0] ?? null;
}

/** True if the caller holds a professional management-firm role (admin or member) on any property. */
export function isManagerTier(user: CurrentUser): boolean {
  return Object.values(user.roleByProperty).some(
    (roles) => roles.includes('PROPERTY_MANAGER_ADMIN') || roles.includes('PROPERTY_MANAGER_MEMBER'),
  );
}

/** True if the caller holds a board role (admin or member) specifically on this property. */
export function isBoardTierOnProperty(user: CurrentUser, propertyId: string): boolean {
  const roles = user.roleByProperty[propertyId] ?? [];
  return roles.includes('PROPERTY_BOARD_ADMIN') || roles.includes('PROPERTY_BOARD_MEMBER');
}

/** True if the caller holds a manager-firm role (admin or member) specifically on this property. */
export function isManagerTierOnProperty(user: CurrentUser, propertyId: string): boolean {
  const roles = user.roleByProperty[propertyId] ?? [];
  return roles.includes('PROPERTY_MANAGER_ADMIN') || roles.includes('PROPERTY_MANAGER_MEMBER');
}

/** True if the caller holds PROPERTY_OWNER specifically on this property (alongside any other role). */
export function isOwnerOnProperty(user: CurrentUser, propertyId: string): boolean {
  return (user.roleByProperty[propertyId] ?? []).includes('PROPERTY_OWNER');
}

/**
 * True if the account owns at least one unit anywhere - i.e. whether the
 * consolidated "copropriétaire" space exists for it at all. A pure
 * mandataire (board/manager role with no PROPERTY_OWNER grant on any
 * property, e.g. a professional gérant) has no personal space to land on.
 */
export function hasCopro(user: CurrentUser): boolean {
  return Object.values(user.roleByProperty).some((roles) => roles.includes('PROPERTY_OWNER'));
}

/**
 * Aucune affectation valide nulle part : ni lot possédé, ni rôle de gestion.
 *
 * C'est l'état d'un compte créé depuis un lien d'invitation public. Soumettre
 * la demande ne pose aucun rôle - seul le syndic en pose un, en la validant
 * (AcceptMembershipRequestService côté API) -, si bien qu'entre les deux il n'y
 * a rien derrière « Mes échéances », « Mes paiements » ou « Messagerie » qu'un
 * écran vide. Ces entrées sont donc masquées jusqu'à la validation ; seul le
 * tableau de bord reste, où le lot demandé s'affiche en attente.
 */
export function hasNoPropertyAccess(user: CurrentUser): boolean {
  return !hasCopro(user) && !canManageProperties(user);
}

/**
 * Gates the accounting module (property:accounting:read) - board/manager
 * tiers only, admin or member (see V2__rbac_permissions.sql: both tiers
 * hold the identical bundle for this property, unlike some other
 * permissions that split by tier).
 */
export function canReadAccounting(user: CurrentUser, propertyId: string): boolean {
  return isAdmin(user) || isBoardTierOnProperty(user, propertyId) || isManagerTierOnProperty(user, propertyId);
}

/** property:accounting:write - same population as canReadAccounting today (see above). */
export function canWriteAccounting(user: CurrentUser, propertyId: string): boolean {
  return canReadAccounting(user, propertyId);
}

/** document:read - board/manager tiers on this property (same population as canReadAccounting). */
export function canReadDocuments(user: CurrentUser, propertyId: string): boolean {
  return isAdmin(user) || isBoardTierOnProperty(user, propertyId) || isManagerTierOnProperty(user, propertyId);
}

/** document:write - same population as canReadDocuments today (see above). */
export function canWriteDocuments(user: CurrentUser, propertyId: string): boolean {
  return canReadDocuments(user, propertyId);
}
