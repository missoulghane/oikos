import type { PropsWithChildren, ReactNode } from 'react';

interface EmptyStateProps {
  title: string;
  action?: ReactNode;
}

export function EmptyState({ title, action, children }: PropsWithChildren<EmptyStateProps>) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-2xl border border-dashed border-gray-300 dark:border-gray-700 py-12 text-center">
      <p className="text-sm font-medium text-gray-600 dark:text-gray-400">{title}</p>
      {children && <p className="text-sm text-gray-400 dark:text-gray-500">{children}</p>}
      {action}
    </div>
  );
}
