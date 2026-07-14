import { Link } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import type { Property } from '@/features/properties/types/property.types';

export function PropertyCard({ property }: { property: Property }) {
  return (
    <Link to={`/properties/${property.id}`} className="block">
      <Card>
        <h2 className="font-medium text-slate-900">{property.name}</h2>
        <p className="text-sm text-slate-500">{property.address}</p>
      </Card>
    </Link>
  );
}
