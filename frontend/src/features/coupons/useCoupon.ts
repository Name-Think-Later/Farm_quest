import { useMutation, useQuery } from '@tanstack/react-query';
import { consumeCoupon, fetchCoupon } from './coupons.api';

export function useCoupon() {
  return useQuery({
    queryKey: ['coupon'],
    queryFn: async () => {
      const result = await fetchCoupon();
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
  });
}

export function useConsumeCoupon() {
  return useMutation({
    mutationFn: consumeCoupon,
  });
}
