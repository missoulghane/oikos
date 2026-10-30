import type { ButtonHTMLAttributes } from 'react';

type ButtonVariant = 'primary' | 'secondary';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  isLoading?: boolean;
}

const VARIANT_CLASSES: Record<ButtonVariant, string> = {
  primary: 'bg-brand-500 text-white shadow-theme-xs hover:bg-brand-600 disabled:bg-brand-300',
  secondary:
    'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-400 shadow-theme-xs ring-1 ring-inset ring-gray-300 dark:ring-gray-700 hover:bg-gray-50 dark:hover:bg-white/[0.03] dark:hover:text-gray-300 disabled:text-gray-400 dark:disabled:text-gray-500',
};

const BASE_CLASSES =
  'inline-flex min-h-11 items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-colors disabled:cursor-not-allowed';

/**
 * The button look, for an element that is not a <button> - a router Link that
 * navigates, so that it keeps middle click and "open in a new tab" while
 * looking like the actions next to it.
 */
export function buttonClassName(variant: ButtonVariant = 'primary', className = ''): string {
  return `${BASE_CLASSES} ${VARIANT_CLASSES[variant]} ${className}`;
}

export function Button({
  variant = 'primary',
  isLoading = false,
  disabled,
  children,
  className = '',
  ...rest
}: ButtonProps) {
  return (
    <button
      className={buttonClassName(variant, className)}
      disabled={disabled ?? isLoading}
      {...rest}
    >
      {isLoading ? 'Chargement…' : children}
    </button>
  );
}
