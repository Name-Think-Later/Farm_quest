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
      <div className="section-card accent-card">
        <strong>任務提示</strong>
        <p>確認已抵達景點後，按下按鈕即可前往下一步，開始與 AI NPC 對話。</p>
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
