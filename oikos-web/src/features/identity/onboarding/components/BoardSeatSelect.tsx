import { forwardRef, type SelectHTMLAttributes } from 'react';
import { Select } from '@/shared/components/Select/Select';
import { BOARD_SEATS, BOARD_SEAT_LABELS } from '@/features/identity/onboarding/constants/boardSeats';

type BoardSeatSelectProps = Omit<SelectHTMLAttributes<HTMLSelectElement>, 'children'> & { errorMessage?: string };

/** Choix du siège au conseil syndical - même contrat que Select : on lui étale register(...). */
export const BoardSeatSelect = forwardRef<HTMLSelectElement, BoardSeatSelectProps>((props, ref) => (
  <Select ref={ref} label="Votre rôle dans le conseil syndical" {...props}>
    {BOARD_SEATS.map((seat) => (
      <option key={seat} value={seat}>
        {BOARD_SEAT_LABELS[seat]}
      </option>
    ))}
  </Select>
));

BoardSeatSelect.displayName = 'BoardSeatSelect';
