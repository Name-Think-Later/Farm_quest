import { useMutation, useQuery } from '@tanstack/react-query';
import { fetchCurrentQuest, fetchQuestCouponPreview, sendRiddleMessage, verifyLocation } from './quests.api';

export function useCurrentQuest() {
  return useQuery({
    queryKey: ['current-quest'],
    queryFn: async () => {
      const result = await fetchCurrentQuest();
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
  });
}

export function useQuestCouponPreview() {
  return useQuery({
    queryKey: ['quest-coupon-preview'],
    queryFn: async () => {
      const result = await fetchQuestCouponPreview();
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
  });
}

export function useVerifyLocation() {
  return useMutation({
    mutationFn: verifyLocation,
  });
}

export function useRiddleChat() {
  return useMutation({
    mutationFn: sendRiddleMessage,
  });
}
