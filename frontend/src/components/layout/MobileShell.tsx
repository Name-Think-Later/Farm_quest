import type { PropsWithChildren, ReactNode } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useSessionStore } from '../../features/session/sessionStore';

type MobileShellProps = PropsWithChildren<{
  title?: string;
  description?: string;
  actions?: ReactNode;
}>;

export function MobileShell({ title, description, actions, children }: MobileShellProps) {
  const navigate = useNavigate();
  const token = useSessionStore((state) => state.token);
  const resetSession = useSessionStore((state) => state.resetSession);

  return (
    <div className="app-shell">
      {token ? (
        <header className="page-header">
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
      <main className="page-content">{children}</main>
      {actions ? <footer className="page-actions">{actions}</footer> : null}
    </div>
  );
}
