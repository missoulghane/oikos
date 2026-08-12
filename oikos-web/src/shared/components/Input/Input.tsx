import { forwardRef, useState, type InputHTMLAttributes } from 'react';
import { EyeIcon, EyeCloseIcon } from '@/shared/icons';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  errorMessage?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, errorMessage, id, className = '', type, ...rest }, ref) => {
    const inputId = id ?? rest.name;
    const isPassword = type === 'password';
    const [isVisible, setIsVisible] = useState(false);

    return (
      <div className="flex flex-col gap-1">
        <label htmlFor={inputId} className="text-sm font-medium text-gray-700 dark:text-gray-300">
          {label}
        </label>
        <div className="relative">
          <input
            id={inputId}
            ref={ref}
            type={isPassword ? (isVisible ? 'text' : 'password') : type}
            aria-invalid={Boolean(errorMessage)}
            aria-describedby={errorMessage ? `${inputId}-error` : undefined}
            className={`min-h-11 w-full rounded-lg border bg-transparent px-3 py-2 text-base text-gray-800 dark:text-white/90 shadow-theme-xs placeholder:text-gray-400 dark:placeholder:text-white/30 focus:outline-none focus:ring-3 ${
              isPassword ? 'pr-10' : ''
            } ${
              errorMessage
                ? 'border-error-500 focus:border-error-300 focus:ring-error-500/20'
                : 'border-gray-300 dark:border-gray-700 focus:border-brand-300 focus:ring-brand-500/20'
            } ${className}`}
            {...rest}
          />
          {isPassword && (
            <button
              type="button"
              onClick={() => setIsVisible((value) => !value)}
              aria-label={isVisible ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
              className="absolute right-4 top-1/2 z-30 -translate-y-1/2 cursor-pointer"
            >
              {isVisible ? (
                <EyeIcon className="fill-gray-500 dark:fill-gray-400 size-5" />
              ) : (
                <EyeCloseIcon className="fill-gray-500 dark:fill-gray-400 size-5" />
              )}
            </button>
          )}
        </div>
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
