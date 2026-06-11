import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useSessionStore } from '../../features/session/sessionStore';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { LoadingState } from '../../components/feedback/LoadingState';
import { ErrorState } from '../../components/feedback/ErrorState';
import { InfoState } from '../../components/feedback/InfoState';
import { useCurrentQuest, useRiddleChat, useRiddleMessages } from '../../features/quests/useQuestFlows';
import { toChatMessages } from '../../features/quests/types';

export function RiddleChatPage() {
  const navigate = useNavigate();
  const token = useSessionStore((state) => state.token);
  const resetSession = useSessionStore((state) => state.resetSession);
  const [input, setInput] = useState('');
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
    }
    setInput('');
  };

  return (
    <div className="app-shell chat-page">
      {token ? (
        <header className="page-header sticky-header">
          <div className="page-header-row">
            <div className="header-nav-links">
              <Link to="/quest/current" className="header-nav-link">任務</Link>
              <Link to="/coupons/current" className="header-nav-link">優惠券</Link>
            </div>
            <button
              type="button"
              className="header-logout-button"
              onClick={() => {
                resetSession();
                navigate('/');
              }}
            >
              登出
            </button>
          </div>
        </header>
      ) : null}
      <main className="page-content">
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
      </main>
      {questCompleted ? (
        <div className="quest-completed-banner">
          <p>已完成景點任務</p>
          <Link to="/coupons/current" className="quest-completed-link">查看優惠券</Link>
        </div>
      ) : currentQuestQuery.data === null ? null : (
        <div className="chat-composer">
          <form id="chat-form" onSubmit={(event) => void onSubmit(event)}>
            <textarea
              id="chat-input"
              className="chat-textarea"
              rows={1}
              value={input}
              onChange={(event) => setInput(event.target.value)}
              placeholder="輸入答案，或直接詢問提示"
              disabled={mutation.isPending || !questId}
            />
            <button
              type="submit"
              form="chat-form"
              className="chat-send-button"
              disabled={mutation.isPending || !input.trim() || !questId}
            >
              {mutation.isPending ? '⋯' : '➤'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
