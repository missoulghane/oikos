import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useMyPayments } from '@/features/property-ownership/payments/hooks/useMyPayments';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { PAYMENT_MODE_LABELS } from '@/features/property-ownership/payments/constants/paymentModeLabels';
import { Card } from '@/shared/components/Card/Card';
import { Badge } from '@/shared/components/Badge/Badge';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { HomeStackParamList } from '@/app/navigation/HomeStackNavigator';

type Props = NativeStackScreenProps<HomeStackParamList, 'MyPaymentDetail'>;

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.detailRow}>
      <Text style={styles.detailLabel}>{label}</Text>
      <Text style={styles.detailValue}>{value}</Text>
    </View>
  );
}

/**
 * Resolved from the owner's payment list rather than fetched by id: there is no
 * GET /payments/{id} endpoint, and the payment's journalEntryId is unusable here
 * (reading a journal entry needs ACCOUNTING_READ, which a plain owner lacks).
 */
export function MyPaymentDetailScreen({ route }: Props) {
  const payments = useMyPayments();
  const units = useMyUnits();

  if (payments.isLoading || units.isLoading) {
    return (
      <SafeAreaView style={styles.container}>
        <Loader label="Chargement du paiement…" />
      </SafeAreaView>
    );
  }

  if (payments.isError) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.content}>
          <Alert message={getErrorMessage(payments.error)} />
        </View>
      </SafeAreaView>
    );
  }

  const payment = (payments.data ?? []).find((candidate) => candidate.id === route.params.paymentId);

  if (!payment) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.content}>
          <Alert message="Ce paiement est introuvable." />
        </View>
      </SafeAreaView>
    );
  }

  const unit = (units.data ?? []).find((candidate) => candidate.unitId === payment.unitId);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.header}>
          <Text style={styles.title}>
            Paiement du {new Date(payment.valueDate).toLocaleDateString('fr-FR')}
          </Text>
          <Badge color="light">{PAYMENT_MODE_LABELS[payment.mode]}</Badge>
        </View>

        <Card style={styles.card}>
          <DetailRow label="Montant" value={`${payment.amount.toLocaleString('fr-FR')} MAD`} />
          <DetailRow label="Mode de paiement" value={PAYMENT_MODE_LABELS[payment.mode]} />
          <DetailRow label="Date de valeur" value={new Date(payment.valueDate).toLocaleDateString('fr-FR')} />
          <DetailRow label="Lot" value={unit ? formatUnitLabel(unit) : '—'} />
          <DetailRow label="Référence" value={payment.id} />
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
  header: {
    gap: 8,
    alignItems: 'flex-start',
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.gray[900],
  },
  card: {
    gap: 12,
  },
  detailRow: {
    gap: 2,
  },
  detailLabel: {
    fontSize: 13,
    color: colors.gray[500],
  },
  detailValue: {
    fontSize: 15,
    color: colors.gray[900],
  },
});
