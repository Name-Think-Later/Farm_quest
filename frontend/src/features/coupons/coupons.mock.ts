import { useSessionStore } from '../session/sessionStore';
import { CouponDetail } from './types';

export async function getMockCoupon(): Promise<CouponDetail> {
  await new Promise((resolve) => setTimeout(resolve, 160));
  const state = useSessionStore.getState();
  return {
    id: 'coupon-tea-001',
    title: '茶園體驗折價券',
    merchant: '山霧茶屋',
    expiresAt: '2026-12-31 18:00',
    status: state.couponStatus,
    usageText:
      state.couponStatus === 'used'
        ? `已於 ${state.couponConsumedAt ?? '稍早'} 使用`
        : state.couponStatus === 'expired'
          ? '此優惠券已過期，無法使用。'
          : state.couponStatus === 'invalid'
            ? '此優惠券目前無效，請洽現場人員。'
            : '向店家出示後，確認無誤再由你按下使用按鈕。',
  };
}

export async function consumeMockCoupon() {
  await new Promise((resolve) => setTimeout(resolve, 220));
  const state = useSessionStore.getState();
  if (state.couponStatus !== 'available') {
    return {
      success: false,
      message: '目前優惠券狀態不可再次使用。',
    };
  }

  state.consumeCoupon();
  return {
    success: true,
    message: '優惠券已標記為使用完成。',
  };
}
