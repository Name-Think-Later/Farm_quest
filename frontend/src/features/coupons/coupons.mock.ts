import type { Coupon } from './types';

const mockCoupons: Coupon[] = [
  {
    id: 'coupon-tea-001',
    title: '茶園體驗折價券',
    merchant: '山霧茶屋',
    expiresAt: '2026-12-31 18:00',
    description: '向店家出示後，確認品項與折抵內容無誤，再由你自行按下使用按鈕。',
    status: 'ISSUED',
    usageText: '向店家出示後，確認無誤再由你按下使用按鈕。',
    displayCode: 'MOCK001',
  },
];

export async function getMockCoupons(): Promise<Coupon[]> {
  await new Promise((resolve) => setTimeout(resolve, 160));
  return mockCoupons;
}

export async function getMockCouponById(couponId: string): Promise<Coupon | null> {
  await new Promise((resolve) => setTimeout(resolve, 160));
  return mockCoupons.find((coupon) => coupon.id === couponId) ?? null;
}

export async function consumeMockCoupon(couponId: string) {
  await new Promise((resolve) => setTimeout(resolve, 220));
  const coupon = mockCoupons.find((item) => item.id === couponId);

  if (!coupon) {
    return {
      success: false,
      message: '找不到這張優惠券。',
    };
  }

  return {
    success: true,
    message: '優惠券已標記為使用完成。',
  };
}
