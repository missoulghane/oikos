export interface RegisterUserPayload {
  fullName: string;
  email: string;
  password: string;
  returnTo?: string;
  invitationToken?: string;
  unitId?: string;
}

export interface RegisterPropertyAdminPayload extends RegisterUserPayload {
  phone?: string;
  propertyName: string;
  propertyAddress: string;
}

export interface VerifyAccountPayload {
  token: string;
}

export interface ActivateAccountPayload {
  token: string;
  newPassword: string;
}

export interface AcceptInvitationPayload {
  token: string;
  password?: string;
}

export interface MessageResponse {
  message: string;
}
