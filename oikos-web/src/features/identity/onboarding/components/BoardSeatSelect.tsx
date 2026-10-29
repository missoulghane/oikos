import { forwardRef, type SelectHTMLAttributes } from 'react';
import { Select } from '@/shared/components/Select/Select';
import { Loader } from '@/shared/components/Loader/Loader';
import { useBoardRoles } from '@/shared/hooks/useBoardRoles';

type BoardSeatSelectProps = Omit<SelectHTMLAttributes<HTMLSelectElement>, 'children'> & { errorMessage?: string };

/**
 * Choix du siège au conseil syndical - même contrat que Select : on lui étale register(...).
 * Même catalogue que « Ajouter un membre » (table board_role), restreint aux
 * sièges ouverts à l'inscription. Monté une fois le catalogue chargé, pour que
 * la valeur par défaut du formulaire trouve son option.
 */
export const BoardSeatSelect = forwardRef<HTMLSelectElement, BoardSeatSelectProps>((props, ref) => {
  const boardRoles = useBoardRoles();
  if (!boardRoles.data) {
    return <Loader label="Chargement des rôles…" />;
  }
  return (
    <Select ref={ref} label="Votre rôle dans le conseil syndical" {...props}>
      {boardRoles.data
        .filter((role) => role.availableAtSignup)
        .map((role) => (
          <option key={role.code} value={role.code}>
            {role.label}
          </option>
        ))}
    </Select>
  );
});

BoardSeatSelect.displayName = 'BoardSeatSelect';
