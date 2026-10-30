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
        {/* appearance-none et chevron dessiné, comme les listes du gabarit
            (oikos-template) : la flèche native du navigateur change d'un
            système à l'autre et collait au bord du champ. */}
        <div className="relative">
          <select
            id={selectId}
            ref={ref}
            aria-invalid={Boolean(errorMessage)}
            aria-describedby={errorMessage ? `${selectId}-error` : undefined}
            className={`min-h-11 w-full appearance-none rounded-lg border bg-transparent py-2 pl-3 pr-11 text-base text-gray-800 shadow-theme-xs focus:outline-none focus:ring-3 dark:bg-gray-900 dark:text-white/90 [&>option]:text-gray-700 dark:[&>option]:bg-gray-900 dark:[&>option]:text-gray-400 ${
              errorMessage
                ? 'border-error-500 focus:border-error-300 focus:ring-error-500/20'
                : 'border-gray-300 dark:border-gray-700 focus:border-brand-300 focus:ring-brand-500/20'
            } ${className}`}
            {...rest}
          >
            {children}
          </select>
          <svg
            aria-hidden="true"
            className="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-gray-500 dark:text-gray-400"
            width="20"
            height="20"
            viewBox="0 0 20 20"
            fill="none"
          >
            <path
              d="M4.79175 7.396L10.0001 12.6043L15.2084 7.396"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </div>
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
