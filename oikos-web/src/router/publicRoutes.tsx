import { lazy, Suspense } from 'react';
import type { RouteObject } from 'react-router-dom';
import { Loader } from '@/shared/components/Loader/Loader';

const LoginPage = lazy(() => import('@/features/auth').then((m) => ({ default: m.LoginPage })));

export const publicRoutes: RouteObject[] = [
  {
    path: '/login',
    element: (
      <Suspense fallback={<Loader />}>
        <LoginPage />
      </Suspense>
    ),
  },
];
