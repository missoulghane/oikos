export interface CurrentUser {
  id: string;
  fullName: string;
  email: string;
  roles: string[];
  managedPropertyIds: string[];
  verified: boolean;
  enabled: boolean;
}

export interface OwnedUnit {
  unitId: string;
  unitNumber: string;
  buildingId: string;
  buildingName: string;
  propertyId: string;
  propertyName: string;
  ownershipShare: number;
}
