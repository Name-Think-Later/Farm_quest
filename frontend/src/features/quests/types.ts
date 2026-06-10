export type QuestDetailResponse = {
  questId: string;
  title: string;
  description: string;
  sortOrder: number;
  status: string;
  startedAt: string | null;
  locationVerifiedAt: string | null;
  current: boolean;
  nextStep: string;
};

export type StartQuestResponse = {
  questId: string;
  status: string;
  startedAt: string;
  nextStep: string;
};

export type LocationHintResponse = {
  questId: string;
  locationId: string;
  locationName: string;
  hintText: string;
  radiusMeters: number;
  maxAccuracyMeters: number;
};

export type LocationVerificationPayload = {
  permissionDenied: boolean;
  latitude?: number;
  longitude?: number;
  accuracyMeters?: number;
};

export type LocationVerificationResponse = {
  questId: string;
  status: string;
  passed: boolean;
  distanceMeters: number;
  accuracyMeters: number;
  locationVerifiedAt: string;
  nextStep: string;
};

export type AiRiddleMessage = {
  messageId: string;
  role: 'VISITOR' | 'ASSISTANT';
  content: string;
  answerCorrect: boolean | null;
  createdAt: string;
};

export type AiRiddleConversationResponse = {
  questId: string;
  conversationId: string | null;
  status: string;
  questCompleted: boolean;
  nextStep: string;
  messages: AiRiddleMessage[];
};

export type AiRiddleMessagePayload = {
  content: string;
};

export type AiRiddleMessageResponse = {
  questId: string;
  conversationId: string;
  status: string;
  replyContent: string;
  correct: boolean;
  questCompleted: boolean;
  nextStep: string;
  safeMessage: string | null;
};

export type QuestProgress = {
  questId: string;
  title: string;
  stepLabel: string;
  description: string;
  nextAction: string;
  locationHint: string;
  stateText: string;
  currentStep: number;
  totalSteps: number;
  nextRoute: '/quest/current' | '/quest/location' | '/quest/riddle' | '/coupons/current';
  canStartQuest: boolean;
};

export type ChatMessage = {
  id: string;
  role: 'ai' | 'user';
  content: string;
};

export function toQuestProgress(
  quest: QuestDetailResponse,
  locationHintText?: string,
): QuestProgress {
  if (quest.status === 'COMPLETED') {
    return {
      questId: quest.questId,
      title: quest.title,
      stepLabel: '目前任務',
      description: quest.description,
      nextAction: '前往優惠券頁',
      locationHint: '任務已完成，不需再次定位。',
      stateText: '已完成景點任務',
      currentStep: 3,
      totalSteps: 3,
      nextRoute: '/coupons/current',
      canStartQuest: false,
    };
  }

  if (quest.nextStep === 'AI_RIDDLE_AVAILABLE' || quest.status === 'LOCATION_VERIFIED' || quest.status === 'AI_RIDDLE_STARTED') {
    return {
      questId: quest.questId,
      title: quest.title,
      stepLabel: '目前任務',
      description: quest.description,
      nextAction: '前往 AI 對話頁作答',
      locationHint: locationHintText ?? '你已抵達正確地點，可以開始對話。',
      stateText: '可開始 AI 猜謎',
      currentStep: 3,
      totalSteps: 3,
      nextRoute: '/quest/riddle',
      canStartQuest: false,
    };
  }

  if (quest.nextStep === 'VERIFY_LOCATION' || quest.status === 'STARTED') {
    return {
      questId: quest.questId,
      title: quest.title,
      stepLabel: '目前任務',
      description: quest.description,
      nextAction: '先完成地點驗證',
      locationHint: locationHintText ?? '請前往指定景點附近完成 GPS 驗證。',
      stateText: '尚未完成 GPS 驗證',
      currentStep: 2,
      totalSteps: 3,
      nextRoute: '/quest/location',
      canStartQuest: false,
    };
  }

  return {
    questId: quest.questId,
    title: quest.title,
    stepLabel: '目前任務',
    description: quest.description,
    nextAction: '開始任務',
    locationHint: locationHintText ?? '開始任務後即可前往指定景點進行驗證。',
    stateText: '尚未開始任務',
    currentStep: 1,
    totalSteps: 3,
    nextRoute: '/quest/current',
    canStartQuest: true,
  };
}

export function toChatMessages(messages: AiRiddleMessage[]): ChatMessage[] {
  return messages.map((message) => ({
    id: message.messageId,
    role: message.role === 'ASSISTANT' ? 'ai' : 'user',
    content: message.content,
  }));
}
