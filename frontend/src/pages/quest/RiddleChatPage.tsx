import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { useRiddleChat } from '../../features/quests/useQuestFlows';
import { useSessionStore } from '../../features/session/sessionStore';

export function RiddleChatPage() {
  const navigate = useNavigate();
  const [input, setInput] = useState('');
  const [resultText, setResultText] = useState('');
  const chatMessages = useSessionStore((state) => state.chatMessages);
  const mutation = useRiddleChat();

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!input.trim()) return;
    const result = await mutation.mutateAsync(input);
    if (result.ok) {
      setResultText(result.data.rewardMessage ?? result.data.reply.content);
      setInput('');
    }
  };

  return (
    <MobileShell
      title="AI 對話式猜謎"
      description="猜謎、答題與提示都在同一個 AI 對話中完成。答題結果以後端回傳狀態為準。"
      actions={
        <div className="inline-row">
          <button type="button" className="secondary-button" onClick={() => setInput('給我提示')}>快速填入提示需求</button>
          <button type="submit" form="chat-form" className="primary-button" disabled={mutation.isPending || !input.trim()}>{mutation.isPending ? '送出中…' : '送出訊息'}</button>
        </div>
      }
    >
      <NetworkBanner />
      <div className="section-card">
        <strong>任務提示</strong>
        <p>你已通過地點驗證，現在可以直接回答「高山茶」，或先向 AI 詢問提示。</p>
      </div>
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
      <form id="chat-form" className="section-card" onSubmit={(event) => void onSubmit(event)}>
        <div className="field-group">
          <label className="field-label" htmlFor="chat-input">輸入內容</label>
          <textarea id="chat-input" className="textarea-field" rows={4} value={input} onChange={(event) => setInput(event.target.value)} placeholder="輸入答案，或直接詢問提示" />
        </div>
      </form>
      {resultText ? <div className="status-card"><strong>目前狀態</strong><p>{resultText}</p></div> : null}
      <button type="button" className="text-button" onClick={() => navigate('/coupons/current')}>前往優惠券頁</button>
    </MobileShell>
  );
}
