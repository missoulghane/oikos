import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { colors } from '@/shared/theme/colors';

interface StepperProps {
  label: string;
  value: number;
  onChange: (value: number) => void;
  min?: number;
  max?: number;
  /** Omits the visible label (rows where the name already sits alongside it). */
  hideLabel?: boolean;
}

/**
 * −/+ counter with direct keyboard entry too. Both buttons disable at the
 * bounds rather than silently clamping a value the user typed past them, so
 * it's visible why they can't go further.
 */
export function Stepper({ label, value, onChange, min = 0, max = 999, hideLabel = false }: StepperProps) {
  function clamp(next: number): number {
    return Math.min(max, Math.max(min, next));
  }

  return (
    <View style={styles.row} accessibilityLabel={hideLabel ? label : undefined}>
      {!hideLabel && <Text style={styles.label}>{label}</Text>}
      <View style={styles.controls}>
        <Pressable
          accessibilityRole="button"
          accessibilityLabel={`Diminuer : ${label}`}
          disabled={value <= min}
          onPress={() => onChange(clamp(value - 1))}
          style={[styles.button, value <= min && styles.buttonDisabled]}
        >
          <Text style={styles.buttonText}>−</Text>
        </Pressable>
        <TextInput
          accessibilityLabel={label}
          keyboardType="number-pad"
          value={String(value)}
          onChangeText={(text) => {
            const parsed = Number.parseInt(text, 10);
            onChange(Number.isNaN(parsed) ? min : clamp(parsed));
          }}
          style={styles.input}
        />
        <Pressable
          accessibilityRole="button"
          accessibilityLabel={`Augmenter : ${label}`}
          disabled={value >= max}
          onPress={() => onChange(clamp(value + 1))}
          style={[styles.button, value >= max && styles.buttonDisabled]}
        >
          <Text style={styles.buttonText}>+</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  label: {
    fontSize: 14,
    color: colors.gray[700],
  },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  button: {
    width: 44,
    height: 44,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.gray[300],
  },
  buttonDisabled: {
    opacity: 0.4,
  },
  buttonText: {
    fontSize: 18,
    fontWeight: '500',
    color: colors.gray[700],
  },
  input: {
    minHeight: 44,
    width: 64,
    textAlign: 'center',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.gray[300],
    fontSize: 16,
    color: colors.gray[800],
  },
});
