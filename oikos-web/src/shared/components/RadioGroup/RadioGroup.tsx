import { forwardRef, type InputHTMLAttributes } from 'react';

interface RadioOption {
  value: string;
  label: string;
}

interface RadioGroupProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label: string;
  options: RadioOption[];
  errorMessage?: string;
}

// Same contract as Input/Select: spread react-hook-form's register(...) onto it
// ({name, onChange, onBlur, ref}). The ref is attached to every radio of the
// group - react-hook-form tracks radios by their shared name, so it reads the
// checked one back as a single field value.
export const RadioGroup = forwardRef<HTMLInputElement, RadioGroupProps>(
  ({ label, options, errorMessage, id, className = '', disabled, ...rest }, ref) => {
    const groupId = id ?? rest.name;

    return (
      <fieldset className="flex flex-col gap-1">
        <legend className="text-sm font-medium text-gray-700 dark:text-gray-300">
          {label}
          {/* Même convention que Input/Select - voir RequiredFieldsHint. Ici
              l'astérisque reste dans la légende, qui doit être le premier enfant
              du fieldset : aria-hidden l'exclut du nom accessible du groupe. */}
          {rest.required && (
            <span aria-hidden="true" className="text-gray-500 dark:text-gray-400">
              {' *'}
            </span>
          )}
        </legend>
        <div className={`flex flex-wrap items-center gap-x-6 ${className}`}>
          {options.map((option) => (
            <label
              key={option.value}
              className={`flex min-h-11 items-center gap-2 text-base text-gray-800 dark:text-white/90 ${
                disabled ? 'cursor-not-allowed opacity-50' : 'cursor-pointer'
              }`}
            >
              <input
                type="radio"
                ref={ref}
                value={option.value}
                disabled={disabled}
                aria-invalid={Boolean(errorMessage)}
                aria-describedby={errorMessage ? `${groupId}-error` : undefined}
                className="size-5 shrink-0 accent-brand-500 focus:outline-none focus:ring-3 focus:ring-brand-500/20"
                {...rest}
              />
              {option.label}
            </label>
          ))}
        </div>
        {errorMessage && (
          <p id={`${groupId}-error`} className="text-sm text-error-500 dark:text-error-400">
            {errorMessage}
          </p>
        )}
      </fieldset>
    );
  },
);

RadioGroup.displayName = 'RadioGroup';
