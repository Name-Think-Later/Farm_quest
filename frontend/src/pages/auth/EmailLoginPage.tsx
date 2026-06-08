import { FormEvent, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { ErrorState } from '../../components/feedback/ErrorState';
import { useSendOtp } from '../../features/auth/useSendOtp';
import { useSessionStore } from '../../features/session/sessionStore';

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function EmailLoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [submittedMessage, setSubmittedMessage] = useState('');
  const otpCooldownEndsAt = useSessionStore((state) => state.otpCooldownEndsAt);
  const mutation = useSendOtp();

  const emailError = useMemo(() => {
    if (!email) return '';
    return emailRegex.test(email) ? '' : '請輸入正確的 Email 格式。';
  }, [email]);

  const cooldownSeconds = otpCooldownEndsAt ? Math.max(0, Math.ceil((otpCooldownEndsAt - Date.now()) / 1000)) : 0;

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (emailError || !email) {
      return;
    }

    const result = await mutation.mutateAsync({ email });
    if (result.ok) {
      setSubmittedMessage(result.data.message);
      navigate('/auth/otp');
    }
  };

  return (
    <MobileShell
      title="遊客 Email 登入"
      actions={
        <button type="submit" form="email-login-form" className="primary-button" disabled={mutation.isPending || Boolean(emailError) || !email}>
          {mutation.isPending ? '送出中…' : '送出 OTP 驗證碼'}
        </button>
      }
    >
      <NetworkBanner />
      <form id="email-login-form" className="section-card" onSubmit={(event) => void onSubmit(event)}>
        <div className="field-group">
          <label className="field-label" htmlFor="email">Email</label>
          <input id="email" className="input-field" type="email" inputMode="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="name@example.com" />
          {emailError ? <span className="field-error">{emailError}</span> : null}
          <p className="helper-text">請先完成 Email 登入與 OTP 驗證，才能進入任務與優惠券流程。重新寄送需冷卻，避免重複寄送。</p>
        </div>
      </form>
      {submittedMessage ? <div className="status-card"><strong>送出結果</strong><p>{submittedMessage}</p></div> : null}
      {cooldownSeconds > 0 ? <div className="status-card"><strong>重新寄送冷卻中</strong><p>請在 {cooldownSeconds} 秒後再重新寄送。</p></div> : null}
      {mutation.error ? <ErrorState message={(mutation.error as Error).message} /> : null}
      <button type="button" className="text-button" onClick={() => navigate('/')}>返回入口頁</button>
    </MobileShell>
  );
}
