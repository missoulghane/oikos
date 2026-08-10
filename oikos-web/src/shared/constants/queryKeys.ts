export const queryKeys = {
  me: {
    detail: () => ['me', 'detail'] as const,
    units: () => ['me', 'units'] as const,
    installments: () => ['me', 'installments'] as const,
    membershipRequests: () => ['me', 'membership-requests'] as const,
  },
  properties: {
    list: (page: number, size: number) => ['properties', 'list', page, size] as const,
    detail: (id: string) => ['properties', 'detail', id] as const,
    buildings: (propertyId: string, page: number, size: number) =>
      ['properties', propertyId, 'buildings', page, size] as const,
    installments: (
      propertyId: string,
      page: number,
      size: number,
      status: string[] | undefined,
      dueDateFrom: string | undefined,
      dueDateTo: string | undefined,
      installmentCallId: string | undefined,
      sortBy: string,
      sortDirection: string,
    ) =>
      [
        'properties',
        propertyId,
        'installments',
        page,
        size,
        status,
        dueDateFrom,
        dueDateTo,
        installmentCallId,
        sortBy,
        sortDirection,
      ] as const,
    unitTypePrices: (propertyId: string) => ['properties', propertyId, 'unit-type-prices'] as const,
    unitTypes: (propertyId: string) => ['properties', propertyId, 'unit-types'] as const,
    installmentCalls: (propertyId: string, page: number, size: number) =>
      ['properties', propertyId, 'installment-calls', page, size] as const,
    contacts: (
      propertyId: string,
      page: number,
      size: number,
      search: string | undefined,
      hasLinkedAccount: boolean | undefined,
    ) => ['properties', propertyId, 'contacts', page, size, search, hasLinkedAccount] as const,
    accountingOpenExercise: (propertyId: string) =>
      ['properties', propertyId, 'accounting', 'open-exercise'] as const,
    accountingLedgerAccounts: (propertyId: string) =>
      ['properties', propertyId, 'accounting', 'ledger-accounts'] as const,
    accountingJournalEntries: (propertyId: string, page: number, size: number) =>
      ['properties', propertyId, 'accounting', 'entries', page, size] as const,
    accountingJournalEntry: (propertyId: string, entryId: string) =>
      ['properties', propertyId, 'accounting', 'entries', entryId] as const,
    accountingExpenses: (propertyId: string) => ['properties', propertyId, 'accounting', 'expenses'] as const,
    latestPayment: (propertyId: string) => ['properties', propertyId, 'payments', 'latest'] as const,
    ledgerAccountEntries: (
      propertyId: string,
      accountId: string,
      page: number,
      size: number,
      pieceDateFrom: string | undefined,
      pieceDateTo: string | undefined,
      search: string | undefined,
      status: string | undefined,
    ) =>
      [
        'properties',
        propertyId,
        'accounting',
        'ledger-accounts',
        accountId,
        'entries',
        page,
        size,
        pieceDateFrom,
        pieceDateTo,
        search,
        status,
      ] as const,
  },
  installmentCalls: {
    detail: (id: string) => ['installment-calls', id, 'detail'] as const,
  },
  buildings: {
    units: (
      buildingId: string,
      page: number,
      size: number,
      search: string | undefined,
      ownershipStatus: string | undefined,
    ) => ['buildings', buildingId, 'units', page, size, search, ownershipStatus] as const,
  },
  units: {
    detail: (unitId: string) => ['units', unitId, 'detail'] as const,
    owners: (unitId: string) => ['units', unitId, 'owners'] as const,
    installments: (unitId: string) => ['units', unitId, 'installments'] as const,
    payments: (unitId: string) => ['units', unitId, 'payments'] as const,
  },
  parties: {
    list: (propertyId: string | undefined, page: number, size: number, search: string | undefined) =>
      ['parties', 'list', propertyId, page, size, search] as const,
    detail: (id: string) => ['parties', 'detail', id] as const,
    lots: (id: string) => ['parties', id, 'lots'] as const,
  },
  invitations: {
    preview: (token: string) => ['invitations', 'preview', token] as const,
    availableUnits: (token: string) => ['invitations', token, 'available-units'] as const,
    list: (propertyId: string, page: number, size: number) =>
      ['invitations', propertyId, 'list', page, size] as const,
    membershipRequests: (propertyId: string, page: number, size: number) =>
      ['invitations', propertyId, 'membership-requests', page, size] as const,
  },
  boardMembers: {
    list: (propertyId: string) => ['board-members', propertyId, 'list'] as const,
  },
  documents: {
    list: (ownerType: string, ownerId: string, page: number, size: number) =>
      ['documents', ownerType, ownerId, 'list', page, size] as const,
  },
  messaging: {
    conversations: (page: number, size: number, search: string | undefined, box: 'RECEIVED' | 'SENT') =>
      ['messaging', 'conversations', page, size, search, box] as const,
    unreadSummary: () => ['messaging', 'unread-summary'] as const,
    messages: (conversationId: string) => ['messaging', 'conversations', conversationId, 'messages'] as const,
    recipients: (propertyId: string, search: string | undefined) =>
      ['messaging', 'recipients', propertyId, search] as const,
    drafts: (page: number, size: number, search: string | undefined) =>
      ['messaging', 'drafts', page, size, search] as const,
    draft: (draftId: string) => ['messaging', 'drafts', draftId] as const,
  },
};
