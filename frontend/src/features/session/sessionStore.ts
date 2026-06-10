import { create } from 'zustand';
import { tokenStorage } from './tokenStorage';

type SessionState = {
  email: string;
  token: string | null;
  isAuthenticated: boolean;
  sessionChecked: boolean;
  otpRequested: boolean;
  otpCooldownEndsAt: number | null;
  invalidSession: boolean;
  setEmail: (email: string) => void;
  requestOtp: (resendAvailableAt?: string) => void;
  authenticate: (token: string, email?: string) => void;
  markSessionChecked: (authenticated: boolean) => void;
  markInvalidSession: () => void;
  clearInvalidSession: () => void;
  resetSession: () => void;
  hydrateFromStorage: () => void;
};

function cooldownFromTimestamp(resendAvailableAt?: string) {
  if (!resendAvailableAt) {
    return Date.now() + 60_000;
  }

  const parsed = new Date(resendAvailableAt).getTime();
  return Number.isNaN(parsed) ? Date.now() + 60_000 : parsed;
}

export const useSessionStore = create<SessionState>((set) => ({
  email: '',
  token: tokenStorage.getToken(),
  isAuthenticated: false,
  sessionChecked: false,
  otpRequested: false,
  otpCooldownEndsAt: null,
  invalidSession: false,
  setEmail: (email) => set({ email }),
  requestOtp: (resendAvailableAt) =>
    set({
      otpRequested: true,
      otpCooldownEndsAt: cooldownFromTimestamp(resendAvailableAt),
      invalidSession: false,
    }),
  authenticate: (token, email) => {
    tokenStorage.setToken(token);
    set({
      token,
      email: email ?? '',
      isAuthenticated: true,
      sessionChecked: true,
      invalidSession: false,
    });
  },
  markSessionChecked: (authenticated) =>
    set((state) => ({
      isAuthenticated: authenticated,
      sessionChecked: true,
      invalidSession: authenticated ? false : state.invalidSession,
    })),
  markInvalidSession: () => {
    tokenStorage.clearToken();
    set({
      token: null,
      isAuthenticated: false,
      sessionChecked: true,
      invalidSession: true,
    });
  },
  clearInvalidSession: () =>
    set({
      invalidSession: false,
    }),
  resetSession: () => {
    tokenStorage.clearToken();
    set({
      email: '',
      token: null,
      isAuthenticated: false,
      sessionChecked: true,
      otpRequested: false,
      otpCooldownEndsAt: null,
      invalidSession: false,
    });
  },
  hydrateFromStorage: () => {
    const token = tokenStorage.getToken();
    set({
      token,
      isAuthenticated: false,
      sessionChecked: false,
      invalidSession: false,
    });
  },
}));
