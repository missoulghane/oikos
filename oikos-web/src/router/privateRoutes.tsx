import { lazy, Suspense } from 'react';
import type { RouteObject } from 'react-router-dom';
import { Navigate } from 'react-router-dom';
import { AppLayout } from '@/shared/layouts/AppLayout';
import { Loader } from '@/shared/components/Loader/Loader';

const PropertiesPage = lazy(() =>
  import('@/features/property-mngt/properties').then((m) => ({ default: m.PropertiesPage })),
);
const CreatePropertyPage = lazy(() =>
  import('@/features/property-mngt/properties').then((m) => ({ default: m.CreatePropertyPage })),
);
const PropertyDetailLayout = lazy(() =>
  import('@/features/property-mngt/properties').then((m) => ({ default: m.PropertyDetailLayout })),
);
const PropertyInfoSectionLayout = lazy(() =>
  import('@/features/property-mngt/properties').then((m) => ({ default: m.PropertyInfoSectionLayout })),
);
const PropertyGeneralInfoTab = lazy(() =>
  import('@/features/property-mngt/properties').then((m) => ({ default: m.PropertyGeneralInfoTab })),
);
const PropertyLotsTab = lazy(() =>
  import('@/features/property-mngt/properties').then((m) => ({ default: m.PropertyLotsTab })),
);
const UnitDetailPage = lazy(() =>
  import('@/features/property-mngt/properties').then((m) => ({ default: m.UnitDetailPage })),
);
const InstallmentsSectionLayout = lazy(() =>
  import('@/features/property-mngt/installments').then((m) => ({ default: m.InstallmentsSectionLayout })),
);
const InstallmentsListTab = lazy(() =>
  import('@/features/property-mngt/installments').then((m) => ({ default: m.InstallmentsListTab })),
);
const InstallmentCallsTab = lazy(() =>
  import('@/features/property-mngt/installments').then((m) => ({ default: m.InstallmentCallsTab })),
);
const InstallmentsOtherTab = lazy(() =>
  import('@/features/property-mngt/installments').then((m) => ({ default: m.InstallmentsOtherTab })),
);
const PropertyConfigurationPage = lazy(() =>
  import('@/features/property-mngt/pricing').then((m) => ({ default: m.PropertyConfigurationPage })),
);
const FinanceSectionLayout = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.FinanceSectionLayout })),
);
const MovementsTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.MovementsTab })),
);
const FinanceOtherTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.FinanceOtherTab })),
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
            <PropertyDetailLayout />
          </Suspense>
        ),
        children: [
          { index: true, element: <Navigate to="property" replace /> },
          {
            path: 'property',
            element: (
              <Suspense fallback={<Loader />}>
                <PropertyInfoSectionLayout />
              </Suspense>
            ),
            children: [
              {
                index: true,
                element: (
                  <Suspense fallback={<Loader />}>
                    <PropertyGeneralInfoTab />
                  </Suspense>
                ),
              },
              {
                path: 'lots',
                element: (
                  <Suspense fallback={<Loader />}>
                    <PropertyLotsTab />
                  </Suspense>
                ),
              },
              {
                path: 'configuration',
                element: (
                  <Suspense fallback={<Loader />}>
                    <PropertyConfigurationPage />
                  </Suspense>
                ),
              },
            ],
          },
          {
            path: 'installments',
            element: (
              <Suspense fallback={<Loader />}>
                <InstallmentsSectionLayout />
              </Suspense>
            ),
            children: [
              {
                index: true,
                element: (
                  <Suspense fallback={<Loader />}>
                    <InstallmentsListTab />
                  </Suspense>
                ),
              },
              {
                path: 'calls',
                element: (
                  <Suspense fallback={<Loader />}>
                    <InstallmentCallsTab />
                  </Suspense>
                ),
              },
              {
                path: 'other',
                element: (
                  <Suspense fallback={<Loader />}>
                    <InstallmentsOtherTab />
                  </Suspense>
                ),
              },
            ],
          },
          {
            path: 'finance',
            element: (
              <Suspense fallback={<Loader />}>
                <FinanceSectionLayout />
              </Suspense>
            ),
            children: [
              {
                index: true,
                element: (
                  <Suspense fallback={<Loader />}>
                    <MovementsTab />
                  </Suspense>
                ),
              },
              {
                path: 'other',
                element: (
                  <Suspense fallback={<Loader />}>
                    <FinanceOtherTab />
                  </Suspense>
                ),
              },
            ],
          },
        ],
      },
      {
        path: '/units/:id',
        element: (
          <Suspense fallback={<Loader />}>
            <UnitDetailPage />
          </Suspense>
        ),
      },
    ],
  },
];
