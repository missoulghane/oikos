import type { NavigatorScreenParams } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { LoginScreen, ForgotPasswordScreen, ResetPasswordScreen } from '@/features/identity/auth';
import {
  RegisterUserScreen,
  VerifyEmailScreen,
  ActivateAccountScreen,
  AcceptInvitationScreen,
  RegisterPropertyManagerAdminScreen,
} from '@/features/identity/register';
import { InvitationLandingScreen } from '@/features/identity/invitations';
import { OnboardingNavigator, type OnboardingStackParamList } from '@/app/navigation/OnboardingNavigator';

export type AuthStackParamList = {
  Login: undefined;
  RegisterUser: { invitationToken?: string; unitId?: string };
  VerifyEmail: { token?: string };
  ActivateAccount: { token?: string };
  AcceptInvitation: { token?: string };
  InvitationLanding: { token?: string; unitId?: string };
  RegisterPropertyManagerAdmin: undefined;
  Onboarding: NavigatorScreenParams<OnboardingStackParamList> | undefined;
  ForgotPassword: undefined;
  ResetPassword: { token?: string };
};

const Stack = createNativeStackNavigator<AuthStackParamList>();

export function AuthNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="RegisterUser" component={RegisterUserScreen} />
      <Stack.Screen name="VerifyEmail" component={VerifyEmailScreen} />
      <Stack.Screen name="ActivateAccount" component={ActivateAccountScreen} />
      <Stack.Screen name="AcceptInvitation" component={AcceptInvitationScreen} />
      <Stack.Screen name="InvitationLanding" component={InvitationLandingScreen} />
      <Stack.Screen name="RegisterPropertyManagerAdmin" component={RegisterPropertyManagerAdminScreen} />
      <Stack.Screen name="Onboarding" component={OnboardingNavigator} />
      <Stack.Screen name="ForgotPassword" component={ForgotPasswordScreen} />
      <Stack.Screen name="ResetPassword" component={ResetPasswordScreen} />
    </Stack.Navigator>
  );
}
