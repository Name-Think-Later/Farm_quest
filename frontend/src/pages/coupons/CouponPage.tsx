import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { LoadingState } from '../../components/feedback/LoadingState';
import { ErrorState } from '../../components/feedback/ErrorState';
import { useCoupons } from '../../features/coupons/useCoupon';
import type { CouponStatus } from '../../features/coupons/types';

function statusText(status: CouponStatus) {
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
  const query = useCoupons();

  return (
    <MobileShell title="優惠券列表頁">
      <NetworkBanner />
      {query.isLoading ? <LoadingState message="正在讀取優惠券…" /> : null}
      {query.error ? <ErrorState message={(query.error as Error).message} onRetry={() => void query.refetch()} /> : null}
      {query.data ? (
        query.data.length > 0 ? (
          <div className="coupon-list">
            {query.data.map((coupon) => (
              <div key={coupon.id} className="coupon-card accent-card">
                <strong>{coupon.title}</strong>
                <div className="coupon-meta">
                  <p>適用商家：{coupon.merchant || '店家資訊缺漏'}</p>
                  <p>有效期限：{coupon.expiresAt}</p>
                  <p>
                    <span className={`coupon-status ${coupon.status}`}>{statusText(coupon.status)}</span>
                  </p>
                  <p>{coupon.usageText}</p>
                </div>
                <div className="coupon-card-actions">
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={() => navigate(`/coupons/current/${coupon.id}`)}
                  >
                    查看詳情
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : <div className="status-card"><strong>目前沒有可顯示的優惠券</strong><p>完成任務後，新的優惠券會出現在這裡。</p></div>
      ) : null}
      <button type="button" className="text-button" onClick={() => navigate('/quest/current')}>
        返回任務頁
      </button>
    </MobileShell>
  );
}
