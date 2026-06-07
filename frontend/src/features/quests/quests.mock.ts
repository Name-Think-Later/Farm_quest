import { ChatMessage, useSessionStore } from '../session/sessionStore';
import { CouponPreview, LocationVerificationResponse, QuestProgress, RiddleResponse } from './types';

export async function getMockCurrentQuest(): Promise<QuestProgress> {
  await new Promise((resolve) => setTimeout(resolve, 180));
  const state = useSessionStore.getState();

  if (!state.locationVerified) {
    return {
      stepLabel: '目前任務',
      description: '前往茶園入口，完成 GPS 驗證後才能進入 AI 對話猜謎。',
      nextAction: '先完成地點驗證',
      locationHint: '請尋找茶園入口木牌附近的空曠區域。',
      stateText: '尚未完成 GPS 驗證',
      currentStep: 1,
      totalSteps: 3,
    };
  }

  if (!state.questCompleted) {
    return {
      stepLabel: '目前任務',
      description: 'GPS 驗證完成，接下來與 AI NPC 對話並回答謎題。',
      nextAction: '前往 AI 對話頁作答',
      locationHint: '你已抵達正確地點，可以開始對話。',
      stateText: '可開始 AI 猜謎',
      currentStep: 2,
      totalSteps: 3,
    };
  }

  return {
    stepLabel: '目前任務',
    description: '景點任務已完成，可以前往優惠券頁查看與使用。',
    nextAction: '前往優惠券頁',
    locationHint: '任務已完成，不需再次定位。',
    stateText: '已完成景點任務',
    currentStep: 3,
    totalSteps: 3,
  };
}

export async function verifyMockLocation(): Promise<LocationVerificationResponse> {
  await new Promise((resolve) => setTimeout(resolve, 220));

  const accuracyMeters = 18;
  useSessionStore.getState().setLocationResult(true, 'GPS 驗證通過，可以進入後續謎題。', accuracyMeters);
  return {
    verified: true,
    message: 'GPS 驗證通過，可以進入後續謎題。',
    distanceText: '已確認你在任務點範圍內，現在可以前往 AI 對話頁。',
    accuracyMeters,
  };
}

export async function sendMockRiddleMessage(input: string): Promise<RiddleResponse> {
  await new Promise((resolve) => setTimeout(resolve, 250));

  const lower = input.trim().toLowerCase();
  const state = useSessionStore.getState();
  const userMessage: ChatMessage = {
    id: `user-${Date.now()}`,
    role: 'user',
    content: input,
  };
  state.addChatMessage(userMessage);

  if (lower.includes('提示')) {
    const reply = {
      id: `ai-${Date.now()}`,
      role: 'ai' as const,
      content: '提示：答案和茶葉的生長環境有關，想想高山、濕度與日夜溫差。',
    };
    state.addChatMessage(reply);
    return { reply, answeredCorrectly: false };
  }

  if (lower.includes('高山茶')) {
    const reply = {
      id: `ai-${Date.now()}`,
      role: 'ai' as const,
      content: '答對了，你已完成景點任務並取得優惠券。',
    };
    state.addChatMessage(reply);
    state.completeQuest();
    return {
      reply,
      answeredCorrectly: true,
      rewardMessage: '已完成景點任務並取得優惠券。',
    };
  }

  const reply = {
    id: `ai-${Date.now()}`,
    role: 'ai' as const,
    content: '這個答案還不夠接近，你可以再想想，或直接向我索取提示。',
  };
  state.addChatMessage(reply);
  return { reply, answeredCorrectly: false };
}

export async function getMockCouponPreview(): Promise<CouponPreview> {
  await new Promise((resolve) => setTimeout(resolve, 120));
  const state = useSessionStore.getState();
  return {
    title: '茶園體驗折價券',
    merchant: '山霧茶屋',
    expiresAt: '2026-12-31 18:00',
    status: state.couponStatus,
    description: '請向店家出示本券內容，店家確認後再由你按下使用按鈕。',
  };
}
