import { useState, type ReactNode } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors } from '@/shared/theme/colors';

interface FilterPanelSearch {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

interface FilterPanelProps {
  /** Number of fields differing from their defaults - see countActiveFilters. */
  activeCount: number;
  onClear: () => void;
  /** Omit on lists that have no meaningful free-text dimension. */
  search?: FilterPanelSearch;
  children: ReactNode;
}

/**
 * Mirrors oikos-web's FilterPanel: a stack of pickers is taller than the phone
 * viewport, so the list itself would start below the fold. The fields stay
 * collapsed behind a "Filtres" toggle until asked for. Unlike the web version
 * there is no breakpoint where they are permanently visible - every screen here
 * is a phone screen.
 */
export function FilterPanel({ activeCount, onClear, search, children }: FilterPanelProps) {
  const [isOpen, setIsOpen] = useState(false);
  const hasActiveFilters = activeCount > 0;

  return (
    <View style={styles.container}>
      {/* One row: the search field takes the leftover width, the toggle keeps
          its label (discoverability is worth more here than 40pt of input) and
          the reset shrinks to its icon, named for screen readers. */}
      <View style={styles.toolbar}>
        {search && (
          <View style={styles.searchField}>
            <Ionicons name="search" size={16} color={colors.gray[400]} />
            <TextInput
              value={search.value}
              onChangeText={search.onChange}
              placeholder={search.placeholder ?? 'Rechercher…'}
              placeholderTextColor={colors.gray[400]}
              accessibilityLabel="Rechercher"
              autoCorrect={false}
              style={styles.searchInput}
            />
            {search.value.length > 0 && (
              <Pressable
                accessibilityRole="button"
                accessibilityLabel="Effacer la recherche"
                onPress={() => search.onChange('')}
              >
                <Ionicons name="close-circle" size={16} color={colors.gray[400]} />
              </Pressable>
            )}
          </View>
        )}

        <Pressable
          accessibilityRole="button"
          accessibilityLabel="Filtres"
          accessibilityState={{ expanded: isOpen }}
          onPress={() => setIsOpen((value) => !value)}
          style={[styles.toggle, hasActiveFilters && styles.toggleActive]}
        >
          <Ionicons
            name={isOpen ? 'funnel' : 'funnel-outline'}
            size={16}
            color={hasActiveFilters ? colors.brand[500] : colors.gray[700]}
          />
          <Text style={[styles.toggleText, hasActiveFilters && styles.toggleTextActive]}>Filtres</Text>
          {hasActiveFilters && (
            <View style={styles.countBadge}>
              <Text style={styles.countText}>{activeCount}</Text>
            </View>
          )}
        </Pressable>

        {hasActiveFilters && (
          <Pressable
            accessibilityRole="button"
            accessibilityLabel="Réinitialiser les filtres"
            onPress={onClear}
            style={styles.clear}
          >
            <Ionicons name="close" size={18} color={colors.gray[500]} />
          </Pressable>
        )}
      </View>

      {/* Unmounted rather than hidden: RN has no `display: none`, and the filter
          values live in the parent's state anyway, so nothing is lost. */}
      {isOpen && <View style={styles.fields}>{children}</View>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 12,
  },
  toolbar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  searchField: {
    // Takes the width the buttons leave; flexShrink lets it give way rather
    // than pushing them off the row on a narrow phone.
    flex: 1,
    flexShrink: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    minHeight: 44,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.gray[300],
    paddingHorizontal: 12,
  },
  searchInput: {
    flex: 1,
    fontSize: 15,
    color: colors.gray[800],
    // Zeroes the intrinsic padding Android gives a TextInput, which would
    // otherwise make this row taller than the 44pt touch target above.
    paddingVertical: 0,
  },
  toggle: {
    flexShrink: 0,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    minHeight: 44,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.gray[300],
    paddingHorizontal: 12,
  },
  toggleActive: {
    borderColor: colors.brand[500],
    backgroundColor: colors.brand[50],
  },
  toggleText: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[700],
  },
  toggleTextActive: {
    color: colors.brand[500],
  },
  countBadge: {
    minWidth: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: colors.brand[500],
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 6,
  },
  countText: {
    fontSize: 12,
    fontWeight: '600',
    color: colors.white,
  },
  clear: {
    flexShrink: 0,
    alignItems: 'center',
    justifyContent: 'center',
    minWidth: 44,
    minHeight: 44,
  },
  fields: {
    gap: 12,
  },
});
