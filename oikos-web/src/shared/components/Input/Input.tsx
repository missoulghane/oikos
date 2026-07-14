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
        <label htmlFor={inputId} className="text-sm font-medium text-slate-700">
          {label}
        </label>
        <input
          id={inputId}
          ref={ref}
          aria-invalid={Boolean(errorMessage)}
          aria-describedby={errorMessage ? `${inputId}-error` : undefined}
          className={`min-h-11 rounded-md border px-3 py-2 text-base focus:outline-none focus:ring-2 focus:ring-slate-400 ${
            errorMessage ? 'border-red-500' : 'border-slate-300'
          } ${className}`}
          {...rest}
        />
        {errorMessage && (
          <p id={`${inputId}-error`} className="text-sm text-red-600">
            {errorMessage}
          </p>
        )}
      </div>
    );
  },
);

Input.displayName = 'Input';
