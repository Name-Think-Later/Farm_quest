import { create } from 'zustand';
import { tokenStorage } from './tokenStorage';

export type ChatRole = 'ai' | 'user';

export type ChatMessage = {
  id: string;
  role: ChatRole;
  content: string;
};

export type CouponStatus = 'available' | 'used' | 'expired' | 'invalid';

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
  couponStatus: CouponStatus;
  couponConsumedAt: string | null;
  chatMessages: ChatMessage[];
  setEmail: (email: string) => void;
  requestOtp: () => void;
  verifyOtp: () => void;
  markInvalidSession: () => void;
  clearInvalidSession: () => void;
  setLocationResult: (verified: boolean, message: string, accuracyMeters: number | null) => void;
  addChatMessage: (message: ChatMessage) => void;
  completeQuest: () => void;
  consumeCoupon: () => void;
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
  couponStatus: 'available',
  couponConsumedAt: null,
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
  completeQuest: () => set({ questCompleted: true, couponStatus: 'available' }),
  consumeCoupon: () =>
    set({
      couponStatus: 'used',
      couponConsumedAt: new Date().toISOString(),
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
      couponStatus: 'available',
      couponConsumedAt: null,
      chatMessages: initialChat,
    });
  },
  hydrateFromStorage: () => {
    const token = tokenStorage.getToken();
    set({ token, verified: Boolean(token) && token !== 'invalid-token', invalidSession: token === 'invalid-token' });
  },
}));
