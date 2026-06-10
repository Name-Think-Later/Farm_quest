export type SendOtpPayload = {
  email: string;
};

export type VerifyOtpPayload = {
  email: string;
  otp: string;
};

export type EmailVerificationResponse = {
  email: string;
  expiresAt: string;
  resendAvailableAt: string;
  status: string;
};

export type VisitorSessionResponse = {
  visitorAccountId: string;
  email: string;
  sessionToken: string | null;
  issuedAt: string;
  expiresAt: string;
  authenticated: boolean;
};

export type CurrentUserResponse = {
  authenticated: boolean;
  visitorAccountId: string;
  email: string;
  sessionExpiresAt: string;
};

export type LogoutResponse = {
  success: boolean;
};
