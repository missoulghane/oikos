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
  return isAdmin(user) || Object.values(user.roleByProperty).some((role) => role !== 'PROPERTY_OWNER');
}

/**
 * Only a manager-firm admin (uncapped) - or the platform admin - may create an
 * additional property. A board admin already holds their one and only
 * allowed property from registration onward (see the backend's
 * EnforcePropertyCreationLimitService), so this never lets them create a
 * second one.
 */
export function canCreateProperty(user: CurrentUser): boolean {
  return isAdmin(user) || Object.values(user.roleByProperty).includes('PROPERTY_MANAGER_ADMIN');
}

/** The single property this account is staff on, or null if it manages none or several. */
export function singleManagedPropertyId(user: CurrentUser): string | null {
  const managedPropertyIds = Object.entries(user.roleByProperty)
    .filter(([, role]) => role !== 'PROPERTY_OWNER')
    .map(([propertyId]) => propertyId);
  return managedPropertyIds.length === 1 ? managedPropertyIds[0] : null;
}

/** True if the caller holds an ADMIN-tier role (board or manager-firm) on this specific property. */
export function isAdminTierOnProperty(user: CurrentUser, propertyId: string): boolean {
  const role = user.roleByProperty[propertyId];
  return isAdmin(user) || role === 'PROPERTY_BOARD_ADMIN' || role === 'PROPERTY_MANAGER_ADMIN';
}

/** The id of the single property this account is a volunteer board member/admin of, or null. */
export function boardPropertyId(user: CurrentUser): string | null {
  const entry = Object.entries(user.roleByProperty).find(
    ([, role]) => role === 'PROPERTY_BOARD_ADMIN' || role === 'PROPERTY_BOARD_MEMBER',
  );
  return entry ? entry[0] : null;
}

/** True if the caller holds a professional management-firm role (admin or member) on any property. */
export function isManagerTier(user: CurrentUser): boolean {
  return Object.values(user.roleByProperty).some(
    (role) => role === 'PROPERTY_MANAGER_ADMIN' || role === 'PROPERTY_MANAGER_MEMBER',
  );
}

/** True if the caller holds a board role (admin or member) specifically on this property. */
export function isBoardTierOnProperty(user: CurrentUser, propertyId: string): boolean {
  const role = user.roleByProperty[propertyId];
  return role === 'PROPERTY_BOARD_ADMIN' || role === 'PROPERTY_BOARD_MEMBER';
}

/** True if the caller holds a manager-firm role (admin or member) specifically on this property. */
export function isManagerTierOnProperty(user: CurrentUser, propertyId: string): boolean {
  const role = user.roleByProperty[propertyId];
  return role === 'PROPERTY_MANAGER_ADMIN' || role === 'PROPERTY_MANAGER_MEMBER';
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
