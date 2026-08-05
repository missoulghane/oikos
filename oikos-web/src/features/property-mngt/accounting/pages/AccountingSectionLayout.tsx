import { Outlet, useOutletContext } from 'react-router-dom';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

export function AccountingSectionLayout() {
  const { property } = useOutletContext<{ property: Property }>();

  return <Outlet context={{ property }} />;
}
