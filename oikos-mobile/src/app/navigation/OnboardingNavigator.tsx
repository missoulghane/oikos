import { createNativeStackNavigator } from '@react-navigation/native-stack';
import {
  OnboardingProvider,
  AccountStepScreen,
  PropertyStepScreen,
  DuesModeStepScreen,
  UnitTypesStepScreen,
  BuildingsStepScreen,
  BankAccountsStepScreen,
  SummaryStepScreen,
  OnboardingDoneScreen,
} from '@/features/identity/onboarding';

export type OnboardingStackParamList = {
  Account: undefined;
  Property: undefined;
  DuesMode: undefined;
  UnitTypes: undefined;
  Buildings: undefined;
  BankAccounts: undefined;
  Summary: undefined;
  Done: undefined;
};

const Stack = createNativeStackNavigator<OnboardingStackParamList>();

/**
 * Board-admin (volunteer syndic) sign-up wizard. The provider wraps every
 * step: the draft must survive navigating from one step to the next, exactly
 * like oikos-web's OnboardingProvider wrapping its own nested <Routes>.
 * Registered as a single nested navigator inside AuthNavigator (see
 * AuthNavigator.tsx) rather than flattening these 8 screens into
 * AuthStackParamList directly.
 */
export function OnboardingNavigator() {
  return (
    <OnboardingProvider>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        <Stack.Screen name="Account" component={AccountStepScreen} />
        <Stack.Screen name="Property" component={PropertyStepScreen} />
        <Stack.Screen name="DuesMode" component={DuesModeStepScreen} />
        <Stack.Screen name="UnitTypes" component={UnitTypesStepScreen} />
        <Stack.Screen name="Buildings" component={BuildingsStepScreen} />
        <Stack.Screen name="BankAccounts" component={BankAccountsStepScreen} />
        <Stack.Screen name="Summary" component={SummaryStepScreen} />
        <Stack.Screen name="Done" component={OnboardingDoneScreen} />
      </Stack.Navigator>
    </OnboardingProvider>
  );
}
