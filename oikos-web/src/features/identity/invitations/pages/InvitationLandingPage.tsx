import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '@/app/store';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useInvitationPreview } from '@/features/identity/invitations/hooks/useInvitationPreview';
import { useInvitationAvailableUnits } from '@/features/identity/invitations/hooks/useInvitationAvailableUnits';
import { useAcceptInvitation } from '@/features/identity/invitations/hooks/useAcceptInvitation';
import { useSubmitMembershipRequest } from '@/features/identity/invitations/hooks/useSubmitMembershipRequest';
import { UnitPicker } from '@/features/identity/invitations/components/UnitPicker';

const UNUSABLE_REASON_LABELS: Record<string, string> = {
  DISABLED: "Ce lien d'invitation a été désactivé.",
  CONSUMED: "Ce lien d'invitation a déjà été utilisé.",
  EXPIRED: "Ce lien d'invitation a expiré.",
};

type AutoConfirmStatus = 'idle' | 'pending' | 'success' | 'error';

function invitationReturnTo(token: string, unitId: string): string {
  return `/invitations?token=${encodeURIComponent(token)}&unitId=${encodeURIComponent(unitId)}`;
}

/**
 * 2-step wizard: (1) choose a lot, (2) log in or create an account. Account
 * creation is a full navigation to the standard /register/user page (not an
 * inline form) - it goes through email verification like any other account,
 * carrying this page's own URL back via returnTo so step 2 auto-completes
 * the moment the visitor returns authenticated (same mechanism whether they
 * registered or simply logged in with an existing account).
 */
export function InvitationLandingPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const token = searchParams.get('token');
  const unitId = searchParams.get('unitId');
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  const { data: preview, isPending: isPreviewPending, isError: isPreviewError } = useInvitationPreview(token);
  const { data: availableUnits } = useInvitationAvailableUnits(token, Boolean(preview?.usable));

  const acceptMutation = useAcceptInvitation();
  const submitMutation = useSubmitMembershipRequest();
  const mutation = preview?.type === 'PUBLIC' ? submitMutation : acceptMutation;

  function selectUnit(newUnitId: string) {
    setSearchParams((params) => {
      params.set('unitId', newUnitId);
      return params;
    });
  }

  // mutation's identity changes every render (TanStack Query), so it can't
  // sit in the effect's own dependency array below without re-firing the
  // mutate() call on every unrelated re-render - kept in a ref, refreshed
  // post-render, instead.
  const mutationRef = useRef(mutation);
  useEffect(() => {
    mutationRef.current = mutation;
  });

  // Step 2 auto-completes as soon as a lot is chosen and the caller is
  // authenticated - covers both "already logged in on arrival" and "just
  // came back from /login or /register/user via returnTo" identically, with
  // no state to carry across pages besides the URL itself. hasAutoConfirmed
  // guards against StrictMode's dev-mode double effect invocation actually
  // firing the mutation twice (a PRIVATE invitation is single-use, so a
  // second call would 409 against the first's own success).
  //
  // Completion is tracked via this local state, set from mutateAsync()'s own
  // promise, rather than the mutation object's isPending/isSuccess/isError -
  // under StrictMode's synthetic mount/cleanup/remount, the mutation hook's
  // own reactive state (and even its mutate()-level onSuccess/onError
  // callbacks) can silently fail to notify this component for the call fired
  // during the first (discarded) pass, even though the request itself
  // completes; the plain setState below doesn't depend on that subscription
  // and reliably drives the render either way. Confirmed dev-only (StrictMode
  // is stripped from production builds) via a side-by-side prod-build test.
  const hasAutoConfirmed = useRef(false);
  const [autoConfirmStatus, setAutoConfirmStatus] = useState<AutoConfirmStatus>('idle');
  const [autoConfirmError, setAutoConfirmError] = useState<unknown>(null);
  useEffect(() => {
    if (isAuthenticated && token && unitId && preview?.usable && !hasAutoConfirmed.current) {
      hasAutoConfirmed.current = true;
      setAutoConfirmStatus('pending');
      mutationRef.current.mutateAsync({ token, unitId }).then(
        () => setAutoConfirmStatus('success'),
        (error: unknown) => {
          setAutoConfirmError(error);
          setAutoConfirmStatus('error');
        },
      );
    }
  }, [isAuthenticated, token, unitId, preview?.usable]);

  // On success the caller is already authenticated (accept/submit only ever
  // run from the auto-confirm effect above) - land them straight on their own
  // space instead of an intermediate message with a "go to login" link that
  // wouldn't make sense for someone already logged in.
  const isPublic = preview?.type === 'PUBLIC';
  useEffect(() => {
    if (autoConfirmStatus === 'success') {
      navigate(isPublic ? '/property-ownership/membership-requests' : '/property-ownership/units', {
        replace: true,
      });
    }
  }, [autoConfirmStatus, isPublic, navigate]);

  if (isPreviewPending) {
    return (
      <AuthLayout>
        <Card>
          <Loader />
        </Card>
      </AuthLayout>
    );
  }

  if (isPreviewError || !preview || !token) {
    return (
      <AuthLayout>
        <Card>
          <h2 className="mb-4 text-lg font-semibold text-gray-900">Invitation</h2>
          <Alert message="Ce lien d'invitation est invalide." />
        </Card>
      </AuthLayout>
    );
  }

  if (!preview.usable) {
    return (
      <AuthLayout>
        <Card>
          <h2 className="mb-4 text-lg font-semibold text-gray-900">Invitation</h2>
          <Alert message={UNUSABLE_REASON_LABELS[preview.reason ?? ''] ?? "Ce lien d'invitation n'est plus valide."} />
          <Link to="/login" className="mt-4 inline-block text-sm font-medium text-gray-900 underline">
            Retour à la connexion
          </Link>
        </Card>
      </AuthLayout>
    );
  }

  if (autoConfirmStatus === 'success') {
    return (
      <AuthLayout>
        <Card>
          <Loader />
        </Card>
      </AuthLayout>
    );
  }

  const returnTo = unitId ? invitationReturnTo(token, unitId) : null;
  // PUBLIC invitations submit their membership request as part of
  // registration itself (see RegisterUserService), so by the time the new
  // account verifies its email and logs back in there's nothing left to
  // confirm here - send it straight to "Mes invitations" instead of bouncing
  // back through this page for an auto-confirm effect that would just be a
  // no-op. PRIVATE still needs the return trip: accept only ever runs
  // post-login (see the auto-confirm effect above), so its returnTo (both
  // here and on the "J'ai déjà un compte" button below) stays pointed here.
  const registerReturnTo = unitId && isPublic ? '/property-ownership/membership-requests' : returnTo;
  const registerUrl =
    registerReturnTo &&
    `/register/user?returnTo=${encodeURIComponent(registerReturnTo)}${
      isPublic && unitId ? `&invitationToken=${encodeURIComponent(token)}&unitId=${encodeURIComponent(unitId)}` : ''
    }`;

  return (
    <AuthLayout>
      <Card>
        <h2 className="mb-1 text-lg font-semibold text-gray-900">{preview.propertyName}</h2>
        <p className="mb-4 text-sm text-gray-500">{preview.propertyAddress}</p>

        <div className="flex flex-col gap-4">
          <div>
            <p className="mb-2 text-sm font-medium text-gray-700">1. Choisissez votre lot</p>
            <UnitPicker units={availableUnits?.content ?? []} value={unitId} onChange={selectUnit} />
          </div>

          {unitId && (
            <div>
              <p className="mb-2 text-sm font-medium text-gray-700">2. Connectez-vous ou créez un compte</p>
              {isAuthenticated ? (
                <>
                  {autoConfirmStatus === 'pending' && <Loader label="Finalisation…" />}
                  {autoConfirmStatus === 'error' && <Alert message={getErrorMessage(autoConfirmError)} />}
                </>
              ) : (
                <div className="flex flex-col gap-2">
                  <Button type="button" onClick={() => navigate(registerUrl as string)}>
                    Créer un compte
                  </Button>
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={() => navigate(`/login?returnTo=${encodeURIComponent(returnTo as string)}`)}
                  >
                    J'ai déjà un compte
                  </Button>
                </div>
              )}
            </div>
          )}
        </div>
      </Card>
    </AuthLayout>
  );
}
