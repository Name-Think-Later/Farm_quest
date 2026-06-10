import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { LoadingState } from '../../components/feedback/LoadingState';
import { ErrorState } from '../../components/feedback/ErrorState';
import { fetchGameEntry, fetchGameState, resolveEntryAction } from '../../features/game/game.api';
import { useSessionStore } from '../../features/session/sessionStore';

const networkHint = '若網路不穩，已載入的任務文字仍可閱讀，但驗證與 AI 對話需要連線。';

export function EntryPage() {
  const navigate = useNavigate();
  const resetSession = useSessionStore((state) => state.resetSession);
  const token = useSessionStore((state) => state.token);
  const invalidSession = useSessionStore((state) => state.invalidSession);

  const entryQuery = useQuery({
    queryKey: ['game-entry'],
    queryFn: async () => {
      const result = await fetchGameEntry();
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
  });

  const stateQuery = useQuery({
    queryKey: ['game-state', token],
    enabled: Boolean(token),
    queryFn: async () => {
      const result = await fetchGameState();
      if (!result.ok) {
        if (result.code === 'SESSION_INVALID' || result.code === 'SESSION_EXPIRED') {
          resetSession();
        }
        throw new Error(result.message);
      }
      return result.data;
    },
  });

  const isLoading = entryQuery.isLoading || (Boolean(token) && stateQuery.isLoading);
  const error = entryQuery.error ?? stateQuery.error;
  const entry = entryQuery.data;
  const state = stateQuery.data ?? null;
  const action = resolveEntryAction(state, invalidSession);

  return (
    <MobileShell
      title={entry?.name ?? '農遊謎走'}
      actions={
        <button
          type="button"
          className="primary-button"
          onClick={() => navigate(action.nextRoute)}
          disabled={isLoading || Boolean(error)}
        >
          {action.ctaLabel}
        </button>
      }
    >
      <NetworkBanner />
      {isLoading ? <LoadingState message="正在載入遊戲入口資訊…" /> : null}
      {error ? <ErrorState message={(error as Error).message} onRetry={() => { void entryQuery.refetch(); void stateQuery.refetch(); }} /> : null}
      {entry ? (
        <div className="section-card primary-feature-card">
          <p className="section-kicker">開始前提醒</p>
          <strong className="feature-title">準備出發</strong>
          <p className="feature-copy">{networkHint}</p>
        </div>
      ) : null}
      {action.invalidSession ? (
        <div className="status-card status-error">
          <strong>{action.headline}</strong>
          <p>{action.description}</p>
        </div>
      ) : (
        <div className="status-card">
          <strong>{action.headline}</strong>
          <p>{action.description}</p>
        </div>
      )}
      <div className="hero-card page-hero-card">
        <p className="hero-kicker">走進茶園，跟著提示一步步探索</p>
        <strong className="feature-title">從登入開始今天的探索旅程</strong>
        <p className="hero-body">掃描入口後，先完成 Email 登入與 OTP 驗證，再依序進入任務、GPS 驗證、AI 猜謎與優惠券領取。</p>
      </div>
    </MobileShell>
  );
}
