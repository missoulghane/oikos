import { lazy, Suspense } from 'react';
import type { RouteObject } from 'react-router-dom';
import { Loader } from '@/shared/components/Loader/Loader';

const LoginPage = lazy(() => import('@/features/identity/auth').then((m) => ({ default: m.LoginPage })));
const RegisterUserPage = lazy(() => import('@/features/identity/register').then((m) => ({ default: m.RegisterUserPage })));
const OnboardingWizardPage = lazy(() =>
  import('@/features/identity/onboarding').then((m) => ({ default: m.OnboardingWizardPage })),
);
const RegisterPropertyManagerAdminPage = lazy(() =>
  import('@/features/identity/register').then((m) => ({ default: m.RegisterPropertyManagerAdminPage })),
);
const VerifyEmailPage = lazy(() => import('@/features/identity/register').then((m) => ({ default: m.VerifyEmailPage })));
const ActivateAccountPage = lazy(() =>
  import('@/features/identity/register').then((m) => ({ default: m.ActivateAccountPage })),
);
const AcceptInvitationPage = lazy(() =>
  import('@/features/identity/register').then((m) => ({ default: m.AcceptInvitationPage })),
);
const InvitationLandingPage = lazy(() =>
  import('@/features/identity/invitations').then((m) => ({ default: m.InvitationLandingPage })),
);
const ConvocationConfirmationPage = lazy(() =>
  import('@/features/property-ownership/general-meetings').then((m) => ({ default: m.ConvocationConfirmationPage })),
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
    // Sous-arbre complet : le wizard gère lui-même ses étapes (une URL chacune).
    path: '/register/board-admin/*',
    element: (
      <Suspense fallback={<Loader />}>
        <OnboardingWizardPage />
      </Suspense>
    ),
  },
  {
    path: '/register/manager-admin',
    element: (
      <Suspense fallback={<Loader />}>
        <RegisterPropertyManagerAdminPage />
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
  {
    path: '/invitations',
    element: (
      <Suspense fallback={<Loader />}>
        <InvitationLandingPage />
      </Suspense>
    ),
  },
  {
    // Public, et destinée à le rester : elle existe pour les copropriétaires qui
    // n'ont pas de compte et n'en auront pas. La déplacer derrière une
    // authentification rendrait la confirmation impossible à exactement ceux
    // pour qui elle a été faite (adr/0002 §10).
    path: '/convocations/confirmation',
    element: (
      <Suspense fallback={<Loader />}>
        <ConvocationConfirmationPage />
      </Suspense>
    ),
  },
];
