import { Select } from '@/shared/components/Select/Select';
import type { AvailableUnit } from '@/features/identity/invitations/types/invitation.types';

interface UnitPickerProps {
  units: AvailableUnit[];
  value: string | null;
  onChange: (unitId: string) => void;
  errorMessage?: string;
}

export function UnitPicker({ units, value, onChange, errorMessage }: UnitPickerProps) {
  return (
    <Select
      label="Lot"
      name="unitId"
      value={value ?? ''}
      onChange={(event) => onChange(event.target.value)}
      errorMessage={errorMessage}
    >
      <option value="" disabled>
        {units.length === 0 ? 'Aucun lot disponible pour le moment' : 'Sélectionner un lot'}
      </option>
      {units.map((unit) => (
        <option key={unit.id} value={unit.id}>
          {unit.unitNumber} — {unit.unitTypeName}
        </option>
      ))}
    </Select>
  );
}
