import { apiGet } from '../../lib/http/apiClient';
import { useSessionStore } from '../session/sessionStore';
import type { EntryActionState, GameEntryResponse, GameStateResponse } from './types';

export async function fetchGameEntry() {
  return apiGet<GameEntryResponse>('/api/game');
}

export async function fetchGameState() {
  return apiGet<GameStateResponse>('/api/game/state', {
    authenticated: true,
  });
}

export function resolveEntryAction(gameState: GameStateResponse | null, invalidSession: boolean): EntryActionState {
  if (invalidSession) {
    return {
      hasSession: false,
      invalidSession: true,
      nextRoute: '/auth/email',
      ctaLabel: '重新開始',
      headline: '目前 session 已失效',
      description: '請重新開始遊戲，再次進入 Email 驗證流程。',
    };
  }

  const token = useSessionStore.getState().token;
  if (!token || !gameState) {
    return {
      hasSession: false,
      invalidSession: false,
      nextRoute: '/auth/email',
      ctaLabel: '前往 Email 登入',
      headline: '下一步：先完成 Email 登入',
      description: '登入後才能進入任務、GPS 驗證、AI 猜謎與優惠券流程。',
    };
  }

  if (gameState.progressStatus === 'COMPLETED') {
    return {
      hasSession: true,
      invalidSession: false,
      nextRoute: '/coupons/current',
      ctaLabel: '查看優惠券',
      headline: '你已完成目前任務',
      description: '可以前往優惠券頁查看已取得的優惠券。',
    };
  }

  if (gameState.nextStep === 'VERIFY_LOCATION') {
    return {
      hasSession: true,
      invalidSession: false,
      nextRoute: '/quest/location',
      ctaLabel: '前往地點驗證',
      headline: '你可以繼續目前任務',
      description: '系統已保留你的登入狀態，下一步請完成 GPS 驗證。',
    };
  }

  if (gameState.nextStep === 'AI_RIDDLE_AVAILABLE') {
    return {
      hasSession: true,
      invalidSession: false,
      nextRoute: '/quest/riddle',
      ctaLabel: '前往 AI 猜謎',
      headline: '你可以繼續目前任務',
      description: 'GPS 驗證已完成，現在可以進入 AI 對話猜謎。',
    };
  }

  return {
    hasSession: true,
    invalidSession: false,
    nextRoute: '/quest/current',
    ctaLabel: '開始任務',
    headline: '你可以直接繼續目前任務',
    description: '系統已保留你的登入狀態，可直接回到任務流程。',
  };
}
