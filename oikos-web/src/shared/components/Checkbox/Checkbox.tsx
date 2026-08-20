import { forwardRef, type InputHTMLAttributes } from 'react';

interface CheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label: string;
  /** Précision affichée sous le libellé : ce que cocher ou décocher déclenche. */
  hint?: string;
  errorMessage?: string;
}

// Même contrat qu'Input/Select/RadioGroup : on lui étale le register(...) de
// react-hook-form ({name, onChange, onBlur, ref}).
export const Checkbox = forwardRef<HTMLInputElement, CheckboxProps>(
  ({ label, hint, errorMessage, id, className = '', disabled, ...rest }, ref) => {
    const inputId = id ?? rest.name;

    return (
      <div className={`flex flex-col gap-1 ${className}`}>
        <label
          className={`flex items-start gap-2 text-base text-gray-800 dark:text-white/90 ${
            disabled ? 'cursor-not-allowed opacity-50' : 'cursor-pointer'
          }`}
        >
          <input
            id={inputId}
            type="checkbox"
            ref={ref}
            disabled={disabled}
            aria-invalid={Boolean(errorMessage)}
            aria-describedby={errorMessage ? `${inputId}-error` : undefined}
            // mt-0.5 : la case s'aligne sur la première ligne du libellé plutôt
            // que de flotter au milieu quand un texte d'aide le fait passer à
            // deux lignes.
            className="mt-0.5 size-5 shrink-0 rounded border-gray-300 accent-brand-500 focus:outline-none focus:ring-3 focus:ring-brand-500/20 dark:border-gray-700"
            {...rest}
          />
          <span className="flex flex-col">
            {label}
            {hint && <span className="text-sm text-gray-500 dark:text-gray-400">{hint}</span>}
          </span>
        </label>
        {errorMessage && (
          <p id={`${inputId}-error`} className="text-sm text-error-500 dark:text-error-400">
            {errorMessage}
          </p>
        )}
      </div>
    );
  },
);

Checkbox.displayName = 'Checkbox';
