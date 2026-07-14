import type { PropsWithChildren, ReactNode } from 'react';

interface EmptyStateProps {
  title: string;
  action?: ReactNode;
}

export function EmptyState({ title, action, children }: PropsWithChildren<EmptyStateProps>) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-lg border border-dashed border-slate-300 py-12 text-center">
      <p className="text-sm font-medium text-slate-600">{title}</p>
      {children && <p className="text-sm text-slate-400">{children}</p>}
      {action}
    </div>
  );
}
