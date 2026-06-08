import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { LoadingState } from '../../components/feedback/LoadingState';
import { ErrorState } from '../../components/feedback/ErrorState';
import { fetchGameEntry, fetchGameState } from '../../features/game/game.api';
import { useSessionStore } from '../../features/session/sessionStore';

export function EntryPage() {
  const navigate = useNavigate();
  const resetSession = useSessionStore((state) => state.resetSession);

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
    queryKey: ['game-state'],
    queryFn: async () => {
      const result = await fetchGameState();
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
  });

  const isLoading = entryQuery.isLoading || stateQuery.isLoading;
  const error = entryQuery.error ?? stateQuery.error;
  const entry = entryQuery.data;
  const state = stateQuery.data;

  return (
    <MobileShell
      title={entry?.name ?? '農遊謎走'}
      actions={
        <button
          type="button"
          className="primary-button"
          onClick={() => {
            if (!state) return;
            if (state.invalidSession) {
              resetSession();
            }
            navigate(state.nextRoute);
          }}
        >
          {state?.ctaLabel ?? '開始'}
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
          <p className="feature-copy">{entry.networkHint}</p>
        </div>
      ) : null}
      {state?.invalidSession ? (
        <div className="status-card status-error">
          <strong>目前 session 已失效</strong>
          <p>請重新開始遊戲，再次進入 Email 驗證流程。</p>
        </div>
      ) : null}
      <div className="hero-card page-hero-card">
        <p className="hero-kicker">走進茶園，跟著提示一步步探索</p>
        <strong className="feature-title">開始今天的探索旅程</strong>
        <p className="hero-body">掃描入口後開始戶外探索，依序完成 Email 驗證、GPS 確認、AI 猜謎與優惠券領取。</p>
      </div>
    </MobileShell>
  );
}
