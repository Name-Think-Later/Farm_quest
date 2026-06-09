import { useSessionStore } from '../session/sessionStore';
import type { Coupon, CouponStatus } from './types';

export const QUEST_REWARD_COUPON_ID = 'coupon-tea-001';

type CouponTemplate = {
  id: string;
  title: string;
  merchant: string;
  expiresAt: string;
  description: string;
  requiresQuestCompletion?: boolean;
};

const couponCatalog: CouponTemplate[] = [
  {
    id: QUEST_REWARD_COUPON_ID,
    title: '茶園體驗折價券',
    merchant: '山霧茶屋',
    expiresAt: '2026-12-31 18:00',
    description: '向店家出示後，確認品項與折抵內容無誤，再由你自行按下使用按鈕。',
    requiresQuestCompletion: true,
  },
  {
    id: 'coupon-dessert-002',
    title: '茶點加購折價券',
    merchant: '茶香點心坊',
    expiresAt: '2026-11-15 17:30',
    description: '可折抵一份指定茶點，請於點餐前先出示優惠券內容。',
  },
  {
    id: 'coupon-market-003',
    title: '農產市集滿額券',
    merchant: '山城農產市集',
    expiresAt: '2026-04-30 16:00',
    description: '單筆消費滿額即可折抵，限活動期間內使用。',
  },
  {
    id: 'coupon-gift-004',
    title: '限定伴手禮兌換券',
    merchant: '旅客服務中心',
    expiresAt: '2026-10-01 18:00',
    description: '請依現場公告兌換指定伴手禮，若活動資格異常將無法使用。',
  },
];

function resolvedStatus(template: CouponTemplate, status: CouponStatus): CouponStatus {
  if (!template.merchant.trim()) {
    return 'invalid';
  }

  return status;
}

function usageText(template: CouponTemplate, status: CouponStatus, consumedAt: string | null) {
  if (!template.merchant.trim()) {
    return '店家資訊缺漏，這張優惠券目前不可使用。';
  }

  if (status === 'used') {
    return `已於 ${consumedAt ?? '稍早'} 使用`;
  }

  if (status === 'expired') {
    return '此優惠券已過期，無法使用。';
  }

  if (status === 'invalid') {
    return '此優惠券目前無效，請洽現場人員。';
  }

  return '向店家出示後，確認無誤再由你按下使用按鈕。';
}

function toCoupon(template: CouponTemplate, status: CouponStatus, consumedAt: string | null): Coupon {
  const effectiveStatus = resolvedStatus(template, status);

  return {
    id: template.id,
    title: template.title,
    merchant: template.merchant,
    expiresAt: template.expiresAt,
    description: template.description,
    status: effectiveStatus,
    usageText: usageText(template, effectiveStatus, consumedAt),
  };
}

function visibleCoupons() {
  const state = useSessionStore.getState();

  return couponCatalog
    .filter((coupon) => state.questCompleted || !coupon.requiresQuestCompletion)
    .map((coupon) => {
      const couponState = state.couponStates[coupon.id] ?? { status: 'invalid' as const, consumedAt: null };
      return toCoupon(coupon, couponState.status, couponState.consumedAt);
    });
}

export async function getMockCoupons(): Promise<Coupon[]> {
  await new Promise((resolve) => setTimeout(resolve, 160));
  return visibleCoupons();
}

export async function getMockCouponById(couponId: string): Promise<Coupon | null> {
  await new Promise((resolve) => setTimeout(resolve, 160));
  return visibleCoupons().find((coupon) => coupon.id === couponId) ?? null;
}

export async function consumeMockCoupon(couponId: string) {
  await new Promise((resolve) => setTimeout(resolve, 220));
  const state = useSessionStore.getState();
  const coupon = visibleCoupons().find((item) => item.id === couponId);

  if (!coupon) {
    return {
      success: false,
      message: '找不到這張優惠券。',
    };
  }

  if (coupon.status !== 'available') {
    return {
      success: false,
      message: '目前優惠券狀態不可再次使用。',
    };
  }

  state.consumeCoupon(couponId);
  return {
    success: true,
    message: '優惠券已標記為使用完成。',
  };
}
