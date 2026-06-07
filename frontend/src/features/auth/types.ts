export type SendOtpPayload = {
  email: string;
};

export type VerifyOtpPayload = {
  email: string;
  otp: string;
};

export type OtpResponse = {
  success: boolean;
  message: string;
};
