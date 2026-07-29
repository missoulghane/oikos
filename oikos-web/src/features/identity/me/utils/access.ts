import type { CurrentUser } from '@/features/identity/me/types/me.types';

export function isAdmin(user: CurrentUser): boolean {
  return user.roles.includes('ROLE_ADMIN') || user.roles.includes('ROLE_MASTER');
}

export function isManagerOfAny(user: CurrentUser): boolean {
  return user.managedPropertyIds.length > 0;
}

/** ADMIN, or a MANAGER of at least one property - the accounts allowed to administer properties. */
export function canManageProperties(user: CurrentUser): boolean {
  return isAdmin(user) || isManagerOfAny(user);
}
