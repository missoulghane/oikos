export interface RegisterUserPayload {
  fullName: string;
  email: string;
  phone?: string;
  password: string;
}

export interface RegisterPropertyManagerPayload extends RegisterUserPayload {
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

export interface MessageResponse {
  message: string;
}
