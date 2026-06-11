import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { LoadingState } from '../../components/feedback/LoadingState';
import { ErrorState } from '../../components/feedback/ErrorState';
import { InfoState } from '../../components/feedback/InfoState';
import { useCurrentQuest, useLocationHint, useStartQuest } from '../../features/quests/useQuestFlows';
import { toQuestProgress } from '../../features/quests/types';

export function CurrentQuestPage() {
  const navigate = useNavigate();
  const query = useCurrentQuest();
  const locationHintQuery = useLocationHint(query.data?.questId);
  const startMutation = useStartQuest();

  const progress = query.data ? toQuestProgress(query.data, locationHintQuery.data?.hintText) : null;

  return (
    <MobileShell
      title="任務首頁 / 目前任務"
      actions={
        query.data === null ? null : (
          <button
            type="button"
            className="primary-button"
            disabled={!progress || startMutation.isPending}
            onClick={async () => {
              if (!progress) {
                return;
              }

              if (progress.canStartQuest) {
                await startMutation.mutateAsync(progress.questId);
                navigate('/quest/location');
                return;
              }

              navigate(progress.nextRoute);
            }}
          >
            {startMutation.isPending ? '開始中…' : progress?.nextAction ?? '前往下一步'}
          </button>
        )
      }
    >
      <NetworkBanner />
      {query.isLoading ? <LoadingState message="正在讀取目前任務…" /> : null}
      {query.error ? <ErrorState message={(query.error as Error).message} onRetry={() => void query.refetch()} /> : null}
      {locationHintQuery.error ? <ErrorState message={(locationHintQuery.error as Error).message} onRetry={() => void locationHintQuery.refetch()} /> : null}
      {startMutation.error ? <ErrorState message={(startMutation.error as Error).message} /> : null}
      {!query.isLoading && !query.error && query.data === null ? <InfoState message="目前沒有進行中的任務，請自由探索" /> : null}
      {progress ? (
        <>
          <div className="section-card primary-feature-card">
            <p className="section-kicker">{progress.stepLabel}</p>
            <strong className="feature-title">{progress.title}</strong>
            <p className="feature-copy">{progress.description}</p>
          </div>
          <div className="section-card split-card">
            <div>
              <p className="label-text">任務進度</p>
              <strong className="value-text">第 {progress.currentStep} / {progress.totalSteps} 步</strong>
            </div>
            <div>
              <p className="label-text">目前狀態</p>
              <strong className="value-text">{progress.stateText}</strong>
            </div>
            <div>
              <p className="label-text">前往提示</p>
              <p className="value-paragraph">{progress.locationHint}</p>
            </div>
          </div>
        </>
      ) : null}
    </MobileShell>
  );
}
