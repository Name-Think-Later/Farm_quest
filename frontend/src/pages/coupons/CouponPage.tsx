import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { LoadingState } from '../../components/feedback/LoadingState';
import { ErrorState } from '../../components/feedback/ErrorState';
import { useConsumeCoupon, useCoupon } from '../../features/coupons/useCoupon';

function statusText(status: 'available' | 'used' | 'expired' | 'invalid') {
  switch (status) {
    case 'available':
      return '可使用';
    case 'used':
      return '已使用';
    case 'expired':
      return '已過期';
    case 'invalid':
      return '無效';
  }
}

export function CouponPage() {
  const navigate = useNavigate();
  const query = useCoupon();
  const mutation = useConsumeCoupon();

  return (
    <MobileShell
      title="優惠券頁"
      actions={<button type="button" className="primary-button" disabled={!query.data || query.data.status !== 'available' || mutation.isPending} onClick={async () => { await mutation.mutateAsync(); void query.refetch(); }}>{mutation.isPending ? '使用中…' : '確認使用優惠券'}</button>}
    >
      <NetworkBanner />
      {query.isLoading ? <LoadingState message="正在讀取優惠券…" /> : null}
      {query.error ? <ErrorState message={(query.error as Error).message} onRetry={() => void query.refetch()} /> : null}
      {query.data ? (
        <div className="coupon-card accent-card">
          <strong>{query.data.title}</strong>
          <p>適用商家：{query.data.merchant}</p>
          <p>有效期限：{query.data.expiresAt}</p>
          <p><span className="coupon-status">{statusText(query.data.status)}</span></p>
          <p>{query.data.usageText}</p>
        </div>
      ) : null}
      {mutation.data?.ok ? <div className="status-card"><strong>使用狀態</strong><p>{mutation.data.data.message}</p></div> : null}
      <button type="button" className="text-button" onClick={() => navigate('/quest/current')}>返回任務頁</button>
    </MobileShell>
  );
}
