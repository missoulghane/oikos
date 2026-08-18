import { lazy, Suspense } from 'react';
import type { RouteObject } from 'react-router-dom';
import { Navigate, Outlet } from 'react-router-dom';
import { AppLayout } from '@/shared/layouts/AppLayout';
import { Loader } from '@/shared/components/Loader/Loader';
import { ForbiddenPage } from '@/shared/pages/ForbiddenPage';
import { DashboardPage } from '@/shared/pages/DashboardPage';
import { LandingPage } from '@/shared/pages/LandingPage';
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
const PropertyInvitationsTab = lazy(() =>
  import('@/features/property-mngt/invitations').then((m) => ({ default: m.PropertyInvitationsTab })),
);
const PropertyBoardTab = lazy(() =>
  import('@/features/property-mngt/board-members').then((m) => ({ default: m.PropertyBoardTab })),
);
const PropertyDocumentsTab = lazy(() =>
  import('@/features/property-mngt/documents').then((m) => ({ default: m.PropertyDocumentsTab })),
);
const UnitDetailPage = lazy(() =>
  import('@/features/property-mngt/properties').then((m) => ({ default: m.UnitDetailPage })),
);
const GeneralMeetingsSectionLayout = lazy(() =>
  import('@/features/property-mngt/general-meetings').then((m) => ({ default: m.GeneralMeetingsSectionLayout })),
);
const GeneralMeetingsListTab = lazy(() =>
  import('@/features/property-mngt/general-meetings').then((m) => ({ default: m.GeneralMeetingsListTab })),
);
const CreateGeneralMeetingPage = lazy(() =>
  import('@/features/property-mngt/general-meetings').then((m) => ({ default: m.CreateGeneralMeetingPage })),
);
const GeneralMeetingDetailLayout = lazy(() =>
  import('@/features/property-mngt/general-meetings').then((m) => ({ default: m.GeneralMeetingDetailLayout })),
);
const MeetingInformationTab = lazy(() =>
  import('@/features/property-mngt/general-meetings').then((m) => ({ default: m.MeetingInformationTab })),
);
const AgendaTab = lazy(() =>
  import('@/features/property-mngt/general-meetings').then((m) => ({ default: m.AgendaTab })),
);
const ConvocationsTab = lazy(() =>
  import('@/features/property-mngt/general-meetings').then((m) => ({ default: m.ConvocationsTab })),
);
const ConvocationDetailPage = lazy(() =>
  import('@/features/property-mngt/general-meetings').then((m) => ({ default: m.ConvocationDetailPage })),
);
const SessionTab = lazy(() =>
  import('@/features/property-mngt/general-meetings').then((m) => ({ default: m.SessionTab })),
);
const MinutesTab = lazy(() =>
  import('@/features/property-mngt/general-meetings').then((m) => ({ default: m.MinutesTab })),
);
const MeetingQuorumSettingsTab = lazy(() =>
  import('@/features/property-mngt/general-meetings').then((m) => ({ default: m.MeetingQuorumSettingsTab })),
);
const MyGeneralMeetingsPage = lazy(() =>
  import('@/features/property-ownership/general-meetings').then((m) => ({ default: m.MyGeneralMeetingsPage })),
);
const MyGeneralMeetingDetailPage = lazy(() =>
  import('@/features/property-ownership/general-meetings').then((m) => ({ default: m.MyGeneralMeetingDetailPage })),
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
const AccountingOverviewTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingOverviewTab })),
);
const AccountingExerciseTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingExerciseTab })),
);
const AccountingLedgerAccountsTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingLedgerAccountsTab })),
);
const TreasuryAccountDetailPage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.TreasuryAccountDetailPage })),
);
const CreateBankAccountPage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.CreateBankAccountPage })),
);
const RecordAccountReceiptPage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.RecordAccountReceiptPage })),
);
const RecordTreasuryTransferPage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.RecordTreasuryTransferPage })),
);
const AccountingUnitAccountsTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingUnitAccountsTab })),
);
const AccountingJournalTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingJournalTab })),
);
const JournalEntryDetailPage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.JournalEntryDetailPage })),
);
const AccountingExpensesTab = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.AccountingExpensesTab })),
);
const CreateSupplierPaymentPage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.CreateSupplierPaymentPage })),
);
const CreatePayrollExpensePage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.CreatePayrollExpensePage })),
);
const CreateBankChargePage = lazy(() =>
  import('@/features/property-mngt/accounting').then((m) => ({ default: m.CreateBankChargePage })),
);
const RecordOwnerPaymentPage = lazy(() =>
  import('@/features/property-mngt/installments').then((m) => ({ default: m.RecordOwnerPaymentPage })),
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
const MyUnitDetailPage = lazy(() =>
  import('@/features/property-ownership/units').then((m) => ({ default: m.MyUnitDetailPage })),
);
const ProfilePage = lazy(() => import('@/features/identity/me').then((m) => ({ default: m.ProfilePage })));
const MyInstallmentsPage = lazy(() =>
  import('@/features/property-ownership/installments').then((m) => ({ default: m.MyInstallmentsPage })),
);
const MyInstallmentDetailPage = lazy(() =>
  import('@/features/property-ownership/installments').then((m) => ({ default: m.MyInstallmentDetailPage })),
);
const MyPaymentsPage = lazy(() =>
  import('@/features/property-ownership/payments').then((m) => ({ default: m.MyPaymentsPage })),
);
const MyPaymentDetailPage = lazy(() =>
  import('@/features/property-ownership/payments').then((m) => ({ default: m.MyPaymentDetailPage })),
);
const MyMembershipRequestsPage = lazy(() =>
  import('@/features/property-ownership/membership-requests').then((m) => ({
    default: m.MyMembershipRequestsPage,
  })),
);
const MessagingLayout = lazy(() =>
  import('@/features/messaging').then((m) => ({ default: m.MessagingLayout })),
);
const NewConversationPage = lazy(() =>
  import('@/features/messaging').then((m) => ({ default: m.NewConversationPage })),
);
const ConversationPage = lazy(() =>
  import('@/features/messaging').then((m) => ({ default: m.ConversationPage })),
);
const DraftsListPage = lazy(() =>
  import('@/features/messaging').then((m) => ({ default: m.DraftsListPage })),
);
const NotificationsListPage = lazy(() =>
  import('@/features/notifications').then((m) => ({ default: m.NotificationsListPage })),
);

export const privateRoutes: RouteObject[] = [
  {
    element: <AppLayout />,
    children: [
      { path: '/', element: <LandingPage /> },
      { path: '/forbidden', element: <ForbiddenPage /> },
      { path: '/dashboard', element: <DashboardPage /> },
      {
        path: '/profile',
        element: (
          <Suspense fallback={<Loader />}>
            <ProfilePage />
          </Suspense>
        ),
      },
      {
        // No RequireAccess here: a plain USER may view their own party record
        // (ownsParty) - the API enforces the real per-resource authorization
        // (manager/admin of the property, or the party's own linked account).
        // Kept outside both /property-mngt and /property-ownership since it is
        // genuinely dual-purpose (see PartyDetailPage's own canInvite branching).
        path: '/parties/:propertyId/:partyId',
        element: (
          <Suspense fallback={<Loader />}>
            <PartyDetailPage />
          </Suspense>
        ),
      },
      {
        // Cross-property mailbox: open to every authenticated user, same
        // rationale as /parties/:propertyId/:partyId above - the API enforces
        // per-conversation authorization (isPropertyMember /
        // isConversationParticipant) regardless of route grouping, and
        // messaging is transverse to both /property-mngt and
        // /property-ownership so it does not belong under either prefix.
        // Three mailbox views (Réception/Envoyé/Brouillon) rather than one
        // flat list - /messages itself just redirects to the default one.
        path: '/messages',
        element: <Navigate to="/messages/reception" replace />,
      },
      {
        // Single-pane view: MessagingLayout renders the conversation list:
        // conversationId fills the outlet with the selected thread, replacing
        // the list rather than sitting beside it (see MessagingLayout).
        path: '/messages/reception',
        element: (
          <Suspense fallback={<Loader />}>
            <MessagingLayout box="RECEIVED" />
          </Suspense>
        ),
        children: [
          {
            path: ':conversationId',
            element: (
              <Suspense fallback={<Loader />}>
                <ConversationPage />
              </Suspense>
            ),
          },
        ],
      },
      {
        path: '/messages/sent',
        element: (
          <Suspense fallback={<Loader />}>
            <MessagingLayout box="SENT" />
          </Suspense>
        ),
        children: [
          {
            path: ':conversationId',
            element: (
              <Suspense fallback={<Loader />}>
                <ConversationPage />
              </Suspense>
            ),
          },
        ],
      },
      {
        // No child :conversationId route here - a draft isn't a conversation
        // yet, clicking one always resumes editing via /messages/new?draftId=
        // instead of opening a read-only detail pane (see DraftsListPage).
        path: '/messages/drafts',
        element: (
          <Suspense fallback={<Loader />}>
            <DraftsListPage />
          </Suspense>
        ),
      },
      {
        path: '/messages/new',
        element: (
          <Suspense fallback={<Loader />}>
            <NewConversationPage />
          </Suspense>
        ),
      },
      {
        // Transverse, same rationale as /messages above - open to every
        // authenticated user, cuts across both /property-mngt and
        // /property-ownership (target UX rule 6: notifications are always
        // global, never scoped to the active space).
        path: '/notifications',
        element: (
          <Suspense fallback={<Loader />}>
            <NotificationsListPage />
          </Suspense>
        ),
      },
      {
        // Everything a gérant/syndic can do on a property. Guarded once at the
        // subtree root rather than per-route: before this, /properties/:id and
        // its whole tab tree (accounting, configuration, ...) had NO route
        // guard at all - only the API rejected the underlying calls, so a
        // plain owner landing here by URL still saw the full admin page shell.
        path: '/property-mngt',
        element: (
          <RequireAccess check={canManageProperties}>
            <Outlet />
          </RequireAccess>
        ),
        children: [
          {
            path: 'properties',
            element: (
              <Suspense fallback={<Loader />}>
                <PropertiesPage />
              </Suspense>
            ),
          },
          {
            path: 'properties/new',
            element: (
              <RequireAccess check={canCreateProperty}>
                <Suspense fallback={<Loader />}>
                  <CreatePropertyPage />
                </Suspense>
              </RequireAccess>
            ),
          },
          {
            path: 'properties/:id',
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
                    path: 'invitations',
                    element: (
                      <Suspense fallback={<Loader />}>
                        <PropertyInvitationsTab />
                      </Suspense>
                    ),
                  },
                  {
                    path: 'board',
                    element: (
                      <Suspense fallback={<Loader />}>
                        <PropertyBoardTab />
                      </Suspense>
                    ),
                  },
                  {
                    path: 'documents',
                    element: (
                      <Suspense fallback={<Loader />}>
                        <PropertyDocumentsTab />
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
                path: 'general-meetings',
                element: (
                  <Suspense fallback={<Loader />}>
                    <GeneralMeetingsSectionLayout />
                  </Suspense>
                ),
                children: [
                  {
                    index: true,
                    element: (
                      <Suspense fallback={<Loader />}>
                        <GeneralMeetingsListTab />
                      </Suspense>
                    ),
                  },
                  {
                    // Declared before ':meetingId' so "new" is not swallowed as an id.
                    path: 'new',
                    element: (
                      <Suspense fallback={<Loader />}>
                        <CreateGeneralMeetingPage />
                      </Suspense>
                    ),
                  },
                  {
                    path: ':meetingId',
                    element: (
                      <Suspense fallback={<Loader />}>
                        <GeneralMeetingDetailLayout />
                      </Suspense>
                    ),
                    children: [
                      {
                        // L'onglet d'accueil : ce qui décrit l'AG entière. L'ordre du jour
                        // a sa propre URL depuis qu'il n'est plus le premier onglet.
                        index: true,
                        element: (
                          <Suspense fallback={<Loader />}>
                            <MeetingInformationTab />
                          </Suspense>
                        ),
                      },
                      {
                        path: 'agenda',
                        element: (
                          <Suspense fallback={<Loader />}>
                            <AgendaTab />
                          </Suspense>
                        ),
                      },
                      {
                        path: 'convocations',
                        element: (
                          <Suspense fallback={<Loader />}>
                            <ConvocationsTab />
                          </Suspense>
                        ),
                      },
                      {
                        path: 'convocations/:convocationId',
                        element: (
                          <Suspense fallback={<Loader />}>
                            <ConvocationDetailPage />
                          </Suspense>
                        ),
                      },
                      {
                        path: 'session',
                        element: (
                          <Suspense fallback={<Loader />}>
                            <SessionTab />
                          </Suspense>
                        ),
                      },
                      {
                        path: 'minutes',
                        element: (
                          <Suspense fallback={<Loader />}>
                            <MinutesTab />
                          </Suspense>
                        ),
                      },
                    ],
                  },
                ],
              },
              {
                path: 'meeting-settings',
                element: (
                  <Suspense fallback={<Loader />}>
                    <MeetingQuorumSettingsTab />
                  </Suspense>
                ),
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
                        <AccountingOverviewTab />
                      </Suspense>
                    ),
                  },
                  {
                    path: 'ledger-accounts',
                    element: (
                      <Suspense fallback={<Loader />}>
                        <AccountingLedgerAccountsTab />
                      </Suspense>
                    ),
                  },
                  {
                    path: 'treasury-accounts/:accountId',
                    element: (
                      <Suspense fallback={<Loader />}>
                        <TreasuryAccountDetailPage />
                      </Suspense>
                    ),
                  },
                  {
                    path: 'unit-accounts',
                    element: (
                      <Suspense fallback={<Loader />}>
                        <AccountingUnitAccountsTab />
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
                    path: 'journal/:entryId',
                    element: (
                      <Suspense fallback={<Loader />}>
                        <JournalEntryDetailPage />
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
                ],
              },
              {
                path: 'accounting-exercise',
                element: (
                  <Suspense fallback={<Loader />}>
                    <AccountingExerciseTab />
                  </Suspense>
                ),
              },
              {
                path: 'accounting/treasury-accounts/new',
                element: (
                  <Suspense fallback={<Loader />}>
                    <CreateBankAccountPage />
                  </Suspense>
                ),
              },
              {
                path: 'accounting/treasury-accounts/:accountId/expenses/new',
                element: (
                  <Suspense fallback={<Loader />}>
                    <CreateSupplierPaymentPage />
                  </Suspense>
                ),
              },
              {
                path: 'accounting/treasury-accounts/:accountId/receipts/new',
                element: (
                  <Suspense fallback={<Loader />}>
                    <RecordAccountReceiptPage />
                  </Suspense>
                ),
              },
              {
                path: 'accounting/treasury-accounts/:accountId/transfers/new',
                element: (
                  <Suspense fallback={<Loader />}>
                    <RecordTreasuryTransferPage />
                  </Suspense>
                ),
              },
              {
                // Property-level entry point, for the dashboard's "Saisir une recette":
                // the account-scoped one below fixes the account, this one asks for it.
                path: 'accounting/receipts/new',
                element: (
                  <Suspense fallback={<Loader />}>
                    <RecordAccountReceiptPage />
                  </Suspense>
                ),
              },
              {
                path: 'accounting/supplier-payments/new',
                element: (
                  <Suspense fallback={<Loader />}>
                    <CreateSupplierPaymentPage />
                  </Suspense>
                ),
              },
              {
                path: 'accounting/payroll-expenses/new',
                element: (
                  <Suspense fallback={<Loader />}>
                    <CreatePayrollExpensePage />
                  </Suspense>
                ),
              },
              {
                path: 'accounting/bank-charges/new',
                element: (
                  <Suspense fallback={<Loader />}>
                    <CreateBankChargePage />
                  </Suspense>
                ),
              },
            ],
          },
          {
            path: 'properties/:propertyId/units/:unitId',
            element: (
              <Suspense fallback={<Loader />}>
                <UnitDetailPage />
              </Suspense>
            ),
          },
          {
            path: 'properties/:propertyId/units/:unitId/payment',
            element: (
              <Suspense fallback={<Loader />}>
                <RecordOwnerPaymentPage />
              </Suspense>
            ),
          },
          {
            path: 'parties',
            element: (
              <Suspense fallback={<Loader />}>
                <PartiesPage />
              </Suspense>
            ),
          },
        ],
      },
      {
        // Self-service space of a copropriétaire on their own lots. Open to
        // any authenticated user (a board/manager account may also personally
        // own a lot) - the API is scoped per-resource via ownsUnit/ownsParty
        // regardless of this route grouping.
        path: '/property-ownership',
        element: <Outlet />,
        children: [
          {
            // "Mes lots" is the owner dashboard now (see DashboardPage's
            // OwnerDashboard) - this path is kept only so older links, and the
            // lot detail's own back-link, still land somewhere sensible.
            path: 'units',
            element: <Navigate to="/dashboard" replace />,
          },
          {
            path: 'units/:propertyId/:unitId',
            element: (
              <Suspense fallback={<Loader />}>
                <MyUnitDetailPage />
              </Suspense>
            ),
          },
          {
            path: 'installments',
            element: (
              <Suspense fallback={<Loader />}>
                <MyInstallmentsPage />
              </Suspense>
            ),
          },
          {
            path: 'installments/:installmentId',
            element: (
              <Suspense fallback={<Loader />}>
                <MyInstallmentDetailPage />
              </Suspense>
            ),
          },
          {
            path: 'payments',
            element: (
              <Suspense fallback={<Loader />}>
                <MyPaymentsPage />
              </Suspense>
            ),
          },
          {
            path: 'payments/:paymentId',
            element: (
              <Suspense fallback={<Loader />}>
                <MyPaymentDetailPage />
              </Suspense>
            ),
          },
          {
            path: 'general-meetings',
            element: (
              <Suspense fallback={<Loader />}>
                <MyGeneralMeetingsPage />
              </Suspense>
            ),
          },
          {
            path: 'general-meetings/:meetingId',
            element: (
              <Suspense fallback={<Loader />}>
                <MyGeneralMeetingDetailPage />
              </Suspense>
            ),
          },
          {
            path: 'membership-requests',
            element: (
              <Suspense fallback={<Loader />}>
                <MyMembershipRequestsPage />
              </Suspense>
            ),
          },
        ],
      },
    ],
  },
];
