export const queryKeys = {
  properties: {
    list: (page: number, size: number) => ['properties', 'list', page, size] as const,
    detail: (id: string) => ['properties', 'detail', id] as const,
    buildings: (propertyId: string, page: number, size: number) =>
      ['properties', propertyId, 'buildings', page, size] as const,
  },
  buildings: {
    units: (buildingId: string, page: number, size: number) =>
      ['buildings', buildingId, 'units', page, size] as const,
  },
};
