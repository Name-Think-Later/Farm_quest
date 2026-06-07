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
      actions={<button type="button" className="primary-button" onClick={() => navigate(action)} disabled={!query.data}>{query.data?.nextAction ?? '前往下一步'}</button>}
    >
      <NetworkBanner />
      {query.isLoading ? <LoadingState message="正在讀取目前任務…" /> : null}
      {query.error ? <ErrorState message={(query.error as Error).message} onRetry={() => void query.refetch()} /> : null}
      {query.data ? (
        <>
          <div className="section-card primary-feature-card">
            <p className="section-kicker">{query.data.stepLabel}</p>
            <strong className="feature-title">{query.data.stateText}</strong>
            <p className="feature-copy">{query.data.description}</p>
          </div>
          <div className="section-card split-card">
            <div>
              <p className="label-text">任務進度</p>
              <strong className="value-text">第 {query.data.currentStep} / {query.data.totalSteps} 步</strong>
            </div>
            <div>
              <p className="label-text">目前狀態</p>
              <strong className="value-text">{query.data.stateText}</strong>
            </div>
            <div>
              <p className="label-text">前往提示</p>
              <p className="value-paragraph">{query.data.locationHint}</p>
            </div>
          </div>
        </>
      ) : null}
    </MobileShell>
  );
}
