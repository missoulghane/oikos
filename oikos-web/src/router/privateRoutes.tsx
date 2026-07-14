import { lazy, Suspense } from 'react';
import type { RouteObject } from 'react-router-dom';
import { Navigate } from 'react-router-dom';
import { AppLayout } from '@/shared/layouts/AppLayout';
import { Loader } from '@/shared/components/Loader/Loader';

const PropertiesPage = lazy(() =>
  import('@/features/properties').then((m) => ({ default: m.PropertiesPage })),
);
const CreatePropertyPage = lazy(() =>
  import('@/features/properties').then((m) => ({ default: m.CreatePropertyPage })),
);
const PropertyDetailPage = lazy(() =>
  import('@/features/properties').then((m) => ({ default: m.PropertyDetailPage })),
);

export const privateRoutes: RouteObject[] = [
  {
    element: <AppLayout />,
    children: [
      { path: '/', element: <Navigate to="/properties" replace /> },
      {
        path: '/properties',
        element: (
          <Suspense fallback={<Loader />}>
            <PropertiesPage />
          </Suspense>
        ),
      },
      {
        path: '/properties/new',
        element: (
          <Suspense fallback={<Loader />}>
            <CreatePropertyPage />
          </Suspense>
        ),
      },
      {
        path: '/properties/:id',
        element: (
          <Suspense fallback={<Loader />}>
            <PropertyDetailPage />
          </Suspense>
        ),
      },
    ],
  },
];
