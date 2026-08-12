import type { PropsWithChildren } from 'react';
import { StyleSheet, Text, View, type ViewStyle } from 'react-native';
import { colors } from '@/shared/theme/colors';

type BadgeColor = 'primary' | 'success' | 'error' | 'warning' | 'info' | 'light' | 'dark';
type BadgeVariant = 'light' | 'solid';

interface BadgeProps {
  color?: BadgeColor;
  variant?: BadgeVariant;
  style?: ViewStyle;
}

const VARIANT_STYLES: Record<BadgeVariant, Record<BadgeColor, { background: string; text: string }>> = {
  light: {
    primary: { background: colors.brand[50], text: colors.brand[500] },
    success: { background: colors.success[50], text: colors.success[500] },
    error: { background: colors.error[50], text: colors.error[500] },
    warning: { background: colors.warning[50], text: colors.warning[500] },
    info: { background: colors.brand[50], text: colors.brand[500] },
    light: { background: colors.gray[100], text: colors.gray[700] },
    dark: { background: colors.gray[500], text: colors.white },
  },
  solid: {
    primary: { background: colors.brand[500], text: colors.white },
    success: { background: colors.success[500], text: colors.white },
    error: { background: colors.error[500], text: colors.white },
    warning: { background: colors.warning[500], text: colors.white },
    info: { background: colors.brand[500], text: colors.white },
    light: { background: colors.gray[400], text: colors.white },
    dark: { background: colors.gray[700], text: colors.white },
  },
};

export function Badge({ color = 'primary', variant = 'light', style, children }: PropsWithChildren<BadgeProps>) {
  const colorStyle = VARIANT_STYLES[variant][color];

  return (
    <View style={[styles.badge, { backgroundColor: colorStyle.background }, style]}>
      <Text style={[styles.text, { color: colorStyle.text }]}>{children}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    alignSelf: 'flex-start',
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 3,
  },
  text: {
    fontSize: 12,
    fontWeight: '500',
  },
});
