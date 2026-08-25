import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '@/app/store';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { Checkbox } from '@/shared/components/Checkbox/Checkbox';
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

/**
 * Wizard en deux temps : (1) le lot, (2) la connexion ou la création de
 * compte. La création passe par une navigation complète vers /register/user
 * (pas un formulaire en ligne) - le compte suit la vérification d'email
 * comme n'importe quel autre, en emportant l'URL de cette page dans returnTo,
 * si bien que l'étape 2 se termine d'elle-même au retour authentifié (même
 * mécanisme après une inscription qu'après une simple connexion).
 *
 * <p>Trois liens y aboutissent, et un seul écran les sert.
 *
 * <p>Le lien PUBLIC circule (QR code dans le hall, groupe de voisins) : le
 * visiteur choisit son lot dans la liste des lots libres.
 *
 * <p>Le lien PRIVÉ est adressé nominativement, pour un lot précis, depuis la
 * fiche du contact : le lot est annoncé plutôt que demandé. Il reste
 * remplaçable - « ce n'est pas votre lot ? » ouvre le sélecteur - parce
 * qu'un syndic se trompe de ligne, et parce que le lot désigné n'engage
 * personne tant que le syndic n'a pas validé la demande. Les deux déposent
 * la même demande d'adhésion, soumise à la même validation : c'est
 * précisément ce qui rend ce changement de lot sans danger.
 *
 * <p>Le lien vers un siège au CONSEIL SYNDICAL ne porte sur aucun lot : ni
 * sélecteur, ni certification, et il est le seul à passer encore par
 * l'acceptation directe (voir AcceptInvitationService, côté API).
 */
export function InvitationLandingPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const token = searchParams.get('token');
  const chosenUnitId = searchParams.get('unitId');
  const isCertified = searchParams.get('certified') === '1';
  // Posé par « ce n'est pas votre lot ? » : le sélecteur reste ouvert même
  // après un aller-retour par /login, sans quoi la page se refermerait sur le
  // lot d'origine et redemanderait le même refus.
  const isChoosingOwnLot = searchParams.get('chooseLot') === '1';
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  const { data: preview, isPending: isPreviewPending, isError: isPreviewError } = useInvitationPreview(token);
  const { data: availableUnits } = useInvitationAvailableUnits(token, Boolean(preview?.usable));

  const acceptMutation = useAcceptInvitation();
  const submitMutation = useSubmitMembershipRequest();

  const isBoardSeat = Boolean(preview?.boardRole);
  // Le lot désigné ne s'impose que tant que l'invité ne l'a pas récusé.
  const designatedUnitId = isChoosingOwnLot ? null : (preview?.targetUnitId ?? null);
  const unitId = chosenUnitId ?? designatedUnitId;
  const showUnitPicker = !isBoardSeat && designatedUnitId === null;

  // Un siège au conseil s'accepte encore directement : il n'attribue rien, la
  // validation par un administrateur vient ensuite. Tout le reste - lien
  // public comme lien privé - dépose une demande d'adhésion que le syndic
  // valide, et c'est la même route pour les deux.
  const mutation = isBoardSeat ? acceptMutation : submitMutation;

  // La certification n'a de sens que sur un lot : elle porte sur lui.
  const canProceed = isBoardSeat || (unitId !== null && isCertified);

  function updateParams(mutate: (params: URLSearchParams) => void) {
    setSearchParams((params) => {
      mutate(params);
      return params;
    });
  }

  function selectUnit(newUnitId: string) {
    updateParams((params) => {
      params.set('unitId', newUnitId);
      // Changer de lot rouvre la question : la déclaration porte sur « ce
      // lot », pas sur le formulaire.
      params.delete('certified');
    });
  }

  function setCertified(certified: boolean) {
    updateParams((params) => {
      if (certified) {
        params.set('certified', '1');
      } else {
        params.delete('certified');
      }
    });
  }

  function setChoosingOwnLot(choosing: boolean) {
    updateParams((params) => {
      if (choosing) {
        params.set('chooseLot', '1');
      } else {
        params.delete('chooseLot');
      }
      // Dans les deux sens on repart du lot proposé : cocher ouvre un
      // sélecteur vide, décocher revient au lot du syndic.
      params.delete('unitId');
      params.delete('certified');
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
    if (isAuthenticated && token && canProceed && preview?.usable && !hasAutoConfirmed.current) {
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
  }, [isAuthenticated, token, unitId, canProceed, preview?.usable]);

  // On success the caller is already authenticated (accept/submit only ever
  // run from the auto-confirm effect above) - land them straight on their own
  // space instead of an intermediate message with a "go to login" link that
  // wouldn't make sense for someone already logged in.
  //
  // Le tableau de bord dans tous les cas : c'est là que le lot apparaît, en
  // attente de validation (voir PendingUnitCard). Le lien menait jusqu'ici à
  // « Mes invitations », un écran sans entrée de menu où l'on n'avait plus
  // aucune raison de repasser.
  useEffect(() => {
    if (autoConfirmStatus === 'success') {
      navigate('/dashboard', { replace: true });
    }
  }, [autoConfirmStatus, navigate]);

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
          <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white/90">Invitation</h2>
          <Alert message="Ce lien d'invitation est invalide." />
        </Card>
      </AuthLayout>
    );
  }

  if (!preview.usable) {
    return (
      <AuthLayout>
        <Card>
          <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white/90">Invitation</h2>
          <Alert message={UNUSABLE_REASON_LABELS[preview.reason ?? ''] ?? "Ce lien d'invitation n'est plus valide."} />
          <Link to="/login" className="mt-4 inline-block text-sm font-medium text-gray-900 dark:text-white/90 underline">
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

  // L'URL entière fait le voyage, pas seulement le jeton et le lot : la
  // certification et le choix « un autre lot » sont des réponses déjà données,
  // et les redemander au retour de /login serait redemander deux fois la même
  // chose.
  function invitationReturnTo(): string {
    const params = new URLSearchParams(searchParams);
    if (unitId) {
      params.set('unitId', unitId);
    }
    return `/invitations?${params.toString()}`;
  }

  const returnTo = invitationReturnTo();
  // Le lot est déposé en même temps que le compte (voir RegisterUserService) :
  // au retour de la vérification d'email il n'y a plus rien à confirmer ici,
  // autant envoyer directement au tableau de bord plutôt que de repasser par
  // une page dont l'effet d'auto-confirmation ne ferait rien. Un siège au
  // conseil, lui, s'accepte après connexion : il revient ici.
  const registerReturnTo = isBoardSeat ? returnTo : '/dashboard';
  const registerUrl = `/register/user?returnTo=${encodeURIComponent(registerReturnTo)}${
    unitId ? `&invitationToken=${encodeURIComponent(token)}&unitId=${encodeURIComponent(unitId)}` : ''
  }`;

  const designatedUnitLabel = preview.targetUnitTypeName
    ? `${preview.targetUnitNumber} — ${preview.targetUnitTypeName}`
    : preview.targetUnitNumber;

  return (
    <AuthLayout>
      <Card>
        {/* Le visiteur arrive d'un lien reçu par message ou par email : la page
            s'ouvrait sur le nom de la copropriété et un sélecteur de lot, sans
            jamais dire où il était ni ce qu'il s'apprêtait à faire. */}
        <h2 className="mb-1 text-lg font-semibold text-gray-900 dark:text-white/90">
          Rejoignez votre espace copropriétaire
        </h2>
        <p className="text-sm text-gray-700 dark:text-gray-300">{preview.propertyName}</p>
        <p className="text-sm text-gray-500 dark:text-gray-400">{preview.propertyAddress}</p>

        {/* Un siège au conseil ne se valide pas comme une adhésion : rien à
            promettre ici sur un lot ou une demande à examiner. */}
        {isBoardSeat ? (
          <p className="mb-4 mt-3 text-sm text-gray-500 dark:text-gray-400">
            Connectez-vous ou créez votre compte pour rejoindre le conseil syndical de cette copropriété.
          </p>
        ) : (
          <div className="mb-4 mt-3 text-sm text-gray-500 dark:text-gray-400">
            <p>Pour rejoindre vos voisins et finaliser votre accès, c'est très simple :</p>
            <ul className="mt-2 flex list-disc flex-col gap-1 pl-5">
              <li>
                {designatedUnitId
                  ? 'Confirmez le lot que le syndic vous a attribué, ci-dessous.'
                  : 'Sélectionnez votre lot (appartement, parking, box…) dans la liste ci-dessous.'}
              </li>
              <li>Le syndic prend le relais : il valide votre demande pour sécuriser l'accès.</li>
              <li>
                Une fois votre demande validée, vous recevrez une notification et votre espace de gestion sera
                entièrement à vous ! 🎉
              </li>
            </ul>
          </div>
        )}

        <div className="flex flex-col gap-4">
          {!isBoardSeat && (
            <div>
              <p className="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                {designatedUnitId ? '1. Confirmez votre lot' : '1. Choisissez votre lot'}
              </p>

              {/* Le lot désigné est annoncé, pas caché dans un sélecteur
                  pré-rempli : c'est l'information que le destinataire doit
                  vérifier en premier, et un <select> déjà rempli se survole
                  sans se lire. */}
              {designatedUnitId && (
                <div className="rounded-lg border border-brand-200 bg-brand-50 p-4 dark:border-brand-500/30 dark:bg-brand-500/10">
                  <p className="text-xs font-medium uppercase tracking-wide text-brand-600 dark:text-brand-400">
                    Votre lot
                  </p>
                  <p className="mt-1 text-lg font-semibold text-gray-900 dark:text-white/90">{designatedUnitLabel}</p>
                </div>
              )}

              {showUnitPicker && (
                <UnitPicker units={availableUnits?.content ?? []} value={unitId} onChange={selectUnit} />
              )}

              {/* Un syndic se trompe de ligne, et un lot désigné n'engage
                  personne tant que la demande n'est pas validée : la porte de
                  sortie est ouverte, et sans danger puisque le syndic tranche. */}
              {preview.targetUnitId && (
                <Checkbox
                  className="mt-3"
                  name="chooseLot"
                  label="Ce n'est pas votre lot ? Choisir un autre lot"
                  checked={isChoosingOwnLot}
                  onChange={(event) => setChoosingOwnLot(event.target.checked)}
                />
              )}
            </div>
          )}

          {/* Un lot se désigne soi-même, ici : sur un lien public il se choisit
              dans une liste ouverte, sur un lien privé il se confirme ou se
              remplace. Dans les deux cas la déclaration fait porter la
              désignation par son auteur, et c'est elle qui ouvre l'étape
              suivante. Le syndic valide ensuite. */}
          {!isBoardSeat && unitId && (
            <Checkbox
              name="certified"
              label="Je certifie être le propriétaire ou le mandataire pour ce lot."
              checked={isCertified}
              onChange={(event) => setCertified(event.target.checked)}
            />
          )}

          {canProceed && (
            <div>
              <p className="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                {isBoardSeat ? 'Connectez-vous ou créez un compte' : '2. Connectez-vous ou créez un compte'}
              </p>
              {isAuthenticated ? (
                <>
                  {autoConfirmStatus === 'pending' && <Loader label="Finalisation…" />}
                  {autoConfirmStatus === 'error' && <Alert message={getErrorMessage(autoConfirmError)} />}
                </>
              ) : (
                <div className="flex flex-col gap-2">
                  <Button type="button" onClick={() => navigate(registerUrl)}>
                    Créer un compte
                  </Button>
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={() => navigate(`/login?returnTo=${encodeURIComponent(returnTo)}`)}
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
