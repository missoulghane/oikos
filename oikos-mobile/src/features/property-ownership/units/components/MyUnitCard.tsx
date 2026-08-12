import { Pressable, StyleSheet, Text } from 'react-native';
import { Card } from '@/shared/components/Card/Card';
import { colors } from '@/shared/theme/colors';
import type { OwnedUnit } from '@/features/property-ownership/units/types/unit.types';

export function MyUnitCard({ unit, onPress }: { unit: OwnedUnit; onPress: () => void }) {
  return (
    <Pressable onPress={onPress}>
      <Card style={styles.card}>
        <Text style={styles.propertyName}>{unit.propertyName}</Text>
        <Text style={styles.detail}>
          {unit.buildingName} — Lot {unit.unitNumber}
        </Text>
        <Text style={styles.detail}>{unit.ownershipShare}% des tantièmes</Text>
      </Card>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: 2,
  },
  propertyName: {
    fontSize: 15,
    fontWeight: '500',
    color: colors.gray[900],
  },
  detail: {
    fontSize: 14,
    color: colors.gray[500],
  },
});
