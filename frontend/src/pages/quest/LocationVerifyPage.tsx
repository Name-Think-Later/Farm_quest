import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { useVerifyLocation } from '../../features/quests/useQuestFlows';

export function LocationVerifyPage() {
  const navigate = useNavigate();
  const mutation = useVerifyLocation();

  return (
    <MobileShell
      title="地點驗證"
      description="GPS 是強制條件，確認抵達任務點後才能進入後續謎題。"
      actions={
        <button
          type="button"
          className="primary-button"
          onClick={async () => {
            const result = await mutation.mutateAsync();
            if (result.ok && result.data.verified) {
              navigate('/quest/riddle');
            }
          }}
          disabled={mutation.isPending}
        >
          {mutation.isPending ? '確認中…' : '已抵達，前往下一步'}
        </button>
      }
    >
      <NetworkBanner />
      <div className="section-card">
        <strong>目前任務說明</strong>
        <p>到達景點後，按下按鈕即可確認並前往 AI 對話頁。</p>
        <p className="helper-text">正式串接後，這裡會接上真實 GPS 驗證流程。</p>
      </div>
      {mutation.data?.ok ? (
        <div className="status-card">
          <strong>驗證通過</strong>
          <p>{mutation.data.data.message}</p>
          <p>{mutation.data.data.distanceText}</p>
        </div>
      ) : null}
      <button type="button" className="text-button" onClick={() => navigate('/quest/current')}>
        返回任務頁
      </button>
    </MobileShell>
  );
}
