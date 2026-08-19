import type { ReactNode } from 'react';

interface SelectableCardProps {
  name: string;
  value: string;
  checked: boolean;
  onSelect: (value: string) => void;
  title: string;
  description: ReactNode;
  /** 'radio' = un seul choix possible, 'checkbox' = plusieurs. */
  type?: 'radio' | 'checkbox';
}

/**
 * Carte entièrement cliquable : c'est le `<label>` qui porte la surface, si bien
 * que le clic n'importe où bascule le contrôle natif qu'il enveloppe - sans quoi
 * seul le petit rond serait actionnable, et le focus clavier disparaîtrait.
 */
export function SelectableCard({
  name,
  value,
  checked,
  onSelect,
  title,
  description,
  type = 'radio',
}: SelectableCardProps) {
  return (
    <label
      className={`flex cursor-pointer gap-3 rounded-2xl border p-4 transition-colors ${
        checked
          ? 'border-brand-500 bg-brand-50 dark:border-brand-400 dark:bg-brand-500/[0.12]'
          : 'border-gray-200 hover:bg-gray-50 dark:border-gray-800 dark:hover:bg-white/[0.03]'
      }`}
    >
      <input
        type={type}
        name={name}
        value={value}
        checked={checked}
        onChange={() => onSelect(value)}
        className="mt-0.5 size-5 shrink-0 accent-brand-500"
      />
      <span className="flex flex-col gap-1">
        <span className="text-sm font-medium text-gray-900 dark:text-white/90">{title}</span>
        <span className="text-sm text-gray-600 dark:text-gray-400">{description}</span>
      </span>
    </label>
  );
}
