import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { MyUnitsScreen, MyUnitDetailScreen } from '@/features/property-ownership/units';
import { MyInstallmentsScreen, MyInstallmentDetailScreen } from '@/features/property-ownership/installments';
import { MyPaymentsScreen, MyPaymentDetailScreen } from '@/features/property-ownership/payments';
import { MyMembershipRequestsScreen } from '@/features/property-ownership/membership-requests';
import {
  MyGeneralMeetingsScreen,
  MyGeneralMeetingDetailScreen,
} from '@/features/property-ownership/general-meetings';
import { NotificationBell } from '@/shared/components/NotificationBell/NotificationBell';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

/**
 * The owner's whole space, rooted on Accueil. Accueil *is* the lot list (it
 * absorbed what used to be a separate "Mes lots" tab), which is why every
 * lot/echeance/payment screen hangs off this stack rather than one of its own.
 */
export type HomeStackParamList = {
  Home: undefined;
  // Passing the whole unit rather than just its id: the list already has it in
  // memory (from useMyUnits), and there is no getUnit() ported on mobile.
  MyUnitDetail: { unit: OwnedUnit };
  // Optional seed for the filters, used by a lot's balance badge to open the
  // list already narrowed to that lot's unpaid echeances.
  MyInstallments: { status?: 'DUE'; unitId?: string } | undefined;
  MyInstallmentDetail: { installmentId: string };
  MyPayments: undefined;
  MyPaymentDetail: { paymentId: string };
  MyMembershipRequests: undefined;
  MyGeneralMeetings: undefined;
  // title is carried along so the header reads right before the meeting has
  // loaded - the list already has it, and there is no placeholder worth showing.
  MyGeneralMeetingDetail: { meetingId: string; title?: string };
};

const Stack = createNativeStackNavigator<HomeStackParamList>();

export function HomeStackNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: true }}>
      <Stack.Screen
        name="Home"
        component={MyUnitsScreen}
        options={{ title: 'Accueil', headerRight: () => <NotificationBell /> }}
      />
      <Stack.Screen
        name="MyUnitDetail"
        component={MyUnitDetailScreen}
        options={({ route }) => ({ title: `Lot ${route.params.unit.unitNumber}` })}
      />
      <Stack.Screen name="MyInstallments" component={MyInstallmentsScreen} options={{ title: 'Mes échéances' }} />
      <Stack.Screen
        name="MyInstallmentDetail"
        component={MyInstallmentDetailScreen}
        options={{ title: 'Détail de l’échéance' }}
      />
      <Stack.Screen name="MyPayments" component={MyPaymentsScreen} options={{ title: 'Mes paiements' }} />
      <Stack.Screen
        name="MyPaymentDetail"
        component={MyPaymentDetailScreen}
        options={{ title: 'Détail du paiement' }}
      />
      <Stack.Screen name="MyGeneralMeetings" component={MyGeneralMeetingsScreen} options={{ title: 'Mes assemblées' }} />
      <Stack.Screen
        name="MyGeneralMeetingDetail"
        component={MyGeneralMeetingDetailScreen}
        options={({ route }) => ({ title: route.params.title ?? 'Assemblée générale' })}
      />
      <Stack.Screen
        name="MyMembershipRequests"
        component={MyMembershipRequestsScreen}
        options={{ title: 'Mes invitations' }}
      />
    </Stack.Navigator>
  );
}
