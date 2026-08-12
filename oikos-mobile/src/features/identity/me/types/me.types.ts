export type PropertyRoleName =
  | 'PROPERTY_BOARD_ADMIN'
  | 'PROPERTY_BOARD_MEMBER'
  | 'PROPERTY_MANAGER_ADMIN'
  | 'PROPERTY_MANAGER_MEMBER'
  | 'PROPERTY_OWNER';

export interface CurrentUser {
  id: string;
  fullName: string;
  email: string;
  phone: string | null;
  roles: string[];
  roleByProperty: Record<string, PropertyRoleName[]>;
  verified: boolean;
  enabled: boolean;
  hasAvatar: boolean;
}
