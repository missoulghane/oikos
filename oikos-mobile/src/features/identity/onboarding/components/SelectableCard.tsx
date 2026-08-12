import type { ReactNode } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors } from '@/shared/theme/colors';

interface SelectableCardProps {
  value: string;
  checked: boolean;
  onSelect: (value: string) => void;
  title: string;
  description: ReactNode;
  /** 'radio' = un seul choix possible, 'checkbox' = plusieurs. */
  type?: 'radio' | 'checkbox';
}

/** Whole card is tappable, not just a small control - mirrors the web version's <label>. */
export function SelectableCard({ value, checked, onSelect, title, description, type = 'radio' }: SelectableCardProps) {
  return (
    <Pressable
      accessibilityRole={type === 'radio' ? 'radio' : 'checkbox'}
      accessibilityState={{ checked }}
      onPress={() => onSelect(value)}
      style={[styles.card, checked && styles.cardChecked]}
    >
      <View style={[styles.indicator, checked && styles.indicatorChecked]}>
        {checked && <View style={styles.indicatorDot} />}
      </View>
      <View style={styles.textContainer}>
        <Text style={styles.title}>{title}</Text>
        {typeof description === 'string' ? <Text style={styles.description}>{description}</Text> : description}
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    gap: 12,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.gray[200],
    padding: 16,
  },
  cardChecked: {
    borderColor: colors.brand[500],
    backgroundColor: colors.brand[50],
  },
  indicator: {
    marginTop: 2,
    width: 20,
    height: 20,
    borderRadius: 10,
    borderWidth: 2,
    borderColor: colors.gray[300],
    alignItems: 'center',
    justifyContent: 'center',
  },
  indicatorChecked: {
    borderColor: colors.brand[500],
  },
  indicatorDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: colors.brand[500],
  },
  textContainer: {
    flex: 1,
    gap: 2,
  },
  title: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[900],
  },
  description: {
    fontSize: 14,
    color: colors.gray[600],
  },
});
