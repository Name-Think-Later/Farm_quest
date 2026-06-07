import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { LoadingState } from '../../components/feedback/LoadingState';
import { ErrorState } from '../../components/feedback/ErrorState';
import { useCurrentQuest } from '../../features/quests/useQuestFlows';

export function CurrentQuestPage() {
  const navigate = useNavigate();
  const query = useCurrentQuest();

  const action = query.data?.currentStep === 1 ? '/quest/location' : query.data?.currentStep === 2 ? '/quest/riddle' : '/coupons/current';

  return (
    <MobileShell
      title="任務首頁 / 目前任務"
      description="讓遊客立刻知道現在要去哪裡、做什麼。"
      actions={<button type="button" className="primary-button" onClick={() => navigate(action)} disabled={!query.data}>{query.data?.nextAction ?? '前往下一步'}</button>}
    >
      <NetworkBanner />
      {query.isLoading ? <LoadingState message="正在讀取目前任務…" /> : null}
      {query.error ? <ErrorState message={(query.error as Error).message} onRetry={() => void query.refetch()} /> : null}
      {query.data ? (
        <>
          <div className="section-card">
            <strong>{query.data.stepLabel}</strong>
            <p>{query.data.description}</p>
          </div>
          <div className="section-card">
            <ul className="info-list">
              <li>任務進度：第 {query.data.currentStep} / {query.data.totalSteps} 步</li>
              <li>任務狀態：{query.data.stateText}</li>
              <li>前往地點提示：{query.data.locationHint}</li>
            </ul>
          </div>
        </>
      ) : null}
    </MobileShell>
  );
}
