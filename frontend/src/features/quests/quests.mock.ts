import type {
  AiRiddleConversationResponse,
  AiRiddleMessageResponse,
  LocationHintResponse,
  LocationVerificationResponse,
  QuestDetailResponse,
  StartQuestResponse,
} from './types';

export async function getMockCurrentQuest(): Promise<QuestDetailResponse> {
  await new Promise((resolve) => setTimeout(resolve, 180));
  return {
    questId: 'mock-quest-id',
    title: '第一關任務',
    description: '前往茶園入口，完成 GPS 驗證後才能進入 AI 對話猜謎。',
    sortOrder: 1,
    status: 'NOT_STARTED',
    startedAt: null,
    locationVerifiedAt: null,
    current: true,
    nextStep: 'START_QUEST',
  };
}

export async function startMockQuest(): Promise<StartQuestResponse> {
  await new Promise((resolve) => setTimeout(resolve, 180));
  return {
    questId: 'mock-quest-id',
    status: 'STARTED',
    startedAt: new Date().toISOString(),
    nextStep: 'VERIFY_LOCATION',
  };
}

export async function getMockLocationHint(): Promise<LocationHintResponse> {
  await new Promise((resolve) => setTimeout(resolve, 120));
  return {
    questId: 'mock-quest-id',
    locationId: 'mock-location-id',
    locationName: '茶園入口',
    hintText: '請尋找茶園入口木牌附近的空曠區域。',
    radiusMeters: 30,
    maxAccuracyMeters: 50,
  };
}

export async function verifyMockLocation(): Promise<LocationVerificationResponse> {
  await new Promise((resolve) => setTimeout(resolve, 220));
  return {
    questId: 'mock-quest-id',
    status: 'LOCATION_VERIFIED',
    passed: true,
    distanceMeters: 8.5,
    accuracyMeters: 18,
    locationVerifiedAt: new Date().toISOString(),
    nextStep: 'AI_RIDDLE_AVAILABLE',
  };
}

export async function getMockRiddleMessages(): Promise<AiRiddleConversationResponse> {
  await new Promise((resolve) => setTimeout(resolve, 180));
  return {
    questId: 'mock-quest-id',
    conversationId: 'mock-conversation-id',
    status: 'AI_RIDDLE_STARTED',
    questCompleted: false,
    nextStep: 'AI_RIDDLE_AVAILABLE',
    messages: [
      {
        messageId: 'mock-assistant-message',
        role: 'ASSISTANT',
        content: '歡迎來到第一個景點。你可以直接回答，也可以先向我索取提示。',
        answerCorrect: null,
        createdAt: new Date().toISOString(),
      },
    ],
  };
}

export async function sendMockRiddleMessage(): Promise<AiRiddleMessageResponse> {
  await new Promise((resolve) => setTimeout(resolve, 250));
  return {
    questId: 'mock-quest-id',
    conversationId: 'mock-conversation-id',
    status: 'AI_RIDDLE_STARTED',
    replyContent: '這個答案還不夠接近，你可以再想想，或直接向我索取提示。',
    correct: false,
    questCompleted: false,
    nextStep: 'AI_RIDDLE_AVAILABLE',
    safeMessage: '請依照線索繼續作答。',
  };
}
