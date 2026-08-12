import { useState } from 'react';
import { FlatList, Modal, Pressable, StyleSheet, Text, View } from 'react-native';
import { colors } from '@/shared/theme/colors';

export interface SelectOption {
  label: string;
  value: string;
}

interface SelectProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: SelectOption[];
  placeholder?: string;
  errorMessage?: string;
}

/**
 * React Native has no native equivalent to a web <select> - this opens a
 * modal list instead. The picker component decided in PLAN.md Phase 0 (no
 * external dependency; reused wherever a web `<select>` needs porting).
 */
export function Select({ label, value, onChange, options, placeholder = 'Sélectionner…', errorMessage }: SelectProps) {
  const [isOpen, setIsOpen] = useState(false);
  const selectedLabel = options.find((option) => option.value === value)?.label;

  return (
    <View style={styles.container}>
      <Text style={styles.label}>{label}</Text>
      <Pressable
        accessibilityRole="button"
        onPress={() => setIsOpen(true)}
        style={[styles.trigger, Boolean(errorMessage) && styles.triggerError]}
      >
        <Text style={selectedLabel ? styles.triggerText : styles.triggerPlaceholder}>
          {selectedLabel ?? placeholder}
        </Text>
      </Pressable>
      {errorMessage && <Text style={styles.error}>{errorMessage}</Text>}

      <Modal visible={isOpen} transparent animationType="fade" onRequestClose={() => setIsOpen(false)}>
        <Pressable style={styles.backdrop} onPress={() => setIsOpen(false)}>
          <Pressable style={styles.sheet} onPress={(event) => event.stopPropagation()}>
            <Text style={styles.sheetTitle}>{label}</Text>
            <FlatList
              data={options}
              keyExtractor={(option) => option.value}
              style={styles.list}
              renderItem={({ item }) => (
                <Pressable
                  style={styles.option}
                  onPress={() => {
                    onChange(item.value);
                    setIsOpen(false);
                  }}
                >
                  <Text style={item.value === value ? styles.optionTextSelected : styles.optionText}>
                    {item.label}
                  </Text>
                </Pressable>
              )}
              ListEmptyComponent={<Text style={styles.emptyText}>Aucune option disponible</Text>}
            />
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 4,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[700],
  },
  trigger: {
    minHeight: 44,
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: colors.gray[300],
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  triggerError: {
    borderColor: colors.error[500],
  },
  triggerText: {
    fontSize: 16,
    color: colors.gray[800],
  },
  triggerPlaceholder: {
    fontSize: 16,
    color: colors.gray[400],
  },
  error: {
    fontSize: 14,
    color: colors.error[500],
  },
  backdrop: {
    flex: 1,
    backgroundColor: 'rgba(16, 24, 40, 0.4)',
    justifyContent: 'flex-end',
  },
  sheet: {
    maxHeight: '60%',
    backgroundColor: colors.white,
    borderTopLeftRadius: 16,
    borderTopRightRadius: 16,
    padding: 16,
    gap: 8,
  },
  sheetTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.gray[900],
  },
  list: {
    flexGrow: 0,
  },
  option: {
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.gray[100],
  },
  optionText: {
    fontSize: 16,
    color: colors.gray[800],
  },
  optionTextSelected: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.brand[500],
  },
  emptyText: {
    paddingVertical: 16,
    textAlign: 'center',
    fontSize: 14,
    color: colors.gray[500],
  },
});
