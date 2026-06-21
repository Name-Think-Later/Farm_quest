import { apiGet, apiPost } from '../../lib/http/apiClient';
import type {
  AiRiddleConversationResponse,
  AiRiddleMessagePayload,
  AiRiddleMessageResponse,
  LocationHintResponse,
  LocationVerificationPayload,
  LocationVerificationResponse,
  QuestDetailResponse,
  StartQuestResponse,
} from './types';

export async function fetchCurrentQuest() {
  return apiGet<QuestDetailResponse | null>('/api/quests/current', {
    authenticated: true,
  });
}

export async function startQuest(questId: string) {
  return apiPost<undefined, StartQuestResponse>(`/api/quests/${questId}/start`, undefined, {
    authenticated: true,
  });
}

export async function fetchLocationHint(questId: string) {
  return apiGet<LocationHintResponse>(`/api/quests/${questId}/location-hint`, {
    authenticated: true,
  });
}

export async function verifyLocation(questId: string, payload: LocationVerificationPayload) {
  return apiPost<LocationVerificationPayload, LocationVerificationResponse>(`/api/quests/${questId}/location-verifications`, payload, {
    authenticated: true,
  });
}

export async function fetchRiddleMessages(questId: string) {
  return apiGet<AiRiddleConversationResponse>(`/api/quests/${questId}/ai-riddle/messages`, {
    authenticated: true,
  });
}

export async function sendRiddleMessage(questId: string, payload: AiRiddleMessagePayload) {
  return apiPost<AiRiddleMessagePayload, AiRiddleMessageResponse>(`/api/quests/${questId}/ai-riddle/messages`, payload, {
    authenticated: true,
  });
}
