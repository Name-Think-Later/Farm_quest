import type { GameEntryResponse, GameStateResponse } from './types';

export async function getMockGameEntry(): Promise<GameEntryResponse> {
  await new Promise((resolve) => setTimeout(resolve, 120));
  return {
    gameId: 'mock-game-id',
    code: 'farm-quest-mock',
    name: '農遊謎走',
    entryPath: '/game',
    startsAt: new Date().toISOString(),
    endsAt: new Date(Date.now() + 7 * 24 * 60 * 60_000).toISOString(),
  };
}

export async function getMockGameState(): Promise<GameStateResponse> {
  await new Promise((resolve) => setTimeout(resolve, 120));
  return {
    gameId: 'mock-game-id',
    visitorAccountId: 'mock-visitor-id',
    currentQuestId: 'mock-quest-id',
    currentQuestTitle: '第一關任務',
    progressStatus: 'NOT_STARTED',
    gpsVerified: false,
    aiRiddleAvailable: false,
    nextStep: 'START_QUEST',
  };
}
