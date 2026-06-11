import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  fetchCurrentQuest,
  fetchLocationHint,
  fetchRiddleMessages,
  sendRiddleMessage,
  startQuest,
  verifyLocation,
} from './quests.api';
import type { AiRiddleConversationResponse, AiRiddleMessage, LocationVerificationPayload } from './types';

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
    placeholderData: (previousData) => previousData,
    refetchOnWindowFocus: false,
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
      return { input, response: result.data };
    },
    onSuccess: async (data) => {
      const { input, response } = data;

      if (response.questCompleted) {
        // 任务完成时，手动更新消息数据，避免重新 fetch 导致的错误
        const currentData = queryClient.getQueryData<AiRiddleConversationResponse>(['riddle-messages', questId]);
        if (currentData) {
          const now = new Date().toISOString();
          const userMessage: AiRiddleMessage = {
            messageId: `temp-user-${Date.now()}`,
            role: 'VISITOR',
            content: input,
            answerCorrect: null,
            createdAt: now,
          };
          const aiMessage: AiRiddleMessage = {
            messageId: `temp-ai-${Date.now()}`,
            role: 'ASSISTANT',
            content: response.safeMessage ?? response.replyContent,
            answerCorrect: response.correct,
            createdAt: now,
          };

          queryClient.setQueryData(['riddle-messages', questId], {
            questId: response.questId,
            conversationId: response.conversationId,
            status: 'COMPLETED',
            questCompleted: true,
            nextStep: response.nextStep,
            messages: [...currentData.messages, userMessage, aiMessage],
          });
        }

        await Promise.all([
          queryClient.invalidateQueries({ queryKey: ['current-quest'] }),
          queryClient.invalidateQueries({ queryKey: ['game-state'] }),
        ]);
      } else {
        // 任务未完成，正常刷新消息列表
        await Promise.all([
          queryClient.invalidateQueries({ queryKey: ['riddle-messages', questId] }),
          queryClient.invalidateQueries({ queryKey: ['current-quest'] }),
          queryClient.invalidateQueries({ queryKey: ['game-state'] }),
        ]);
      }
    },
  });
}
