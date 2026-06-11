import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { LoadingState } from '../../components/feedback/LoadingState';
import { ErrorState } from '../../components/feedback/ErrorState';
import { InfoState } from '../../components/feedback/InfoState';
import { useCurrentQuest, useRiddleChat, useRiddleMessages } from '../../features/quests/useQuestFlows';
import { toChatMessages } from '../../features/quests/types';

export function RiddleChatPage() {
  const navigate = useNavigate();
  const [input, setInput] = useState('');
  const [resultText, setResultText] = useState('');
  const [questCompleted, setQuestCompleted] = useState(false);
  const currentQuestQuery = useCurrentQuest();
  const questId = currentQuestQuery.data?.questId;
  const messagesQuery = useRiddleMessages(questId);
  const mutation = useRiddleChat(questId);
  const chatMessages = toChatMessages(messagesQuery.data?.messages ?? []);

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!input.trim()) return;
    const result = await mutation.mutateAsync(input);
    if (result.questCompleted) {
      setQuestCompleted(true);
      setResultText('已完成景點任務並取得優惠券。');
    } else {
      setResultText(result.safeMessage ?? result.replyContent);
    }
    setInput('');
  };

  return (
    <MobileShell
      title="AI 對話式猜謎"
      actions={
        questCompleted || currentQuestQuery.data === null ? null : (
          <div className="inline-row">
            <button type="button" className="secondary-button" onClick={() => setInput('給我提示')}>快速填入提示需求</button>
            <button type="submit" form="chat-form" className="primary-button" disabled={mutation.isPending || !input.trim() || !questId}>{mutation.isPending ? '送出中…' : '送出訊息'}</button>
          </div>
        )
      }
    >
      <NetworkBanner />
      {currentQuestQuery.isLoading || messagesQuery.isLoading ? <LoadingState message="正在載入對話紀錄…" /> : null}
      {currentQuestQuery.error ? <ErrorState message={(currentQuestQuery.error as Error).message} onRetry={() => void currentQuestQuery.refetch()} /> : null}
      {messagesQuery.error ? <ErrorState message={(messagesQuery.error as Error).message} onRetry={() => void messagesQuery.refetch()} /> : null}
      {mutation.error ? <ErrorState message={(mutation.error as Error).message} /> : null}
      {currentQuestQuery.data === null && !currentQuestQuery.isLoading ? <InfoState title="任務已完成" message="此景點任務已完成，您已獲得優惠券。可前往優惠券頁查看。" /> : null}
      <div className="chat-log line-chat-log">
        {chatMessages.map((message) => (
          <div key={message.id} className={`chat-row ${message.role}`}>
            <div className={`chat-bubble ${message.role}`}>
              <strong className="chat-speaker">{message.role === 'ai' ? 'AI NPC' : '你'}</strong>
              <p>{message.content}</p>
            </div>
          </div>
        ))}
      </div>
      {questCompleted || currentQuestQuery.data === null ? null : (
        <form id="chat-form" className="section-card chat-composer-card" onSubmit={(event) => void onSubmit(event)}>
          <div className="field-group">
            <label className="field-label" htmlFor="chat-input">輸入內容</label>
            <textarea id="chat-input" className="textarea-field" rows={4} value={input} onChange={(event) => setInput(event.target.value)} placeholder="輸入答案，或直接詢問提示" />
          </div>
        </form>
      )}
      {resultText ? <div className="status-card"><strong>目前狀態</strong><p>{resultText}</p></div> : null}
      <button type="button" className="text-button" onClick={() => navigate('/coupons/current')}>前往優惠券頁</button>
    </MobileShell>
  );
}
