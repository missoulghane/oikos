export interface RegisterUserPayload {
  fullName: string;
  email: string;
  password: string;
}

export interface RegisterPropertyManagerPayload extends RegisterUserPayload {
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
