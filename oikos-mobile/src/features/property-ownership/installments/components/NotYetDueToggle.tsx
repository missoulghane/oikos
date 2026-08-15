import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors } from '@/shared/theme/colors';

interface NotYetDueToggleProps {
  checked: boolean;
  /** Rows currently hidden by this toggle - 0 when it is on. */
  hiddenCount: number;
  onChange: (checked: boolean) => void;
}

/**
 * Shows or hides the echeances not yet fallen due, which the list leaves out by
 * default. Sits in the filter panel as one more field, mirroring oikos-web.
 *
 * Two things earn their place because the control is unmounted with the rest of
 * the panel: the count of what is currently hidden, and the "Masquées /
 * Affichées" wording. A neutral label would leave someone hunting for an
 * echeance dated next month with nothing telling them where it went.
 *
 * The web version marks the off state with a dashed border; here it stays
 * solid, since Android falls back to a solid border as soon as a dashed one
 * meets a borderRadius. The palette carries the state instead.
 */
export function NotYetDueToggle({ checked, hiddenCount, onChange }: NotYetDueToggleProps) {
  return (
    <View style={styles.field}>
      {/* Same label/control stack as Select, so the row lines up with the
          pickers above it. */}
      <Text style={styles.fieldLabel}>Échéances à échoir</Text>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel="Échéances à échoir"
        accessibilityHint={
          checked
            ? 'Masquer les échéances dont la date n’est pas encore atteinte'
            : 'Afficher les échéances dont la date n’est pas encore atteinte'
        }
        accessibilityState={{ selected: checked }}
        onPress={() => onChange(!checked)}
        style={[styles.chip, checked && styles.chipActive]}
      >
        <Ionicons name="time-outline" size={16} color={checked ? colors.warning[500] : colors.gray[500]} />
        <Text style={[styles.label, checked && styles.labelActive]}>{checked ? 'Affichées' : 'Masquées'}</Text>
        {/* Only meaningful while they are hidden; once shown they are on screen. */}
        {!checked && hiddenCount > 0 && (
          <View style={styles.countBadge}>
            <Text style={styles.countText}>{hiddenCount}</Text>
          </View>
        )}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  field: {
    gap: 6,
  },
  fieldLabel: {
    fontSize: 13,
    fontWeight: '500',
    color: colors.gray[700],
  },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    minHeight: 44,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.gray[300],
    paddingHorizontal: 12,
  },
  chipActive: {
    borderColor: colors.warning[500],
    backgroundColor: colors.warning[50],
  },
  label: {
    fontSize: 13,
    fontWeight: '500',
    color: colors.gray[500],
  },
  labelActive: {
    color: colors.warning[500],
  },
  countBadge: {
    minWidth: 20,
    height: 20,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 6,
    backgroundColor: colors.gray[100],
  },
  countText: {
    fontSize: 11,
    fontWeight: '600',
    color: colors.gray[700],
  },
});
