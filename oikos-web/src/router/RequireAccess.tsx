import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useCurrentUser, type CurrentUser } from '@/features/identity/me';
import { Loader } from '@/shared/components/Loader/Loader';

interface RequireAccessProps {
  check: (user: CurrentUser) => boolean;
  children: ReactNode;
}

/**
 * Route guard driven by a permission-flavored predicate rather than a list of
 * role names (see access.ts) - the API remains the real enforcement
 * boundary; this only avoids a dead-end click for a user who cannot use the
 * page anyway.
 */
export function RequireAccess({ check, children }: RequireAccessProps) {
  const currentUser = useCurrentUser();

  if (currentUser.isLoading) {
    return <Loader />;
  }

  if (!currentUser.data || !check(currentUser.data)) {
    return <Navigate to="/forbidden" replace />;
  }

  return <>{children}</>;
}
