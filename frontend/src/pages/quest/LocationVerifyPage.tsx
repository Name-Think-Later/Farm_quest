import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { ErrorState } from '../../components/feedback/ErrorState';
import { useVerifyLocation } from '../../features/quests/useQuestFlows';

export function LocationVerifyPage() {
  const navigate = useNavigate();
  const [accuracyMeters, setAccuracyMeters] = useState('18');
  const [permissionDenied, setPermissionDenied] = useState(false);
  const mutation = useVerifyLocation();

  const accuracyNumber = useMemo(() => Number(accuracyMeters || '0'), [accuracyMeters]);

  return (
    <MobileShell
      title="地點驗證"
      description="GPS 是強制條件，未通過不能進入後續謎題。"
      actions={
        <div className="inline-row">
          <button type="button" className="secondary-button" onClick={() => setPermissionDenied(true)}>
            模擬拒絕 GPS
          </button>
          <button type="button" className="primary-button" onClick={() => void mutation.mutateAsync(accuracyNumber)} disabled={mutation.isPending}>
            {mutation.isPending ? '定位中…' : '重新定位 / 驗證'}
          </button>
        </div>
      }
    >
      <NetworkBanner />
      <div className="section-card">
        <div className="field-group">
          <label className="field-label" htmlFor="accuracy">目前 accuracyMeters</label>
          <input id="accuracy" className="input-field" inputMode="numeric" value={accuracyMeters} onChange={(event) => setAccuracyMeters(event.target.value.replace(/\D/g, ''))} />
          <p className="helper-text">大於 50 會顯示精度不足，提醒重新定位。</p>
        </div>
      </div>
      {permissionDenied ? <ErrorState title="需要 GPS 權限" message="你已拒絕 GPS 權限，必須開啟定位後才能繼續遊戲。" onRetry={() => setPermissionDenied(false)} /> : null}
      {mutation.data?.ok ? (
        <div className="status-card">
          <strong>{mutation.data.data.verified ? '驗證通過' : '驗證未通過'}</strong>
          <p>{mutation.data.data.message}</p>
          <p>{mutation.data.data.distanceText}</p>
          <p className="helper-text">accuracy: {mutation.data.data.accuracyMeters}m</p>
        </div>
      ) : null}
      <button type="button" className="text-button" onClick={() => navigate(mutation.data?.ok && mutation.data.data.verified ? '/quest/riddle' : '/quest/current')}>
        {mutation.data?.ok && mutation.data.data.verified ? '前往 AI 對話頁' : '返回任務頁'}
      </button>
    </MobileShell>
  );
}
