import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { LoadingState } from '../../components/feedback/LoadingState';
import { ErrorState } from '../../components/feedback/ErrorState';
import { useConsumeCoupon, useCoupon } from '../../features/coupons/useCoupon';
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

export function CouponDetailPage() {
  const navigate = useNavigate();
  const { couponId } = useParams();
  const query = useCoupon(couponId);
  const mutation = useConsumeCoupon();
  const [confirmOpen, setConfirmOpen] = useState(false);

  const coupon = query.data;
  const missingCouponId = !couponId;
  const canConsume = coupon?.status === 'available' && Boolean(coupon?.merchant) && !mutation.isPending;

  return (
    <>
      <MobileShell
        title="優惠券詳情"
        actions={
          coupon && coupon.status === 'available' ? (
            <button type="button" className="primary-button" disabled={!canConsume} onClick={() => setConfirmOpen(true)}>
              {mutation.isPending ? '使用中…' : '確認使用優惠券'}
            </button>
          ) : undefined
        }
      >
        <NetworkBanner />
        {missingCouponId ? <ErrorState message="找不到優惠券編號。" /> : null}
        {query.isLoading ? <LoadingState message="正在讀取優惠券詳情…" /> : null}
        {!missingCouponId && query.error ? <ErrorState message={(query.error as Error).message} onRetry={() => void query.refetch()} /> : null}
        {coupon ? (
          <>
            <div className="coupon-card accent-card">
              <strong>{coupon.title}</strong>
              <div className="coupon-meta">
                <p>優惠內容：{coupon.description}</p>
                <p>適用商家：{coupon.merchant || '店家資訊缺漏'}</p>
                <p>有效期限：{coupon.expiresAt}</p>
                <p>
                  <span className={`coupon-status ${coupon.status}`}>{statusText(coupon.status)}</span>
                </p>
                <p>{coupon.usageText}</p>
              </div>
            </div>
            {mutation.data?.ok ? (
              <div className="status-card">
                <strong>使用狀態</strong>
                <p>{mutation.data.data.message}</p>
              </div>
            ) : null}
            {!coupon.merchant ? (
              <div className="status-card status-error">
                <strong>資料暫時不完整</strong>
                <p>這張優惠券缺少店家資訊，目前不可使用。</p>
              </div>
            ) : null}
          </>
        ) : null}
        <button type="button" className="text-button" onClick={() => navigate('/coupons/current')}>
          返回優惠券列表
        </button>
      </MobileShell>
      {confirmOpen && coupon ? (
        <div className="dialog-backdrop" role="presentation" onClick={() => setConfirmOpen(false)}>
          <div className="dialog-card" role="dialog" aria-modal="true" aria-labelledby="coupon-confirm-title" onClick={(event) => event.stopPropagation()}>
            <strong id="coupon-confirm-title">確認立即使用優惠券？</strong>
            <p>按下確認後，這張優惠券會立即失效，且不能再次使用。</p>
            <div className="dialog-actions">
              <button
                type="button"
                className="primary-button"
                disabled={mutation.isPending}
                onClick={async () => {
                  if (!couponId) {
                    return;
                  }

                  await mutation.mutateAsync(couponId);
                  setConfirmOpen(false);
                }}
              >
                {mutation.isPending ? '使用中…' : '確認立即使用'}
              </button>
              <button type="button" className="secondary-button" disabled={mutation.isPending} onClick={() => setConfirmOpen(false)}>
                取消
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </>
  );
}
