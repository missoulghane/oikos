export interface RegisterUserPayload {
  lastName: string;
  firstName: string;
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
