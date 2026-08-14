import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Card } from '@/shared/components/Card/Card';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { Badge } from '@/shared/components/Badge/Badge';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import { useUnitOwners } from '@/features/property-ownership/units/hooks/useUnitOwners';
import { useMyInstallments } from '@/features/property-ownership/installments/hooks/useMyInstallments';
import { useMyPayments } from '@/features/property-ownership/payments/hooks/useMyPayments';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-ownership/installments/constants/installmentStatusLabels';
import { PAYMENT_MODE_LABELS } from '@/features/property-ownership/payments/constants/paymentModeLabels';
import { PARTY_TYPE_LABELS } from '@/features/property-ownership/units/types/unitOwnership.types';
import type { UnitsStackParamList } from '@/app/navigation/UnitsStackNavigator';

type Props = NativeStackScreenProps<UnitsStackParamList, 'MyUnitDetail'>;

/**
 * oikos-web's MyUnitDetailPage reuses property-mngt's UnitOwnersSection /
 * UnitInstallmentsSection / UnitPaymentsSection (canManage={false}) plus a
 * dedicated getUnit() call. property-mngt doesn't exist on mobile (out of
 * scope, see PLAN.md), and it isn't needed here: the unit itself is already
 * fully known from the list this screen was opened from (passed as a route
 * param, no extra fetch), and échéances/paiements are obtained by filtering
 * the already-ported useMyInstallments()/useMyPayments() by unitId - the
 * only genuinely new dependency is the co-owners list (GET /units/{id}/owners,
 * see useUnitOwners), ported directly since it's small and self-contained.
 */
export function MyUnitDetailScreen({ route }: Props) {
  const { unit } = route.params;
  const owners = useUnitOwners(unit.unitId);
  const installments = useMyInstallments();
  const payments = useMyPayments();

  const unitInstallments = (installments.data ?? [])
    .filter((installment) => installment.unitId === unit.unitId)
    .sort((a, b) => new Date(b.dueDate).getTime() - new Date(a.dueDate).getTime());
  const unitPayments = (payments.data ?? [])
    .filter((payment) => payment.unitId === unit.unitId)
    .sort((a, b) => new Date(b.valueDate).getTime() - new Date(a.valueDate).getTime());

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <View>
          <Text style={styles.title}>Lot {unit.unitNumber}</Text>
          <Text style={styles.subtitle}>
            {unit.propertyName} — {unit.buildingName}
          </Text>
          <Text style={styles.subtitle}>{unit.ownershipShare}% des tantièmes</Text>
        </View>

        <Card style={styles.section}>
          <Text style={styles.sectionTitle}>Propriétaires</Text>
          {owners.isLoading && <Loader label="Chargement des propriétaires…" />}
          {owners.isError && <Alert message={getErrorMessage(owners.error)} />}
          {owners.data && owners.data.length === 0 && <Text style={styles.muted}>Aucun propriétaire pour le moment.</Text>}
          {owners.data?.map((owner) => (
            <View key={owner.id} style={styles.listRow}>
              <Text style={styles.listRowText}>
                {owner.partyFullName} ({PARTY_TYPE_LABELS[owner.partyType]}) — {owner.partyEmail}
              </Text>
              <Text style={styles.muted}>{owner.ownershipShare}%</Text>
            </View>
          ))}
        </Card>

        <Card style={styles.section}>
          <Text style={styles.sectionTitle}>Échéances</Text>
          {installments.isLoading && <Loader label="Chargement des échéances…" />}
          {installments.isError && <Alert message={getErrorMessage(installments.error)} />}
          {installments.isSuccess && unitInstallments.length === 0 && <EmptyState title="Aucune échéance pour le moment" />}
          {unitInstallments.map((installment) => (
            <View key={installment.id} style={styles.listRow}>
              <Text style={styles.listRowText}>
                Échéance du {new Date(installment.dueDate).toLocaleDateString('fr-FR')} —{' '}
                {installment.amount.toLocaleString('fr-FR')} MAD
                {installment.status === 'PARTIALLY_SETTLED' &&
                  ` (reste ${installment.outstandingAmount.toLocaleString('fr-FR')} MAD)`}
              </Text>
              <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[installment.status]}>
                {INSTALLMENT_STATUS_LABELS[installment.status]}
              </Badge>
            </View>
          ))}
        </Card>

        <Card style={styles.section}>
          <Text style={styles.sectionTitle}>Paiements</Text>
          {payments.isLoading && <Loader label="Chargement des paiements…" />}
          {payments.isError && <Alert message={getErrorMessage(payments.error)} />}
          {payments.isSuccess && unitPayments.length === 0 && <EmptyState title="Aucun paiement pour le moment" />}
          {unitPayments.map((payment) => (
            <View key={payment.id} style={styles.listRow}>
              <Text style={styles.listRowText}>
                Paiement du {new Date(payment.valueDate).toLocaleDateString('fr-FR')} — {payment.amount.toLocaleString('fr-FR')}{' '}
                MAD
              </Text>
              <Badge color="light">{PAYMENT_MODE_LABELS[payment.mode]}</Badge>
            </View>
          ))}
        </Card>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.gray[50],
  },
  content: {
    padding: 16,
    gap: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  subtitle: {
    fontSize: 14,
    color: colors.gray[500],
  },
  section: {
    gap: 8,
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: colors.gray[900],
  },
  muted: {
    fontSize: 14,
    color: colors.gray[400],
  },
  listRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
    paddingVertical: 6,
    borderTopWidth: 1,
    borderTopColor: colors.gray[100],
  },
  listRowText: {
    flex: 1,
    fontSize: 14,
    color: colors.gray[700],
  },
});
