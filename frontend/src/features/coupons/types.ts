export type CouponDetail = {
  id: string;
  title: string;
  merchant: string;
  expiresAt: string;
  status: 'available' | 'used' | 'expired' | 'invalid';
  usageText: string;
};
