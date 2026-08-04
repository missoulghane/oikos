import { lazy, Suspense } from 'react';
import type { RouteObject } from 'react-router-dom';
import { Navigate } from 'react-router-dom';
import { AppLayout } from '@/shared/layouts/AppLayout';
import { Loader } from '@/shared/components/Loader/Loader';
import { ForbiddenPage } from '@/shared/pages/ForbiddenPage';
import { DashboardPage } from '@/shared/pages/DashboardPage';
import { RequireAccess } from '@/router/RequireAccess';
import { canCreateProperty, canManageProperties } from '@/features/identity/me';

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
const PropertyContactsTab = lazy(() =>
  import('@/features/property-mngt/properties').then((m) => ({ default: m.PropertyContactsTab })),
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
const AccountingSectionLayout = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingSectionLayout })),
);
const AccountingTreasuryTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingTreasuryTab })),
);
const AccountingFinancialAccountsTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingFinancialAccountsTab })),
);
const AccountingExpensesTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingExpensesTab })),
);
const AccountingUnitsTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingUnitsTab })),
);
const AccountingJournalTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingJournalTab })),
);
const AccountingLettrageTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingLettrageTab })),
);
const CreateExpensePage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.CreateExpensePage })),
);
const CreateFinancialAccountPage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.CreateFinancialAccountPage })),
);
const RecordDepositPage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.RecordDepositPage })),
);
const TransferPage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.TransferPage })),
);
const RecordPaymentPage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.RecordPaymentPage })),
);
const RegularizationPage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.RegularizationPage })),
);
const PropertyConfigurationPage = lazy(() =>
  import('@/features/property-mngt/pricing').then((m) => ({ default: m.PropertyConfigurationPage })),
);
const PartiesPage = lazy(() =>
  import('@/features/property-mngt/parties').then((m) => ({ default: m.PartiesPage })),
);
const PartyDetailPage = lazy(() =>
  import('@/features/property-mngt/parties').then((m) => ({ default: m.PartyDetailPage })),
);
const MyUnitsPage = lazy(() => import('@/features/identity/me').then((m) => ({ default: m.MyUnitsPage })));

export const privateRoutes: RouteObject[] = [
  {
    element: <AppLayout />,
    children: [
      { path: '/', element: <Navigate to="/properties" replace /> },
      { path: '/forbidden', element: <ForbiddenPage /> },
      { path: '/dashboard', element: <DashboardPage /> },
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
          <RequireAccess check={canCreateProperty}>
            <Suspense fallback={<Loader />}>
              <CreatePropertyPage />
            </Suspense>
          </RequireAccess>
        ),
      },
      {
        path: '/my-units',
        element: (
          <Suspense fallback={<Loader />}>
            <MyUnitsPage />
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
                path: 'contacts',
                element: (
                  <Suspense fallback={<Loader />}>
                    <PropertyContactsTab />
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
            path: 'accounting',
            element: (
              <Suspense fallback={<Loader />}>
                <AccountingSectionLayout />
              </Suspense>
            ),
            children: [
              {
                index: true,
                element: (
                  <Suspense fallback={<Loader />}>
                    <AccountingTreasuryTab />
                  </Suspense>
                ),
              },
              {
                path: 'financial-accounts',
                element: (
                  <Suspense fallback={<Loader />}>
                    <AccountingFinancialAccountsTab />
                  </Suspense>
                ),
              },
              {
                path: 'expenses',
                element: (
                  <Suspense fallback={<Loader />}>
                    <AccountingExpensesTab />
                  </Suspense>
                ),
              },
              {
                path: 'units',
                element: (
                  <Suspense fallback={<Loader />}>
                    <AccountingUnitsTab />
                  </Suspense>
                ),
              },
              {
                path: 'journal',
                element: (
                  <Suspense fallback={<Loader />}>
                    <AccountingJournalTab />
                  </Suspense>
                ),
              },
              {
                path: 'lettrage',
                element: (
                  <Suspense fallback={<Loader />}>
                    <AccountingLettrageTab />
                  </Suspense>
                ),
              },
            ],
          },
          {
            path: 'accounting/expenses/new',
            element: (
              <Suspense fallback={<Loader />}>
                <CreateExpensePage />
              </Suspense>
            ),
          },
          {
            path: 'accounting/financial-accounts/new',
            element: (
              <Suspense fallback={<Loader />}>
                <CreateFinancialAccountPage />
              </Suspense>
            ),
          },
          {
            path: 'accounting/financial-accounts/deposit',
            element: (
              <Suspense fallback={<Loader />}>
                <RecordDepositPage />
              </Suspense>
            ),
          },
          {
            path: 'accounting/financial-accounts/transfer',
            element: (
              <Suspense fallback={<Loader />}>
                <TransferPage />
              </Suspense>
            ),
          },
        ],
      },
      {
        path: '/properties/:propertyId/units/:unitId',
        element: (
          <Suspense fallback={<Loader />}>
            <UnitDetailPage />
          </Suspense>
        ),
      },
      {
        path: '/properties/:propertyId/units/:unitId/payment',
        element: (
          <Suspense fallback={<Loader />}>
            <RecordPaymentPage />
          </Suspense>
        ),
      },
      {
        path: '/properties/:propertyId/units/:unitId/regularization',
        element: (
          <Suspense fallback={<Loader />}>
            <RegularizationPage />
          </Suspense>
        ),
      },
      {
        path: '/parties',
        element: (
          <RequireAccess check={canManageProperties}>
            <Suspense fallback={<Loader />}>
              <PartiesPage />
            </Suspense>
          </RequireAccess>
        ),
      },
      {
        // No RequireAccess here: a plain USER may view their own party record
        // (ownsParty) - the API enforces the real per-resource authorization
        // (manager/admin of the property, or the party's own linked account).
        path: '/properties/:propertyId/parties/:partyId',
        element: (
          <Suspense fallback={<Loader />}>
            <PartyDetailPage />
          </Suspense>
        ),
      },
    ],
  },
];
