import { useId, useMemo, useState } from 'react';
import {
  composePhone,
  DEFAULT_DIAL_CODE,
  DIAL_CODES,
  splitPhone,
} from '@/shared/components/PhoneField/dialCodes';

interface PhoneFieldProps {
  label: string;
  /** Numéro complet au format international (« +212612345678 »), '' si vide. */
  value: string;
  onChange: (value: string) => void;
  onBlur?: () => void;
  errorMessage?: string;
  name?: string;
}

/**
 * Saisie d'un téléphone en deux morceaux, un sélecteur d'indicatif et le numéro
 * national, pour une seule valeur au format international.
 *
 * <p>L'indicatif choisi est un état local et non une donnée du formulaire :
 * changer de pays quand le numéro est vide ne doit rien écrire (sinon un
 * formulaire jamais rempli partirait avec « +212 » seul, qui a l'air d'un
 * numéro sans en être un).
 */
export function PhoneField({ label, value, onChange, onBlur, errorMessage, name }: PhoneFieldProps) {
  const generatedId = useId();
  const inputId = name ?? generatedId;
  const parsed = useMemo(() => splitPhone(value), [value]);
  const [iso, setIso] = useState(parsed.iso);

  // La valeur peut être réécrite de l'extérieur (reprise d'un brouillon) : c'est
  // alors elle qui commande l'indicatif affiché, pas le dernier clic.
  const selectedIso = parsed.nationalNumber === '' ? iso : parsed.iso;
  const selected = DIAL_CODES.find((code) => code.iso === selectedIso) ?? DEFAULT_DIAL_CODE;

  function handleDialChange(nextIso: string) {
    setIso(nextIso);
    const nextDial = DIAL_CODES.find((code) => code.iso === nextIso)?.dial ?? DEFAULT_DIAL_CODE.dial;
    if (parsed.nationalNumber !== '') {
      onChange(composePhone(nextDial, parsed.nationalNumber));
    }
  }

  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={inputId} className="text-sm font-medium text-gray-700 dark:text-gray-300">
        {label}
      </label>
      <div className="flex gap-2">
        <select
          aria-label="Indicatif du pays"
          value={selectedIso}
          onChange={(event) => handleDialChange(event.target.value)}
          onBlur={onBlur}
          className={`min-h-11 w-28 shrink-0 rounded-lg border bg-white px-2 py-2 text-base text-gray-800 shadow-theme-xs focus:outline-none focus:ring-3 dark:bg-gray-900 dark:text-white/90 ${
            errorMessage
              ? 'border-error-500 focus:border-error-300 focus:ring-error-500/20'
              : 'border-gray-300 focus:border-brand-300 focus:ring-brand-500/20 dark:border-gray-700'
          }`}
        >
          {DIAL_CODES.map((code) => (
            // Le libellé porte le pays : deux pays partagent parfois un indicatif
            // (+1), et « +1 » seul ne dirait pas lequel on vient de choisir.
            <option key={code.iso} value={code.iso}>
              {code.flag} {code.dial}
            </option>
          ))}
        </select>
        <input
          id={inputId}
          name={name}
          type="tel"
          inputMode="tel"
          autoComplete="tel-national"
          placeholder="612345678"
          value={parsed.nationalNumber}
          onChange={(event) => onChange(composePhone(selected.dial, event.target.value))}
          onBlur={onBlur}
          aria-invalid={Boolean(errorMessage)}
          aria-describedby={errorMessage ? `${inputId}-error` : undefined}
          className={`min-h-11 w-full rounded-lg border bg-transparent px-3 py-2 text-base text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:outline-none focus:ring-3 dark:text-white/90 dark:placeholder:text-white/30 ${
            errorMessage
              ? 'border-error-500 focus:border-error-300 focus:ring-error-500/20'
              : 'border-gray-300 focus:border-brand-300 focus:ring-brand-500/20 dark:border-gray-700'
          }`}
        />
      </div>
      {errorMessage && (
        <p id={`${inputId}-error`} className="text-sm text-error-500 dark:text-error-400">
          {errorMessage}
        </p>
      )}
    </div>
  );
}
