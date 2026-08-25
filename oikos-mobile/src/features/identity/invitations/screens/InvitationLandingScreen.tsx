import { useEffect, useRef, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthLayout } from '@/shared/layouts/AuthLayout';
import { Card } from '@/shared/components/Card/Card';
import { Alert } from '@/shared/components/Alert/Alert';
import { Button } from '@/shared/components/Button/Button';
import { Loader } from '@/shared/components/Loader/Loader';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { useAuthStore } from '@/app/store';
import { useInvitationPreview } from '@/features/identity/invitations/hooks/useInvitationPreview';
import { useInvitationAvailableUnits } from '@/features/identity/invitations/hooks/useInvitationAvailableUnits';
import { useAcceptInvitation } from '@/features/identity/invitations/hooks/useAcceptInvitation';
import { useSubmitMembershipRequest } from '@/features/identity/invitations/hooks/useSubmitMembershipRequest';
import { usePendingInvitationStore } from '@/features/identity/invitations/state/pendingInvitationStore';
import { UnitPicker } from '@/features/identity/invitations/components/UnitPicker';
import { colors } from '@/shared/theme/colors';
import type { AuthStackParamList } from '@/app/navigation/AuthNavigator';
import type { MainStackParamList } from '@/app/navigation/MainNavigator';

type Props = NativeStackScreenProps<AuthStackParamList & MainStackParamList, 'InvitationLanding'>;

const UNUSABLE_REASON_LABELS: Record<string, string> = {
  DISABLED: "Ce lien d'invitation a été désactivé.",
  CONSUMED: "Ce lien d'invitation a déjà été utilisé.",
  EXPIRED: "Ce lien d'invitation a expiré.",
};

type AutoConfirmStatus = 'idle' | 'pending' | 'success' | 'error';

/**
 * 2-step flow: (1) choose a lot, (2) log in or create an account. Registered
 * as a screen in both AuthNavigator and MainNavigator (see both navigators)
 * because, unlike the other email-link screens, this one is meant to work
 * whether or not the visitor is already authenticated - mirrors oikos-web's
 * InvitationLandingPage, whose auto-confirm effect checks isAuthenticated
 * directly. The "not authenticated yet, went off to log in/register" leg
 * can't rely on that effect on mobile though: logging in flips
 * isAuthenticated, which makes RootNavigator swap this screen's whole
 * navigator out from under it. See pendingInvitationStore for how that leg
 * is actually finalized.
 */
export function InvitationLandingScreen({ route, navigation }: Props) {
  const { token: routeToken, unitId: initialUnitId } = route.params ?? {};
  const token = routeToken ?? null;
  const [chosenUnitId, setChosenUnitId] = useState<string | null>(initialUnitId ?? null);
  // Posé par « ce n'est pas votre lot ? » : un syndic se trompe de ligne, et
  // le lot désigné n'engage personne tant que la demande n'est pas validée.
  const [isChoosingOwnLot, setIsChoosingOwnLot] = useState(false);
  // Un lien public circule : QR code dans le hall, groupe de voisins, capture
  // d'écran. N'importe qui peut donc désigner n'importe quel lot libre. La
  // déclaration ne vérifie rien à elle seule - c'est le syndic qui valide -
  // mais elle fait porter la désignation par son auteur, et rien ne part avant.
  const [isCertified, setIsCertified] = useState(false);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const setPendingInvitation = usePendingInvitationStore((state) => state.setPending);

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

  const mutationRef = useRef(mutation);
  useEffect(() => {
    mutationRef.current = mutation;
  });

  const hasAutoConfirmed = useRef(false);
  const [autoConfirmStatus, setAutoConfirmStatus] = useState<AutoConfirmStatus>('idle');
  const [autoConfirmError, setAutoConfirmError] = useState<unknown>(null);

  // Only reachable when this screen is mounted under MainNavigator (already
  // authenticated) - see the module doc above for why "went away and came
  // back authenticated" goes through pendingInvitationStore instead.
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

  useEffect(() => {
    if (autoConfirmStatus === 'success') {
      // property-ownership screens don't exist on mobile yet (Phase 2) - land
      // on Home for now; only reachable while already authenticated (Main
      // stack), so this route name is always valid here.
      navigation.navigate('MainTabs', { screen: 'HomeTab', params: { screen: 'Home' } });
    }
  }, [autoConfirmStatus, navigation]);

  function handleCreateAccount() {
    if (!token) {
      return;
    }
    if (!isBoardSeat && unitId) {
      // La demande d'adhésion est déposée par RegisterUserService en même temps
      // que le compte - rien à confirmer une fois l'email vérifié et la
      // connexion faite. Vaut pour les deux liens de copropriétaire.
      navigation.navigate('RegisterUser', { invitationToken: token, unitId });
    } else {
      setPendingInvitation({ token, unitId, isBoardSeat });
      navigation.navigate('RegisterUser', {});
    }
  }

  function handleExistingAccount() {
    if (!token || !preview) {
      return;
    }
    setPendingInvitation({ token, unitId, isBoardSeat });
    navigation.navigate('Login');
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
        <Card style={styles.card}>
          <Text style={styles.title}>Invitation</Text>
          <Alert message="Ce lien d'invitation est invalide." />
        </Card>
      </AuthLayout>
    );
  }

  if (!preview.usable) {
    return (
      <AuthLayout>
        <Card style={styles.card}>
          <Text style={styles.title}>Invitation</Text>
          <Alert message={UNUSABLE_REASON_LABELS[preview.reason ?? ''] ?? "Ce lien d'invitation n'est plus valide."} />
          <Button variant="secondary" onPress={() => navigation.navigate('Login')} style={styles.linkButton}>
            Retour à la connexion
          </Button>
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

  return (
    <AuthLayout scrollable>
      <Card style={styles.card}>
        <Text style={styles.title}>{preview.propertyName}</Text>
        <Text style={styles.address}>{preview.propertyAddress}</Text>

        {/* Un siège au conseil ne se valide pas comme une adhésion : rien à
            promettre ici sur un lot ou une demande à examiner. Le parcours
            copropriétaire, lui, est détaillé en trois temps parce que la
            validation du syndic s'y intercale : sans la nommer, l'attente qui
            suit se lit comme une panne. Même texte que sur le web. */}
        {isBoardSeat ? (
          <Text style={styles.introText}>
            Connectez-vous ou créez votre compte pour rejoindre le conseil syndical de cette copropriété.
          </Text>
        ) : (
          <View style={styles.intro}>
            <Text style={styles.introText}>Pour rejoindre vos voisins et finaliser votre accès, c'est très simple :</Text>
            <Text style={styles.introText}>
              {designatedUnitId
                ? '• Confirmez le lot que le syndic vous a attribué, ci-dessous.'
                : '• Sélectionnez votre lot (appartement, parking, box…) dans la liste ci-dessous.'}
            </Text>
            <Text style={styles.introText}>• Le syndic prend le relais : il valide votre demande pour sécuriser l'accès.</Text>
            <Text style={styles.introText}>
              • Une fois votre demande validée, vous recevrez une notification et votre espace de gestion sera
              entièrement à vous ! 🎉
            </Text>
          </View>
        )}

        {!isBoardSeat && (
          <View style={styles.step}>
            <Text style={styles.stepLabel}>
              {designatedUnitId ? '1. Confirmez votre lot' : '1. Choisissez votre lot'}
            </Text>

            {/* Le lot désigné est annoncé, pas caché dans un sélecteur
                pré-rempli : c'est l'information à vérifier en premier. */}
            {designatedUnitId && (
              <View style={styles.designatedLot}>
                <Text style={styles.designatedLotLabel}>VOTRE LOT</Text>
                <Text style={styles.designatedLotValue}>
                  {preview.targetUnitTypeName
                    ? `${preview.targetUnitNumber} — ${preview.targetUnitTypeName}`
                    : preview.targetUnitNumber}
                </Text>
              </View>
            )}

            {showUnitPicker && (
              <UnitPicker
                units={availableUnits?.content ?? []}
                value={unitId}
                onChange={(nextUnitId) => {
                  setChosenUnitId(nextUnitId);
                  // Changer de lot rouvre la question : la déclaration porte
                  // sur « ce lot », pas sur l'écran.
                  setIsCertified(false);
                }}
              />
            )}

            {preview.targetUnitId && (
              <Pressable
                accessibilityRole="checkbox"
                accessibilityState={{ checked: isChoosingOwnLot }}
                onPress={() => {
                  // Dans les deux sens on repart du lot proposé : cocher ouvre
                  // un sélecteur vide, décocher revient au lot du syndic.
                  setIsChoosingOwnLot((value) => !value);
                  setChosenUnitId(null);
                  setIsCertified(false);
                }}
                style={styles.certification}
              >
                <View style={[styles.checkbox, isChoosingOwnLot && styles.checkboxChecked]}>
                  {isChoosingOwnLot && <Text style={styles.checkboxMark}>✓</Text>}
                </View>
                <Text style={styles.certificationLabel}>Ce n'est pas votre lot ? Choisir un autre lot</Text>
              </Pressable>
            )}
          </View>
        )}

        {!isBoardSeat && unitId && (
          <Pressable
            accessibilityRole="checkbox"
            accessibilityState={{ checked: isCertified }}
            onPress={() => setIsCertified((value) => !value)}
            style={styles.certification}
          >
            <View style={[styles.checkbox, isCertified && styles.checkboxChecked]}>
              {isCertified && <Text style={styles.checkboxMark}>✓</Text>}
            </View>
            <Text style={styles.certificationLabel}>
              Je certifie être le propriétaire ou le mandataire pour ce lot.
            </Text>
          </Pressable>
        )}

        {canProceed && (
          <View style={styles.step}>
            <Text style={styles.stepLabel}>
              {isBoardSeat ? 'Connectez-vous ou créez un compte' : '2. Connectez-vous ou créez un compte'}
            </Text>
            {isAuthenticated ? (
              <>
                {autoConfirmStatus === 'pending' && <Loader label="Finalisation…" />}
                {autoConfirmStatus === 'error' && <Alert message={getErrorMessage(autoConfirmError)} />}
              </>
            ) : (
              <View style={styles.buttonGroup}>
                <Button onPress={handleCreateAccount}>Créer un compte</Button>
                <Button variant="secondary" onPress={handleExistingAccount}>
                  J'ai déjà un compte
                </Button>
              </View>
            )}
          </View>
        )}
      </Card>
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: 8,
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  address: {
    fontSize: 14,
    color: colors.gray[500],
    marginBottom: 8,
  },
  intro: {
    gap: 4,
    marginBottom: 8,
  },
  introText: {
    fontSize: 14,
    color: colors.gray[500],
  },
  designatedLot: {
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.brand[300],
    backgroundColor: colors.brand[50],
    padding: 12,
  },
  designatedLotLabel: {
    fontSize: 11,
    fontWeight: '600',
    letterSpacing: 0.5,
    color: colors.brand[600],
  },
  designatedLotValue: {
    marginTop: 2,
    fontSize: 17,
    fontWeight: '600',
    color: colors.gray[900],
  },
  certification: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 8,
    paddingVertical: 8,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderRadius: 4,
    borderWidth: 1,
    borderColor: colors.gray[300],
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkboxChecked: {
    backgroundColor: colors.brand[500],
    borderColor: colors.brand[500],
  },
  checkboxMark: {
    fontSize: 13,
    lineHeight: 16,
    color: colors.white,
  },
  certificationLabel: {
    flex: 1,
    fontSize: 14,
    color: colors.gray[800],
  },
  step: {
    gap: 8,
  },
  stepLabel: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[700],
  },
  buttonGroup: {
    gap: 8,
  },
  linkButton: {
    marginTop: 8,
  },
});
