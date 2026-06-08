import type { ApiResult } from '../../lib/http/apiResult';
import { fail, ok } from '../../lib/http/apiResult';
import { consumeMockCoupon, getMockCouponById, getMockCoupons } from './coupons.mock';
import type { Coupon } from './types';

type ConsumeCouponResult = {
  success: boolean;
  message: string;
};

export async function fetchCoupons(): Promise<ApiResult<Coupon[]>> {
  return ok(await getMockCoupons());
}

export async function fetchCouponById(couponId: string): Promise<ApiResult<Coupon>> {
  const coupon = await getMockCouponById(couponId);
  return coupon ? ok(coupon) : fail('COUPON_NOT_FOUND', '找不到這張優惠券。');
}

export async function consumeCoupon(couponId: string): Promise<ApiResult<ConsumeCouponResult>> {
  return ok(await consumeMockCoupon(couponId));
}
