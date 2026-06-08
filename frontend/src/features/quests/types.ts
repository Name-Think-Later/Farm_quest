import type { CouponStatus } from '../coupons/types';
import type { ChatMessage } from '../session/sessionStore';

export type QuestProgress = {
  stepLabel: string;
  description: string;
  nextAction: string;
  locationHint: string;
  stateText: string;
  currentStep: number;
  totalSteps: number;
};

export type LocationVerificationResponse = {
  verified: boolean;
  message: string;
  distanceText: string;
  accuracyMeters: number;
};

export type RiddleResponse = {
  reply: ChatMessage;
  answeredCorrectly: boolean;
  rewardMessage?: string;
};

export type CouponPreview = {
  title: string;
  merchant: string;
  expiresAt: string;
  status: CouponStatus;
  description: string;
};
