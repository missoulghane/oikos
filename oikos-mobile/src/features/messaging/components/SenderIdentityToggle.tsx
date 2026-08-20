import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors } from '@/shared/theme/colors';
import type { SenderIdentity } from '@/features/messaging/types/messaging.types';

const OPTIONS: { value: SenderIdentity; label: string }[] = [
  { value: 'OWNER', label: 'Copropriétaire' },
  { value: 'BOARD', label: 'Membre du conseil' },
];

interface SenderIdentityToggleProps {
  value: SenderIdentity | undefined;
  onChange: (value: SenderIdentity) => void;
  disabled?: boolean;
}

export function SenderIdentityToggle({ value, onChange, disabled = false }: SenderIdentityToggleProps) {
  return (
    <View style={styles.group}>
      {OPTIONS.map((option) => {
        const isSelected = value === option.value;
        return (
          <Pressable
            key={option.value}
            disabled={disabled}
            onPress={() => onChange(option.value)}
            style={[styles.option, isSelected && styles.optionSelected]}
          >
            <Text style={[styles.optionText, isSelected && styles.optionTextSelected]}>{option.label}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  group: {
    flexDirection: 'row',
    alignSelf: 'flex-start',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.gray[200],
    padding: 2,
  },
  option: {
    borderRadius: 6,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  optionSelected: {
    backgroundColor: colors.brand[500],
  },
  optionText: {
    fontSize: 13,
    fontWeight: '500',
    color: colors.gray[600],
  },
  optionTextSelected: {
    color: colors.white,
  },
});
