import { forwardRef } from 'react';
import { StyleSheet, Text, TextInput, View, type TextInputProps } from 'react-native';
import { colors } from '@/shared/theme/colors';

interface InputProps extends TextInputProps {
  label: string;
  errorMessage?: string;
}

export const Input = forwardRef<TextInput, InputProps>(({ label, errorMessage, style, ...rest }, ref) => {
  return (
    <View style={styles.container}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        ref={ref}
        placeholderTextColor={colors.gray[400]}
        style={[styles.input, Boolean(errorMessage) && styles.inputError, style]}
        {...rest}
      />
      {errorMessage && <Text style={styles.error}>{errorMessage}</Text>}
    </View>
  );
});

Input.displayName = 'Input';

const styles = StyleSheet.create({
  container: {
    gap: 4,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[700],
  },
  input: {
    minHeight: 44,
    borderWidth: 1,
    borderColor: colors.gray[300],
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 16,
    color: colors.gray[800],
  },
  inputError: {
    borderColor: colors.error[500],
  },
  error: {
    fontSize: 14,
    color: colors.error[500],
  },
});
