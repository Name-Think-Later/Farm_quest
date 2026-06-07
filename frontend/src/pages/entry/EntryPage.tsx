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
  const markInvalidSession = useSessionStore((state) => state.markInvalidSession);
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
      description={entry?.description ?? '掃描入口後開始遊戲。'}
      actions={
        <div className="inline-row">
          <button
            type="button"
            className="secondary-button"
            onClick={() => {
              markInvalidSession();
              void stateQuery.refetch();
            }}
          >
            模擬 token 無效
          </button>
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
        </div>
      }
    >
      <NetworkBanner />
      {isLoading ? <LoadingState message="正在載入遊戲入口資訊…" /> : null}
      {error ? <ErrorState message={(error as Error).message} onRetry={() => { void entryQuery.refetch(); void stateQuery.refetch(); }} /> : null}
      {entry ? (
        <div className="section-card">
          <strong>開始前提醒</strong>
          <p>{entry.networkHint}</p>
        </div>
      ) : null}
      {state?.invalidSession ? (
        <div className="status-card status-error">
          <strong>目前 session 已失效</strong>
          <p>請重新開始遊戲，再次進入 Email 驗證流程。</p>
        </div>
      ) : null}
      <div className="section-card">
        <strong>階段 1 定義的頁面流程</strong>
        <ul className="info-list">
          <li>1. 入口頁</li>
          <li>2. Email 登入與 OTP 驗證</li>
          <li>3. 目前任務頁</li>
          <li>4. GPS 地點驗證</li>
          <li>5. AI 對話猜謎</li>
          <li>6. 優惠券查看與使用</li>
        </ul>
      </div>
    </MobileShell>
  );
}
