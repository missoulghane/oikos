import { useState } from 'react';
import { Select } from '@/shared/components/Select/Select';
import { useBuildings } from '@/features/property-mngt/properties/hooks/useBuildings';
import { useUnits } from '@/features/property-mngt/properties/hooks/useUnits';

// No property-wide unit search endpoint exists (units are only listable per
// building) - one large page per dropdown is simpler than paginated pickers
// for the small datasets a copropriété actually has, same trade-off as
// INSTALLMENT_CALLS_PAGE_SIZE in InstallmentFilters.
const PICKER_PAGE_SIZE = 100;

interface UnitPickerProps {
  propertyId: string;
  unitId: string;
  onChange: (unitId: string) => void;
}

export function UnitPicker({ propertyId, unitId, onChange }: UnitPickerProps) {
  const [buildingId, setBuildingId] = useState('');
  const buildings = useBuildings(propertyId, 0, PICKER_PAGE_SIZE);
  const units = useUnits(buildingId, 0, undefined, undefined, PICKER_PAGE_SIZE);

  return (
    <>
      <Select
        label="Immeuble"
        value={buildingId}
        onChange={(e) => {
          setBuildingId(e.target.value);
          onChange('');
        }}
      >
        <option value="">Sélectionner…</option>
        {buildings.data?.content.map((building) => (
          <option key={building.id} value={building.id}>
            {building.name}
          </option>
        ))}
      </Select>
      <Select label="Lot" value={unitId} onChange={(e) => onChange(e.target.value)} disabled={!buildingId}>
        <option value="">Sélectionner…</option>
        {units.data?.content.map((unit) => (
          <option key={unit.id} value={unit.id}>
            {unit.unitNumber}
            {unit.ownerFullNames.length > 0 ? ` — ${unit.ownerFullNames.join(', ')}` : ''}
          </option>
        ))}
      </Select>
    </>
  );
}
