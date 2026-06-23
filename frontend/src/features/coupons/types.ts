export type CouponStatus = 'ISSUED' | 'CONSUMED' | 'EXPIRED';

export type CouponSummaryResponse = {
  couponId: string;
  questId: string;
  couponCampaignId: string;
  merchantId: string;
  title: string;
  merchantName: string;
  status: CouponStatus;
  displayCode: string;
  issuedAt: string;
  expiresAt: string;
  consumedAt: string | null;
};

export type CouponDetailResponse = {
  couponId: string;
  questId: string;
  couponCampaignId: string;
  merchantId: string;
  title: string;
  description: string;
  merchantName: string;
  merchantAddress: string;
  status: CouponStatus;
  displayCode: string;
  issuedAt: string;
  expiresAt: string;
  consumedAt: string | null;
};

export type Coupon = {
  id: string;
  title: string;
  merchant: string;
  merchantAddress?: string;
  expiresAt: string;
  status: CouponStatus;
  usageText: string;
  description: string;
  displayCode: string;
};

export function toCoupon(summary: CouponSummaryResponse): Coupon {
  return {
    id: summary.couponId,
    title: summary.title,
    merchant: summary.merchantName,
    expiresAt: summary.expiresAt,
    status: summary.status,
    usageText: couponUsageText(summary.status, summary.consumedAt),
    description: `兌換代碼：${summary.displayCode}`,
    displayCode: summary.displayCode,
  };
}

export function toCouponDetail(detail: CouponDetailResponse): Coupon {
  return {
    id: detail.couponId,
    title: detail.title,
    merchant: detail.merchantName,
    merchantAddress: detail.merchantAddress ?? undefined,
    expiresAt: detail.expiresAt,
    status: detail.status,
    usageText: couponUsageText(detail.status, detail.consumedAt),
    description: detail.description,
    displayCode: detail.displayCode,
  };
}

function couponUsageText(status: CouponStatus, consumedAt: string | null) {
  if (status === 'CONSUMED') {
    return `已於 ${consumedAt ? new Date(consumedAt).toLocaleString('zh-TW') : '稍早'} 使用`;
  }

  if (status === 'EXPIRED') {
    return '此優惠券已過期，無法使用。';
  }

  return '向店家出示後，確認無誤再由你按下使用按鈕。';
}
