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
    contacts: (propertyId: string, page: number, size: number, search: string | undefined) =>
      ['properties', propertyId, 'contacts', page, size, search] as const,
    accountingOpenExercise: (propertyId: string) =>
      ['properties', propertyId, 'accounting', 'open-exercise'] as const,
    accountingTreasurySummary: (propertyId: string) =>
      ['properties', propertyId, 'accounting', 'treasury-summary'] as const,
    accountingFinancialAccounts: (propertyId: string) =>
      ['properties', propertyId, 'accounting', 'financial-accounts'] as const,
    accountingExpenses: (propertyId: string, page: number, size: number) =>
      ['properties', propertyId, 'accounting', 'expenses', page, size] as const,
    accountingUnitsSummary: (propertyId: string) =>
      ['properties', propertyId, 'accounting', 'units-summary'] as const,
    accountingJournal: (
      propertyId: string,
      page: number,
      size: number,
      financialAccountId: string | undefined,
      type: string | undefined,
      dateFrom: string | undefined,
      dateTo: string | undefined,
    ) =>
      ['properties', propertyId, 'accounting', 'journal', page, size, financialAccountId, type, dateFrom, dateTo] as const,
    accountingPendingLettrages: (propertyId: string) =>
      ['properties', propertyId, 'accounting', 'lettrage', 'pending'] as const,
  },
  installmentCalls: {
    detail: (id: string) => ['installment-calls', id, 'detail'] as const,
  },
  buildings: {
    units: (buildingId: string, page: number, size: number, search: string | undefined) =>
      ['buildings', buildingId, 'units', page, size, search] as const,
  },
  units: {
    detail: (unitId: string) => ['units', unitId, 'detail'] as const,
    owners: (unitId: string) => ['units', unitId, 'owners'] as const,
    installments: (unitId: string) => ['units', unitId, 'installments'] as const,
    account: (unitId: string) => ['units', unitId, 'account'] as const,
    accountMovements: (unitId: string, page: number, size: number) =>
      ['units', unitId, 'account', 'movements', page, size] as const,
    lettrageProposal: (unitId: string) => ['units', unitId, 'account', 'lettrage-proposal'] as const,
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
    list: (propertyId: string, page: number, size: number) => ['invitations', propertyId, 'list', page, size] as const,
    membershipRequests: (propertyId: string, page: number, size: number) =>
      ['invitations', propertyId, 'membership-requests', page, size] as const,
  },
  boardMembers: {
    list: (propertyId: string) => ['board-members', propertyId, 'list'] as const,
  },
};
