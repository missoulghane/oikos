import { Outlet, useOutletContext } from 'react-router-dom';
import type { Property } from '@/features/property-mngt/properties/types/property.types';

/** Passes the property through, same shape as the installments and accounting sections. */
export function GeneralMeetingsSectionLayout() {
  const { property } = useOutletContext<{ property: Property }>();
  return <Outlet context={{ property }} />;
}
