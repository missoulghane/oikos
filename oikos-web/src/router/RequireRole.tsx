import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useCurrentUser, isAdmin, isManagerOfAny } from '@/features/identity/me';
import { Loader } from '@/shared/components/Loader/Loader';

type Role = 'ADMIN' | 'MANAGER';

interface RequireRoleProps {
  allow: Role[];
  children: ReactNode;
}

/**
 * Route guard for the ADMIN/MANAGER-only areas (property creation, contact
 * management). MANAGER isn't a JWT claim (see useAuthStore) - it is derived
 * from managedPropertyIds, resolved per-request from GET /users/me - so this
 * guard fetches the current user rather than reading roles off the token.
 */
export function RequireRole({ allow, children }: RequireRoleProps) {
  const currentUser = useCurrentUser();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  if (!currentUser.data) {
    return <Navigate to="/forbidden" replace />;
  }

  const allowed =
    (allow.includes('ADMIN') && isAdmin(currentUser.data)) ||
    (allow.includes('MANAGER') && isManagerOfAny(currentUser.data));

  if (!allowed) {
    return <Navigate to="/forbidden" replace />;
  }

  return <>{children}</>;
}
