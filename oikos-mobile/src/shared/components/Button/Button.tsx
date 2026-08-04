import { ActivityIndicator, Pressable, StyleSheet, Text, type PressableProps, type ViewStyle } from 'react-native';
import { colors } from '@/shared/theme/colors';

type ButtonVariant = 'primary' | 'secondary';

interface ButtonProps extends Omit<PressableProps, 'style'> {
  variant?: ButtonVariant;
  isLoading?: boolean;
  children: string;
  style?: ViewStyle;
}

export function Button({ variant = 'primary', isLoading = false, disabled, children, style, ...rest }: ButtonProps) {
  const isDisabled = disabled ?? isLoading;

  return (
    <Pressable
      accessibilityRole="button"
      disabled={isDisabled}
      style={[
        styles.base,
        variant === 'primary' ? styles.primary : styles.secondary,
        isDisabled && styles.disabled,
        style,
      ]}
      {...rest}
    >
      {isLoading ? (
        <ActivityIndicator color={variant === 'primary' ? colors.white : colors.gray[700]} />
      ) : (
        <Text style={variant === 'primary' ? styles.primaryText : styles.secondaryText}>{children}</Text>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: {
    minHeight: 44,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
  primary: {
    backgroundColor: colors.brand[500],
  },
  secondary: {
    backgroundColor: colors.white,
    borderWidth: 1,
    borderColor: colors.gray[300],
  },
  disabled: {
    opacity: 0.5,
  },
  primaryText: {
    color: colors.white,
    fontSize: 14,
    fontWeight: '600',
  },
  secondaryText: {
    color: colors.gray[700],
    fontSize: 14,
    fontWeight: '600',
  },
});
