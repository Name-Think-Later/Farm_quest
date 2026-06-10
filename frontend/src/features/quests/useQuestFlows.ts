import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  fetchCurrentQuest,
  fetchLocationHint,
  fetchRiddleMessages,
  sendRiddleMessage,
  startQuest,
  verifyLocation,
} from './quests.api';
import type { LocationVerificationPayload } from './types';

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

export function useLocationHint(questId: string | undefined) {
  return useQuery({
    queryKey: ['location-hint', questId],
    enabled: Boolean(questId),
    queryFn: async () => {
      if (!questId) {
        throw new Error('找不到任務編號。');
      }

      const result = await fetchLocationHint(questId);
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
  });
}

export function useStartQuest() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (questId: string) => {
      const result = await startQuest(questId);
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['current-quest'] }),
        queryClient.invalidateQueries({ queryKey: ['game-state'] }),
      ]);
    },
  });
}

export function useVerifyLocation(questId: string | undefined) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (payload: LocationVerificationPayload) => {
      if (!questId) {
        throw new Error('找不到任務編號。');
      }

      const result = await verifyLocation(questId, payload);
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['current-quest'] }),
        queryClient.invalidateQueries({ queryKey: ['game-state'] }),
        queryClient.invalidateQueries({ queryKey: ['location-hint', questId] }),
      ]);
    },
  });
}

export function useRiddleMessages(questId: string | undefined) {
  return useQuery({
    queryKey: ['riddle-messages', questId],
    enabled: Boolean(questId),
    queryFn: async () => {
      if (!questId) {
        throw new Error('找不到任務編號。');
      }

      const result = await fetchRiddleMessages(questId);
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
  });
}

export function useRiddleChat(questId: string | undefined) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: string) => {
      if (!questId) {
        throw new Error('找不到任務編號。');
      }

      const result = await sendRiddleMessage(questId, { content: input });
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['riddle-messages', questId] }),
        queryClient.invalidateQueries({ queryKey: ['current-quest'] }),
        queryClient.invalidateQueries({ queryKey: ['game-state'] }),
      ]);
    },
  });
}
