import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { MyUnitsScreen, MyUnitDetailScreen } from '@/features/property-ownership/units';
import { MyInstallmentsScreen } from '@/features/property-ownership/installments';
import { MyPaymentsScreen } from '@/features/property-ownership/payments';
import { MyMembershipRequestsScreen } from '@/features/property-ownership/membership-requests';
import { NotificationBell } from '@/shared/components/NotificationBell/NotificationBell';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

export type UnitsStackParamList = {
  MyUnits: undefined;
  // Passing the whole unit rather than just its id: MyUnitsScreen already has
  // it in memory (from useMyUnits), and oikos-web's separate getUnit() call
  // isn't ported here - see MyUnitDetailScreen's module doc for why.
  MyUnitDetail: { unit: OwnedUnit };
  MyInstallments: undefined;
  MyPayments: undefined;
  MyMembershipRequests: undefined;
};

const Stack = createNativeStackNavigator<UnitsStackParamList>();

export function UnitsStackNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: true }}>
      <Stack.Screen name="MyUnits" component={MyUnitsScreen} options={{ title: 'Mes lots', headerRight: () => <NotificationBell /> }} />
      <Stack.Screen
        name="MyUnitDetail"
        component={MyUnitDetailScreen}
        options={({ route }) => ({ title: `Lot ${route.params.unit.unitNumber}` })}
      />
      <Stack.Screen name="MyInstallments" component={MyInstallmentsScreen} options={{ title: 'Mes échéances' }} />
      <Stack.Screen name="MyPayments" component={MyPaymentsScreen} options={{ title: 'Mes paiements' }} />
      <Stack.Screen name="MyMembershipRequests" component={MyMembershipRequestsScreen} options={{ title: 'Mes invitations' }} />
    </Stack.Navigator>
  );
}
