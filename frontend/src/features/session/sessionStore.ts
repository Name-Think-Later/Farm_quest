import { create } from 'zustand';
import type { CouponStatus } from '../coupons/types';
import { tokenStorage } from './tokenStorage';

export type ChatRole = 'ai' | 'user';

export type ChatMessage = {
  id: string;
  role: ChatRole;
  content: string;
};

type CouponState = {
  status: CouponStatus;
  consumedAt: string | null;
};

type SessionState = {
  email: string;
  token: string | null;
  verified: boolean;
  otpRequested: boolean;
  otpCooldownEndsAt: number | null;
  invalidSession: boolean;
  currentQuestId: string;
  locationVerified: boolean;
  locationMessage: string;
  accuracyMeters: number | null;
  questCompleted: boolean;
  couponStates: Record<string, CouponState>;
  chatMessages: ChatMessage[];
  setEmail: (email: string) => void;
  requestOtp: () => void;
  verifyOtp: () => void;
  markInvalidSession: () => void;
  clearInvalidSession: () => void;
  setLocationResult: (verified: boolean, message: string, accuracyMeters: number | null) => void;
  addChatMessage: (message: ChatMessage) => void;
  completeQuest: () => void;
  consumeCoupon: (couponId: string) => void;
  resetSession: () => void;
  hydrateFromStorage: () => void;
};

const initialChat: ChatMessage[] = [
  {
    id: 'ai-welcome',
    role: 'ai',
    content: '歡迎來到第一個景點。你可以直接回答，也可以先向我索取提示。',
  },
];

function createInitialCouponStates(): Record<string, CouponState> {
  return {
    'coupon-tea-001': {
      status: 'available',
      consumedAt: null,
    },
    'coupon-dessert-002': {
      status: 'used',
      consumedAt: '2026-05-18T09:30:00.000Z',
    },
    'coupon-market-003': {
      status: 'expired',
      consumedAt: null,
    },
    'coupon-gift-004': {
      status: 'invalid',
      consumedAt: null,
    },
  };
}

export const useSessionStore = create<SessionState>((set) => ({
  email: '',
  token: tokenStorage.getToken(),
  verified: Boolean(tokenStorage.getToken()),
  otpRequested: false,
  otpCooldownEndsAt: null,
  invalidSession: false,
  currentQuestId: 'tea-farm-riddle',
  locationVerified: false,
  locationMessage: '尚未進行 GPS 驗證。',
  accuracyMeters: null,
  questCompleted: false,
  couponStates: createInitialCouponStates(),
  chatMessages: initialChat,
  setEmail: (email) => set({ email }),
  requestOtp: () =>
    set({
      otpRequested: true,
      otpCooldownEndsAt: Date.now() + 60_000,
      invalidSession: false,
    }),
  verifyOtp: () => {
    const token = `mock-token-${Date.now()}`;
    tokenStorage.setToken(token);
    set({ token, verified: true, invalidSession: false });
  },
  markInvalidSession: () => {
    tokenStorage.setToken('invalid-token');
    set({ token: 'invalid-token', invalidSession: true, verified: false });
  },
  clearInvalidSession: () => {
    tokenStorage.clearToken();
    set({ token: null, invalidSession: false, verified: false });
  },
  setLocationResult: (verified, message, accuracyMeters) =>
    set({ locationVerified: verified, locationMessage: message, accuracyMeters }),
  addChatMessage: (message) => set((state) => ({ chatMessages: [...state.chatMessages, message] })),
  completeQuest: () => set({ questCompleted: true }),
  consumeCoupon: (couponId) =>
    set((state) => {
      const couponState = state.couponStates[couponId];
      if (!couponState) {
        return state;
      }

      return {
        couponStates: {
          ...state.couponStates,
          [couponId]: {
            status: 'used',
            consumedAt: new Date().toISOString(),
          },
        },
      };
    }),
  resetSession: () => {
    tokenStorage.clearToken();
    set({
      email: '',
      token: null,
      verified: false,
      otpRequested: false,
      otpCooldownEndsAt: null,
      invalidSession: false,
      currentQuestId: 'tea-farm-riddle',
      locationVerified: false,
      locationMessage: '尚未進行 GPS 驗證。',
      accuracyMeters: null,
      questCompleted: false,
      couponStates: createInitialCouponStates(),
      chatMessages: initialChat,
    });
  },
  hydrateFromStorage: () => {
    const token = tokenStorage.getToken();
    set({ token, verified: Boolean(token) && token !== 'invalid-token', invalidSession: token === 'invalid-token' });
  },
}));
