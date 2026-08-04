import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '@/app/store';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { LoginForm } from '@/features/identity/auth/components/LoginForm';
import { useLogin } from '@/features/identity/auth/hooks/useLogin';
import { useInvitationPreview } from '@/features/identity/invitations/hooks/useInvitationPreview';
import { useInvitationAvailableUnits } from '@/features/identity/invitations/hooks/useInvitationAvailableUnits';
import { useAcceptInvitation } from '@/features/identity/invitations/hooks/useAcceptInvitation';
import { useSubmitMembershipRequest } from '@/features/identity/invitations/hooks/useSubmitMembershipRequest';
import { InvitationSignupForm } from '@/features/identity/invitations/components/InvitationSignupForm';
import { UnitPicker } from '@/features/identity/invitations/components/UnitPicker';
import type { InvitationSignupFormValues } from '@/features/identity/invitations/schemas/invitationSignupSchema';

const UNUSABLE_REASON_LABELS: Record<string, string> = {
  DISABLED: "Ce lien d'invitation a été désactivé.",
  CONSUMED: "Ce lien d'invitation a déjà été utilisé.",
  EXPIRED: "Ce lien d'invitation a expiré.",
};

export function InvitationLandingPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  const { data: preview, isPending: isPreviewPending, isError: isPreviewError } = useInvitationPreview(token);
  const needsUnit = preview ? preview.type !== 'PRIVATE_WITH_UNIT' : false;
  const { data: availableUnits } = useInvitationAvailableUnits(token, Boolean(preview?.usable) && needsUnit);

  const [unitId, setUnitId] = useState<string | null>(null);
  const [showLogin, setShowLogin] = useState(false);
  const resumeAfterLoginRef = useRef(false);

  const acceptMutation = useAcceptInvitation();
  const submitMutation = useSubmitMembershipRequest();
  const loginMutation = useLogin();
  const mutation = preview?.type === 'PUBLIC' ? submitMutation : acceptMutation;

  function confirm() {
    if (!token || (needsUnit && !unitId)) {
      return;
    }
    mutation.mutate({ token, unitId: unitId ?? undefined });
  }

  const confirmRef = useRef(confirm);
  useEffect(() => {
    confirmRef.current = confirm;
  });

  // "Connexion + reprise" : once the inline login succeeds, isAuthenticated
  // flips reactively (zustand) and this effect relaunches the same
  // confirmation the user already set up (unit choice included) - no
  // redirect, no state to carry across pages.
  useEffect(() => {
    if (isAuthenticated && resumeAfterLoginRef.current) {
      resumeAfterLoginRef.current = false;
      confirmRef.current();
    }
  }, [isAuthenticated]);

  function handleSignupSubmit(values: InvitationSignupFormValues) {
    if (!token || (needsUnit && !unitId)) {
      return;
    }
    mutation.mutate({ token, unitId: unitId ?? undefined, ...values });
  }

  function handleLoginSubmit(values: { identifier: string; password: string }) {
    loginMutation.mutate(values);
  }

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

  if (mutation.isSuccess) {
    return (
      <AuthLayout>
        <Card>
          <h2 className="mb-4 text-lg font-semibold text-gray-900">{preview.propertyName}</h2>
          <p className="text-sm text-gray-600">
            {preview.type === 'PUBLIC'
              ? "Votre candidature a bien été envoyée. Le gestionnaire de la copropriété va l'examiner et vous serez notifié de sa décision."
              : isAuthenticated
                ? 'Invitation acceptée. Vous avez maintenant accès à ce lot.'
                : 'Invitation acceptée. Vous pouvez maintenant vous connecter pour accéder à votre espace.'}
          </p>
          <Link to="/login" className="mt-4 inline-block text-sm font-medium text-gray-900 underline">
            Aller à la connexion
          </Link>
        </Card>
      </AuthLayout>
    );
  }

  const confirmDisabled = needsUnit && !unitId;

  return (
    <AuthLayout>
      <Card>
        <h2 className="mb-1 text-lg font-semibold text-gray-900">{preview.propertyName}</h2>
        <p className="mb-4 text-sm text-gray-500">{preview.propertyAddress}</p>

        <div className="flex flex-col gap-4">
          {preview.type === 'PRIVATE_WITH_UNIT' && (
            <p className="text-sm text-gray-600">
              Vous êtes invité(e) pour le lot <span className="font-medium">{preview.unitNumber}</span> (
              {preview.unitTypeName}).
            </p>
          )}

          {needsUnit && (
            <UnitPicker units={availableUnits?.content ?? []} value={unitId} onChange={setUnitId} />
          )}

          {isAuthenticated ? (
            <Button onClick={confirm} isLoading={mutation.isPending} disabled={confirmDisabled}>
              {preview.type === 'PUBLIC' ? 'Envoyer ma candidature' : "Rejoindre la copropriété"}
            </Button>
          ) : showLogin ? (
            <>
              <LoginForm
                onSubmit={handleLoginSubmit}
                isSubmitting={loginMutation.isPending}
                errorMessage={loginMutation.error ? getErrorMessage(loginMutation.error) : undefined}
              />
              <button
                type="button"
                onClick={() => {
                  setShowLogin(false);
                  resumeAfterLoginRef.current = false;
                }}
                className="text-sm font-medium text-gray-900 underline"
              >
                Créer un nouveau compte à la place
              </button>
            </>
          ) : (
            <>
              <InvitationSignupForm
                onSubmit={handleSignupSubmit}
                isSubmitting={mutation.isPending}
                errorMessage={mutation.error ? getErrorMessage(mutation.error) : undefined}
                disabled={confirmDisabled}
              />
              <button
                type="button"
                onClick={() => {
                  setShowLogin(true);
                  resumeAfterLoginRef.current = true;
                }}
                className="text-sm font-medium text-gray-900 underline"
              >
                J'ai déjà un compte Oikos
              </button>
            </>
          )}
        </div>
      </Card>
    </AuthLayout>
  );
}
