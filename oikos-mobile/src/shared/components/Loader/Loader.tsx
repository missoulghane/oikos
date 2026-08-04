import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { colors } from '@/shared/theme/colors';

export function Loader({ label = 'Chargement…' }: { label?: string }) {
  return (
    <View accessibilityRole="progressbar" style={styles.container}>
      <ActivityIndicator color={colors.brand[500]} />
      <Text style={styles.label}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    paddingVertical: 32,
  },
  label: {
    fontSize: 14,
    color: colors.gray[500],
  },
});
