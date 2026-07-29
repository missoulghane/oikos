import { lazy, Suspense } from 'react';
import type { RouteObject } from 'react-router-dom';
import { Loader } from '@/shared/components/Loader/Loader';

const LoginPage = lazy(() => import('@/features/identity/auth').then((m) => ({ default: m.LoginPage })));
const RegisterUserPage = lazy(() => import('@/features/identity/register').then((m) => ({ default: m.RegisterUserPage })));
const RegisterPropertyManagerPage = lazy(() =>
  import('@/features/identity/register').then((m) => ({ default: m.RegisterPropertyManagerPage })),
);
const VerifyEmailPage = lazy(() => import('@/features/identity/register').then((m) => ({ default: m.VerifyEmailPage })));
const ActivateAccountPage = lazy(() =>
  import('@/features/identity/register').then((m) => ({ default: m.ActivateAccountPage })),
);
const AcceptInvitationPage = lazy(() =>
  import('@/features/identity/register').then((m) => ({ default: m.AcceptInvitationPage })),
);

export const publicRoutes: RouteObject[] = [
  {
    path: '/login',
    element: (
      <Suspense fallback={<Loader />}>
        <LoginPage />
      </Suspense>
    ),
  },
  {
    path: '/register/user',
    element: (
      <Suspense fallback={<Loader />}>
        <RegisterUserPage />
      </Suspense>
    ),
  },
  {
    path: '/register/property-manager',
    element: (
      <Suspense fallback={<Loader />}>
        <RegisterPropertyManagerPage />
      </Suspense>
    ),
  },
  {
    path: '/verify-email',
    element: (
      <Suspense fallback={<Loader />}>
        <VerifyEmailPage />
      </Suspense>
    ),
  },
  {
    path: '/activate-account',
    element: (
      <Suspense fallback={<Loader />}>
        <ActivateAccountPage />
      </Suspense>
    ),
  },
  {
    path: '/accept-invitation',
    element: (
      <Suspense fallback={<Loader />}>
        <AcceptInvitationPage />
      </Suspense>
    ),
  },
];
