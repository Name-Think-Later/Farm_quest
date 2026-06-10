import { apiGet, apiPost } from '../../lib/http/apiClient';
import type {
  CurrentUserResponse,
  EmailVerificationResponse,
  LogoutResponse,
  SendOtpPayload,
  VerifyOtpPayload,
  VisitorSessionResponse,
} from './types';

export async function sendOtp(payload: SendOtpPayload) {
  return apiPost<SendOtpPayload, EmailVerificationResponse>('/api/auth/visitor/email-verifications', payload);
}

export async function verifyOtp(payload: VerifyOtpPayload) {
  return apiPost<VerifyOtpPayload, VisitorSessionResponse>('/api/auth/visitor/email-verifications/confirm', payload);
}

export async function fetchVisitorSession() {
  return apiGet<VisitorSessionResponse>('/api/auth/visitor/session', {
    authenticated: true,
  });
}

export async function fetchCurrentUser() {
  return apiGet<CurrentUserResponse>('/api/auth/me', {
    authenticated: true,
  });
}

export async function logout() {
  return apiPost<undefined, LogoutResponse>('/api/auth/logout', undefined, {
    authenticated: true,
  });
}
