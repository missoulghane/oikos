import { StyleSheet, Text, View } from 'react-native';
import { colors } from '@/shared/theme/colors';

type AlertVariant = 'error' | 'success' | 'warning';

const VARIANT_STYLES: Record<AlertVariant, { background: string; border: string; text: string; icon: string }> = {
  error: { background: colors.error[50], border: colors.error[500], text: colors.error[500], icon: '⚠' },
  success: { background: colors.success[50], border: colors.success[500], text: colors.success[500], icon: '✓' },
  warning: { background: colors.warning[50], border: colors.warning[500], text: colors.warning[500], icon: '!' },
};

export function Alert({ message, variant = 'error' }: { message: string; variant?: AlertVariant }) {
  const variantStyle = VARIANT_STYLES[variant];

  return (
    <View
      accessibilityRole="alert"
      style={[styles.container, { backgroundColor: variantStyle.background, borderColor: variantStyle.border }]}
    >
      <Text style={[styles.icon, { color: variantStyle.text }]}>{variantStyle.icon}</Text>
      <Text style={[styles.message, { color: variantStyle.text }]}>{message}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 8,
    borderWidth: 1,
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  icon: {
    fontSize: 14,
    lineHeight: 20,
  },
  message: {
    flex: 1,
    fontSize: 14,
    lineHeight: 20,
  },
});
