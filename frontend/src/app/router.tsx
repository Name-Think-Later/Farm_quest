import { useEffect } from 'react';
import type { ReactElement } from 'react';
import { createBrowserRouter, Link, Navigate, RouterProvider, useNavigate } from 'react-router-dom';
import { MobileShell } from '../components/layout/MobileShell';
import { NetworkBanner } from '../components/feedback/NetworkBanner';
import { LoadingState } from '../components/feedback/LoadingState';
import { ErrorState } from '../components/feedback/ErrorState';
import { useHealthStatus } from '../features/system/useHealthStatus';
import { useSessionStore } from '../features/session/sessionStore';
import { EntryPage } from '../pages/entry/EntryPage';
import { EmailLoginPage } from '../pages/auth/EmailLoginPage';
import { OtpVerifyPage } from '../pages/auth/OtpVerifyPage';
import { CurrentQuestPage } from '../pages/quest/CurrentQuestPage';
import { LocationVerifyPage } from '../pages/quest/LocationVerifyPage';
import { RiddleChatPage } from '../pages/quest/RiddleChatPage';
import { CouponPage } from '../pages/coupons/CouponPage';

function HomeLayout() {
  const { data, isLoading, error, refetch } = useHealthStatus();
  const hydrateFromStorage = useSessionStore((state) => state.hydrateFromStorage);
  const invalidSession = useSessionStore((state) => state.invalidSession);
  const clearInvalidSession = useSessionStore((state) => state.clearInvalidSession);
  const navigate = useNavigate();

  useEffect(() => {
    hydrateFromStorage();
  }, [hydrateFromStorage]);

  useEffect(() => {
    if (invalidSession) {
      clearInvalidSession();
      navigate('/', { replace: true });
    }
  }, [clearInvalidSession, invalidSession, navigate]);

  return (
    <MobileShell
      title="前端階段 1、2 骨架"
      description="以手機瀏覽器為主的低保真互動流程，包含 route、API client、狀態提示與 mock 驗證。"
    >
      <NetworkBanner />
      {isLoading ? <LoadingState message="正在檢查系統健康狀態…" /> : null}
      {error ? <ErrorState message={(error as Error).message} onRetry={() => void refetch()} /> : null}
      {data ? (
        <div className="section-card">
          <strong>系統健康檢查</strong>
          <p>{data.status} · {data.message}</p>
          <p className="helper-text">最後檢查：{new Date(data.checkedAt).toLocaleString('zh-TW')}</p>
        </div>
      ) : null}
      <div className="route-links">
        <Link className="route-link" to="/">遊戲入口頁</Link>
        <Link className="route-link" to="/auth/email">Email 登入頁</Link>
        <Link className="route-link" to="/auth/otp">OTP 驗證頁</Link>
        <Link className="route-link" to="/quest/current">目前任務頁</Link>
        <Link className="route-link" to="/quest/location">地點驗證頁</Link>
        <Link className="route-link" to="/quest/riddle">AI 對話猜謎頁</Link>
        <Link className="route-link" to="/coupons/current">優惠券頁</Link>
      </div>
    </MobileShell>
  );
}

function RequireVerified({ children }: { children: ReactElement }) {
  const verified = useSessionStore((state) => state.verified);
  return verified ? children : <Navigate to="/auth/email" replace />;
}

const router = createBrowserRouter([
  { path: '/', element: <EntryPage /> },
  { path: '/app', element: <HomeLayout /> },
  { path: '/auth/email', element: <EmailLoginPage /> },
  { path: '/auth/otp', element: <OtpVerifyPage /> },
  { path: '/quest/current', element: <RequireVerified><CurrentQuestPage /></RequireVerified> },
  { path: '/quest/location', element: <RequireVerified><LocationVerifyPage /></RequireVerified> },
  { path: '/quest/riddle', element: <RequireVerified><RiddleChatPage /></RequireVerified> },
  { path: '/coupons/current', element: <RequireVerified><CouponPage /></RequireVerified> },
]);

export function AppRouter() {
  return <RouterProvider router={router} />;
}
