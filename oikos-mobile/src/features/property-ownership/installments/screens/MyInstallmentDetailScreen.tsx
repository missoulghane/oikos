import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useInstallment } from '@/features/property-ownership/installments/hooks/useInstallment';
import { useMyUnits } from '@/features/property-ownership/units/hooks/useMyUnits';
import { formatUnitLabel } from '@/features/property-ownership/units/utils/formatUnitLabel';
import { formatPeriod } from '@/features/property-ownership/installments/utils/formatPeriod';
import {
  INSTALLMENT_STATUS_BADGE_COLORS,
  INSTALLMENT_STATUS_LABELS,
} from '@/features/property-ownership/installments/constants/installmentStatusLabels';
import { Card } from '@/shared/components/Card/Card';
import { Badge } from '@/shared/components/Badge/Badge';
import { Loader } from '@/shared/components/Loader/Loader';
import { Alert } from '@/shared/components/Alert/Alert';
import { getErrorMessage } from '@/shared/utils/getErrorMessage';
import { colors } from '@/shared/theme/colors';
import type { HomeStackParamList } from '@/app/navigation/HomeStackNavigator';

type Props = NativeStackScreenProps<HomeStackParamList, 'MyInstallmentDetail'>;

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.detailRow}>
      <Text style={styles.detailLabel}>{label}</Text>
      <Text style={styles.detailValue}>{value}</Text>
    </View>
  );
}

export function MyInstallmentDetailScreen({ route }: Props) {
  const installment = useInstallment(route.params.installmentId);
  const units = useMyUnits();

  if (installment.isLoading) {
    return (
      <SafeAreaView style={styles.container}>
        <Loader label="Chargement de l'échéance…" />
      </SafeAreaView>
    );
  }

  if (installment.isError) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.content}>
          <Alert message={getErrorMessage(installment.error)} />
        </View>
      </SafeAreaView>
    );
  }

  if (!installment.data) {
    return null;
  }

  const unit = (units.data ?? []).find((candidate) => candidate.unitId === installment.data.unitId);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.header}>
          <Text style={styles.title}>
            Échéance du {new Date(installment.data.dueDate).toLocaleDateString('fr-FR')}
          </Text>
          <Badge color={INSTALLMENT_STATUS_BADGE_COLORS[installment.data.status]}>
            {INSTALLMENT_STATUS_LABELS[installment.data.status]}
          </Badge>
        </View>

        <Card style={styles.card}>
          <DetailRow label="Montant" value={`${installment.data.amount.toLocaleString('fr-FR')} MAD`} />
          <DetailRow
            label="Reste à payer"
            value={`${installment.data.outstandingAmount.toLocaleString('fr-FR')} MAD`}
          />
          <DetailRow label="Lot" value={unit ? formatUnitLabel(unit) : '—'} />
          {installment.data.period && (
            <DetailRow label="Appel de fonds" value={formatPeriod(installment.data.period)} />
          )}
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
