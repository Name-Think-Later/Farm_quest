export type CouponStatus = 'available' | 'used' | 'expired' | 'invalid';

export type Coupon = {
  id: string;
  title: string;
  merchant: string;
  expiresAt: string;
  status: CouponStatus;
  usageText: string;
  description: string;
};
