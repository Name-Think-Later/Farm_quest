import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { Coupon } from './types';
import { consumeCoupon, fetchCouponById, fetchCoupons } from './coupons.api';

type ConsumeCouponResult = {
  success: boolean;
  message: string;
};

export function useCoupons() {
  return useQuery<Coupon[]>({
    queryKey: ['coupons'],
    queryFn: async () => {
      const result = await fetchCoupons();
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
  });
}

export function useCoupon(couponId: string | undefined) {
  return useQuery<Coupon>({
    queryKey: ['coupon', couponId],
    enabled: Boolean(couponId),
    queryFn: async () => {
      if (!couponId) {
        throw new Error('找不到優惠券編號。');
      }

      const result = await fetchCouponById(couponId);
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
  });
}

export function useConsumeCoupon() {
  const queryClient = useQueryClient();

  return useMutation<{ ok: true; data: ConsumeCouponResult }, Error, string>({
    mutationFn: async (couponId) => {
      const result = await consumeCoupon(couponId);
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result;
    },
    onSuccess: async (_, couponId) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['coupons'] }),
        queryClient.invalidateQueries({ queryKey: ['coupon', couponId] }),
      ]);
    },
  });
}
