import * as Linking from 'expo-linking';
import { NavigationContainer, type LinkingOptions } from '@react-navigation/native';
import { useAuthStore } from '@/app/store';
import { AuthNavigator, type AuthStackParamList } from '@/app/navigation/AuthNavigator';
import { MainNavigator, type MainStackParamList } from '@/app/navigation/MainNavigator';
import { Loader } from '@/shared/components/Loader/Loader';

/**
 * Email links (verify-email, activate-account, accept-invitation,
 * reset-password) all carry a `token` query param and are meant to be tapped
 * before/without being logged in, so they only need to resolve against
 * AuthNavigator's screens. Since AuthNavigator and MainNavigator are mounted
 * exclusively (see below), their route names never collide today and can
 * share one flat `screens` map - if a link is tapped while already
 * authenticated, MainNavigator is mounted instead and these paths simply
 * won't match anything yet (acceptable for this phase; revisit if that case
 * needs handling, e.g. an already-logged-in user re-tapping an invite link).
 */
const linking: LinkingOptions<AuthStackParamList & MainStackParamList> = {
  prefixes: [Linking.createURL('/'), 'oikos://'],
  config: {
    screens: {
      Login: 'login',
      RegisterUser: 'register',
      VerifyEmail: 'verify-email',
      ActivateAccount: 'activate-account',
      AcceptInvitation: 'accept-invitation',
      ForgotPassword: 'forgot-password',
      ResetPassword: 'reset-password',
      InvitationLanding: 'invitations',
      RegisterPropertyManagerAdmin: 'register/manager-admin',
      Onboarding: {
        path: 'register/board-admin',
        screens: {
          Account: 'account',
          Property: 'property',
          DuesMode: 'dues-mode',
          UnitTypes: 'unit-types',
          Buildings: 'buildings',
          BankAccounts: 'bank-accounts',
          Summary: 'summary',
          Done: 'done',
        },
      },
      // Nested to mirror MainNavigator -> MainTabNavigator -> per-tab stacks.
      // MyUnitDetail and Conversation are deliberately not linkable: they
      // take a whole OwnedUnit/ConversationSummary object as a param, not
      // just an id - there's no getUnit()/getConversation()-style
      // fetch-by-id on mobile to resolve a bare id from a URL.
      MainTabs: {
        screens: {
          HomeTab: {
            screens: {
              Home: 'home',
              MyInstallments: 'my-installments',
              // Linkable (unlike MyUnitDetail/Conversation) because they take a
              // bare id that the screen can resolve on its own.
              MyInstallmentDetail: 'my-installments/:installmentId',
              MyPayments: 'my-payments',
              MyPaymentDetail: 'my-payments/:paymentId',
              MyMembershipRequests: 'my-membership-requests',
            },
          },
          MessagingTab: {
            screens: {
              ConversationList: 'messages',
              NewConversation: 'messages/new',
              Drafts: 'messages/drafts',
            },
          },
          AccountTab: { screens: { Profile: 'profile' } },
        },
      },
      Notifications: 'notifications',
    },
  },
};

/**
 * Chooses between the Auth and Main stacks based on isAuthenticated - the
 * mobile equivalent of oikos-web's ProtectedRoute. It reacts to the store
 * synchronously, so clearSession() (whether from an explicit logout or a
 * failed token refresh in httpClient.ts) switches back to Auth on its own,
 * with no imperative navigation call needed from the call site.
 */
export function RootNavigator() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const hasHydrated = useAuthStore((state) => state.hasHydrated);

  if (!hasHydrated) {
    return <Loader label="Chargement de la session…" />;
  }

  return (
    <NavigationContainer linking={linking}>
      {isAuthenticated ? <MainNavigator /> : <AuthNavigator />}
    </NavigationContainer>
  );
}
