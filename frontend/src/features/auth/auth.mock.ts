import { useSessionStore } from '../session/sessionStore';
import type { EmailVerificationResponse, SendOtpPayload, VerifyOtpPayload, VisitorSessionResponse } from './types';

export async function sendMockOtp({ email }: SendOtpPayload): Promise<EmailVerificationResponse> {
  await new Promise((resolve) => setTimeout(resolve, 250));
  useSessionStore.getState().setEmail(email);
  useSessionStore.getState().requestOtp();
  return {
    email,
    expiresAt: new Date(Date.now() + 5 * 60_000).toISOString(),
    resendAvailableAt: new Date(Date.now() + 60_000).toISOString(),
    status: 'PENDING',
  };
}

export async function verifyMockOtp({ email, otp }: VerifyOtpPayload): Promise<VisitorSessionResponse> {
  await new Promise((resolve) => setTimeout(resolve, 250));

  if (otp !== '123456') {
    throw new Error('驗證失敗，請確認驗證碼後再試一次。');
  }

  const sessionToken = `mock-token-${Date.now()}`;
  useSessionStore.getState().authenticate(sessionToken, email);
  return {
    visitorAccountId: 'mock-visitor-id',
    email,
    sessionToken,
    issuedAt: new Date().toISOString(),
    expiresAt: new Date(Date.now() + 8 * 60 * 60_000).toISOString(),
    authenticated: true,
  };
}
