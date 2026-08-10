import { Link } from 'react-router-dom';
import { Card } from '@/shared/components/Card/Card';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function PropertyCard({ property }: { property: Property }) {
  return (
    <Link to={`/property-mngt/properties/${property.id}`} className="block">
      <Card>
        <h2 className="font-medium text-gray-900 dark:text-white/90">{property.name}</h2>
        <p className="text-sm text-gray-500 dark:text-gray-400">{property.address}</p>
      </Card>
    </Link>
  );
}
