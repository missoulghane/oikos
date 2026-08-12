import type { CurrentUser } from '@/features/identity/me/types/me.types';

// Mirrors the subset of oikos-web's identity/me/utils/access.ts needed on
// mobile - the rest (canManageProperties, isAdminTierOnProperty, board
// mandate helpers…) gates property-mngt UI, out of scope here.
export function isOwnerOnProperty(user: CurrentUser, propertyId: string): boolean {
  return (user.roleByProperty[propertyId] ?? []).includes('PROPERTY_OWNER');
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
