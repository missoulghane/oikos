import type { PropsWithChildren } from 'react';

export function Card({ children, className = '' }: PropsWithChildren<{ className?: string }>) {
  return (
    <div className={`rounded-2xl border border-gray-200 bg-white p-4 shadow-theme-xs sm:p-6 ${className}`}>
      {children}
    </div>
  );
}
