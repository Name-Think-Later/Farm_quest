import type { ApiResult } from '../../lib/http/apiResult';
import { apiGet, apiPost } from '../../lib/http/apiClient';
import type { Coupon, CouponDetailResponse, CouponSummaryResponse } from './types';
import { toCoupon, toCouponDetail } from './types';

type CouponListResponse = {
  coupons: CouponSummaryResponse[];
};

type ConsumeCouponResult = {
  couponId: string;
  couponUsageId: string;
  status: string;
  consumedAt: string;
};

export async function fetchCoupons(): Promise<ApiResult<Coupon[]>> {
  const result = await apiGet<CouponListResponse>('/api/coupons/my', {
    authenticated: true,
  });

  if (!result.ok) {
    return result;
  }

  return {
    ok: true,
    data: result.data.coupons.map(toCoupon),
  };
}

export async function fetchCouponById(couponId: string): Promise<ApiResult<Coupon>> {
  const result = await apiGet<CouponDetailResponse>(`/api/coupons/${couponId}`, {
    authenticated: true,
  });

  if (!result.ok) {
    return result;
  }

  return {
    ok: true,
    data: toCouponDetail(result.data),
  };
}

export async function consumeCoupon(couponId: string): Promise<ApiResult<ConsumeCouponResult>> {
  return apiPost<{ clientConfirmedAt: string }, ConsumeCouponResult>(
    `/api/coupons/${couponId}/consume`,
    {
      clientConfirmedAt: new Date().toISOString(),
    },
    {
      authenticated: true,
    },
  );
}
