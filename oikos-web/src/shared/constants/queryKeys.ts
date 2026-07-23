export const queryKeys = {
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
      sortBy: string,
      sortDirection: string,
    ) =>
      ['properties', propertyId, 'installments', page, size, status, dueDateFrom, dueDateTo, sortBy, sortDirection] as const,
    unitTypePrices: (propertyId: string) => ['properties', propertyId, 'unit-type-prices'] as const,
    unitTypes: (propertyId: string) => ['properties', propertyId, 'unit-types'] as const,
    installmentCalls: (propertyId: string, page: number, size: number) =>
      ['properties', propertyId, 'installment-calls', page, size] as const,
  },
  installmentCalls: {
    detail: (id: string) => ['installment-calls', id, 'detail'] as const,
  },
  buildings: {
    units: (buildingId: string, page: number, size: number) =>
      ['buildings', buildingId, 'units', page, size] as const,
  },
  units: {
    detail: (unitId: string) => ['units', unitId, 'detail'] as const,
    owners: (unitId: string) => ['units', unitId, 'owners'] as const,
    installments: (unitId: string) => ['units', unitId, 'installments'] as const,
  },
  accounts: {
    byHolder: (holderId: string, accountType: string) => ['accounts', 'by-holder', holderId, accountType] as const,
    movements: (accountId: string, page: number, size: number) =>
      ['accounts', accountId, 'movements', page, size] as const,
  },
};
