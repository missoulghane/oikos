export { useCurrentUser } from '@/features/identity/me/hooks/useCurrentUser';
export { ProfilePage } from '@/features/identity/me/pages/ProfilePage';
export {
  isAdmin,
  canManageProperties,
  canCreateProperty,
  singleManagedPropertyId,
  isAdminTierOnProperty,
  boardPropertyId,
  isManagerTier,
  isBoardTierOnProperty,
  isManagerTierOnProperty,
  canReadAccounting,
  canWriteAccounting,
  canReadDocuments,
  canWriteDocuments,
} from '@/features/identity/me/utils/access';
export type { CurrentUser, PropertyRoleName } from '@/features/identity/me/types/me.types';
