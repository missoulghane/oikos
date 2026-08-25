import { forwardRef, type SelectHTMLAttributes } from 'react';

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  errorMessage?: string;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, errorMessage, id, className = '', children, ...rest }, ref) => {
    const selectId = id ?? rest.name;

    return (
      <div className="flex flex-col gap-1">
        {/* Même convention que Input : l'astérisque dit « obligatoire », son
            absence dit « facultatif » sans avoir à l'écrire sur l'étiquette, et il
            reste hors de <label> pour ne pas s'inviter dans le nom du champ. */}
        <div className="flex items-center gap-1">
          <label htmlFor={selectId} className="text-sm font-medium text-gray-700 dark:text-gray-300">
            {label}
          </label>
          {rest.required && (
            <span aria-hidden="true" className="text-sm font-medium text-gray-500 dark:text-gray-400">
              *
            </span>
          )}
        </div>
        <select
          id={selectId}
          ref={ref}
          aria-invalid={Boolean(errorMessage)}
          aria-describedby={errorMessage ? `${selectId}-error` : undefined}
          className={`min-h-11 rounded-lg border bg-white dark:bg-gray-900 px-3 py-2 text-base text-gray-800 dark:text-white/90 shadow-theme-xs focus:outline-none focus:ring-3 ${
            errorMessage
              ? 'border-error-500 focus:border-error-300 focus:ring-error-500/20'
              : 'border-gray-300 dark:border-gray-700 focus:border-brand-300 focus:ring-brand-500/20'
          } ${className}`}
          {...rest}
        >
          {children}
        </select>
        {errorMessage && (
          <p id={`${selectId}-error`} className="text-sm text-error-500 dark:text-error-400">
            {errorMessage}
          </p>
        )}
      </div>
    );
  },
);

Select.displayName = 'Select';
