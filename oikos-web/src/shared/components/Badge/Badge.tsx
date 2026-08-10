import type { PropsWithChildren } from 'react';

type BadgeVariant = 'light' | 'solid';
type BadgeColor = 'primary' | 'success' | 'error' | 'warning' | 'info' | 'light' | 'dark';

interface BadgeProps {
  variant?: BadgeVariant;
  color?: BadgeColor;
  className?: string;
}

const VARIANT_CLASSES: Record<BadgeVariant, Record<BadgeColor, string>> = {
  light: {
    primary: 'bg-brand-50 dark:bg-brand-500/[0.12] text-brand-500 dark:text-brand-400',
    success: 'bg-success-50 dark:bg-success-500/15 text-success-600 dark:text-success-500',
    error: 'bg-error-50 dark:bg-error-500/15 text-error-600 dark:text-error-400',
    warning: 'bg-warning-50 dark:bg-warning-500/15 text-warning-600 dark:text-warning-400',
    info: 'bg-blue-light-50 dark:bg-blue-light-500/15 text-blue-light-500',
    light: 'bg-gray-100 dark:bg-white/[0.05] text-gray-700 dark:text-gray-300',
    dark: 'bg-gray-500 text-white',
  },
  solid: {
    primary: 'bg-brand-500 text-white',
    success: 'bg-success-500 text-white',
    error: 'bg-error-500 text-white',
    warning: 'bg-warning-500 text-white',
    info: 'bg-blue-light-500 text-white',
    light: 'bg-gray-400 text-white',
    dark: 'bg-gray-700 text-white',
  },
};

export function Badge({
  variant = 'light',
  color = 'primary',
  className = '',
  children,
}: PropsWithChildren<BadgeProps>) {
  return (
    <span
      className={`inline-flex w-fit items-center justify-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium ${VARIANT_CLASSES[variant][color]} ${className}`}
    >
      {children}
    </span>
  );
}
