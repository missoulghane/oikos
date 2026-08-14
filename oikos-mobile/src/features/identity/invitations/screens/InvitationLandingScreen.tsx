import { useEffect, useRef, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
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
  const [unitId, setUnitId] = useState<string | null>(initialUnitId ?? null);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const setPendingInvitation = usePendingInvitationStore((state) => state.setPending);

  const { data: preview, isPending: isPreviewPending, isError: isPreviewError } = useInvitationPreview(token);
  const { data: availableUnits } = useInvitationAvailableUnits(token, Boolean(preview?.usable));

  const acceptMutation = useAcceptInvitation();
  const submitMutation = useSubmitMembershipRequest();
  const isPublic = preview?.type === 'PUBLIC';
  const mutation = isPublic ? submitMutation : acceptMutation;

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

  useEffect(() => {
    if (autoConfirmStatus === 'success') {
      // property-ownership screens don't exist on mobile yet (Phase 2) - land
      // on Home for now; only reachable while already authenticated (Main
      // stack), so this route name is always valid here.
      navigation.navigate('MainTabs', { screen: 'HomeTab', params: { screen: 'Home' } });
    }
  }, [autoConfirmStatus, navigation]);

  function handleCreateAccount() {
    if (!token || !unitId) {
      return;
    }
    if (isPublic) {
      // PUBLIC invitations are consumed by RegisterUserService as part of
      // account creation itself - nothing left to confirm once the new
      // account verifies its email and logs in.
      navigation.navigate('RegisterUser', { invitationToken: token, unitId });
    } else {
      setPendingInvitation({ token, unitId, type: 'PRIVATE' });
      navigation.navigate('RegisterUser', {});
    }
  }

  function handleExistingAccount() {
    if (!token || !unitId || !preview) {
      return;
    }
    setPendingInvitation({ token, unitId, type: preview.type });
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

        <View style={styles.step}>
          <Text style={styles.stepLabel}>1. Choisissez votre lot</Text>
          <UnitPicker units={availableUnits?.content ?? []} value={unitId} onChange={setUnitId} />
        </View>

        {unitId && (
          <View style={styles.step}>
            <Text style={styles.stepLabel}>2. Connectez-vous ou créez un compte</Text>
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
