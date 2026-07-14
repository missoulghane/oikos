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
        <label htmlFor={selectId} className="text-sm font-medium text-slate-700">
          {label}
        </label>
        <select
          id={selectId}
          ref={ref}
          aria-invalid={Boolean(errorMessage)}
          aria-describedby={errorMessage ? `${selectId}-error` : undefined}
          className={`min-h-11 rounded-md border bg-white px-3 py-2 text-base focus:outline-none focus:ring-2 focus:ring-slate-400 ${
            errorMessage ? 'border-red-500' : 'border-slate-300'
          } ${className}`}
          {...rest}
        >
          {children}
        </select>
        {errorMessage && (
          <p id={`${selectId}-error`} className="text-sm text-red-600">
            {errorMessage}
          </p>
        )}
      </div>
    );
  },
);

Select.displayName = 'Select';
