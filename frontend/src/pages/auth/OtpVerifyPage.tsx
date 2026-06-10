import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MobileShell } from '../../components/layout/MobileShell';
import { NetworkBanner } from '../../components/feedback/NetworkBanner';
import { ErrorState } from '../../components/feedback/ErrorState';
import { useVerifyOtp } from '../../features/auth/useVerifyOtp';
import { useSessionStore } from '../../features/session/sessionStore';

export function OtpVerifyPage() {
  const navigate = useNavigate();
  const email = useSessionStore((state) => state.email);
  const [otp, setOtp] = useState('');
  const [statusMessage, setStatusMessage] = useState('');
  const mutation = useVerifyOtp();

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    const result = await mutation.mutateAsync({ email, otp });
    if (!result.ok) {
      setStatusMessage(result.message);
      return;
    }

    setStatusMessage('驗證成功，正在帶你前往目前任務頁。');
    if (result.data.authenticated) {
      navigate('/quest/current');
    }
  };

  return (
    <MobileShell
      title="Email 驗證確認"
      actions={<button type="submit" form="otp-form" className="primary-button" disabled={mutation.isPending || otp.length < 6 || !email}>{mutation.isPending ? '驗證中…' : '確認驗證碼'}</button>}
    >
      <NetworkBanner />
      <form id="otp-form" className="section-card" onSubmit={(event) => void onSubmit(event)}>
        <div className="field-group">
          <label className="field-label" htmlFor="otp">OTP 驗證碼</label>
          <input id="otp" className="input-field" type="text" inputMode="numeric" value={otp} onChange={(event) => setOtp(event.target.value.replace(/\D/g, '').slice(0, 6))} placeholder="輸入 6 碼驗證碼" />
          <p className="helper-text">驗證成功後會自動進入目前任務頁。</p>
        </div>
      </form>
      {!email ? <div className="status-card status-error"><strong>缺少 Email 資訊</strong><p>請返回上一頁重新輸入 Email 後再驗證。</p></div> : null}
      {statusMessage ? <div className="status-card"><strong>驗證狀態</strong><p>{statusMessage}</p></div> : null}
      {mutation.data?.ok === false ? <ErrorState title="驗證未通過" message={mutation.data.message} /> : null}
      <button type="button" className="text-button" onClick={() => navigate('/auth/email')}>返回重新輸入 Email</button>
    </MobileShell>
  );
}
