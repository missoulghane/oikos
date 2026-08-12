import type { PropsWithChildren, ReactNode } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors } from '@/shared/theme/colors';

interface EmptyStateProps {
  title: string;
  action?: ReactNode;
}

export function EmptyState({ title, action, children }: PropsWithChildren<EmptyStateProps>) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      {children && <Text style={styles.body}>{children}</Text>}
      {action}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    gap: 12,
    borderRadius: 16,
    borderWidth: 1,
    borderStyle: 'dashed',
    borderColor: colors.gray[300],
    paddingVertical: 48,
    paddingHorizontal: 16,
  },
  title: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.gray[600],
    textAlign: 'center',
  },
  body: {
    fontSize: 14,
    color: colors.gray[400],
    textAlign: 'center',
  },
});
