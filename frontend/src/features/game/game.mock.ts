import { useSessionStore } from '../session/sessionStore';
import { GameEntry, GameState } from './types';

export async function getMockGameEntry(): Promise<GameEntry> {
  await new Promise((resolve) => setTimeout(resolve, 120));
  return {
    name: '農遊謎走',
    description: '掃描入口後開始戶外探索，依序完成 Email 驗證、GPS 驗證、AI 對話猜謎與優惠券領取。',
    networkHint: '若網路不穩，已載入的任務文字仍可閱讀，但驗證與 AI 對話需要連線。',
  };
}

export async function getMockGameState(): Promise<GameState> {
  await new Promise((resolve) => setTimeout(resolve, 120));
  const state = useSessionStore.getState();

  if (state.invalidSession || state.token === 'invalid-token') {
    return {
      hasSession: false,
      invalidSession: true,
      nextRoute: '/auth/email',
      ctaLabel: '重新開始',
    };
  }

  if (state.verified && state.token) {
    return {
      hasSession: true,
      invalidSession: false,
      nextRoute: '/quest/current',
      ctaLabel: '繼續遊玩',
    };
  }

  return {
    hasSession: false,
    invalidSession: false,
    nextRoute: '/auth/email',
    ctaLabel: '開始遊戲',
  };
}
