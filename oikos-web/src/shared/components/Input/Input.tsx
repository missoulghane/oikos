import { forwardRef, type InputHTMLAttributes } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  errorMessage?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, errorMessage, id, className = '', ...rest }, ref) => {
    const inputId = id ?? rest.name;

    return (
      <div className="flex flex-col gap-1">
        <label htmlFor={inputId} className="text-sm font-medium text-gray-700 dark:text-gray-300">
          {label}
        </label>
        <input
          id={inputId}
          ref={ref}
          aria-invalid={Boolean(errorMessage)}
          aria-describedby={errorMessage ? `${inputId}-error` : undefined}
          className={`min-h-11 rounded-lg border bg-transparent px-3 py-2 text-base text-gray-800 dark:text-white/90 shadow-theme-xs placeholder:text-gray-400 dark:placeholder:text-white/30 focus:outline-none focus:ring-3 ${
            errorMessage
              ? 'border-error-500 focus:border-error-300 focus:ring-error-500/20'
              : 'border-gray-300 dark:border-gray-700 focus:border-brand-300 focus:ring-brand-500/20'
          } ${className}`}
          {...rest}
        />
        {errorMessage && (
          <p id={`${inputId}-error`} className="text-sm text-error-500 dark:text-error-400">
            {errorMessage}
          </p>
        )}
      </div>
    );
  },
);

Input.displayName = 'Input';
