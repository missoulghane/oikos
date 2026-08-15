import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Card } from '@/shared/components/Card/Card';
import { getOutstandingColor } from '@/features/property-ownership/units/utils/unitBalance';
import { colors } from '@/shared/theme/colors';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

interface MyUnitCardProps {
  unit: OwnedUnit;
  onPress: () => void;
  /** What this lot still owes; undefined while the echeances are still loading. */
  outstanding?: number;
}

export function MyUnitCard({ unit, onPress, outstanding }: MyUnitCardProps) {
  return (
    <Pressable onPress={onPress}>
      <Card style={styles.card}>
        <View style={styles.headerRow}>
          <Text style={styles.propertyName}>{unit.propertyName}</Text>
          {outstanding !== undefined && (
            <Text style={[styles.balance, { color: getOutstandingColor(outstanding) }]}>
              {/* Signed like an account statement, as on oikos-web. */}
              {outstanding > 0 ? `Solde : -${outstanding.toLocaleString('fr-FR')} MAD` : 'À jour'}
            </Text>
          )}
        </View>
        <Text style={styles.detail}>
          {unit.buildingName} — Lot {unit.unitNumber}
        </Text>
      </Card>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: 2,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    gap: 12,
  },
  propertyName: {
    flex: 1,
    fontSize: 15,
    fontWeight: '500',
    color: colors.gray[900],
  },
  balance: {
    fontSize: 14,
    fontWeight: '600',
  },
  detail: {
    fontSize: 14,
    color: colors.gray[500],
  },
});
