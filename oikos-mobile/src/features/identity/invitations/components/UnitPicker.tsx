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
      value={value ?? ''}
      onChange={onChange}
      options={units.map((unit) => ({ value: unit.id, label: `${unit.unitNumber} — ${unit.unitTypeName}` }))}
      placeholder={units.length === 0 ? 'Aucun lot disponible pour le moment' : 'Sélectionner un lot'}
      errorMessage={errorMessage}
    />
  );
}
