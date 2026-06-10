import type { PropsWithChildren, ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSessionStore } from '../../features/session/sessionStore';

type MobileShellProps = PropsWithChildren<{
  title: string;
  description?: string;
  actions?: ReactNode;
}>;

export function MobileShell({ title, description, actions, children }: MobileShellProps) {
  const navigate = useNavigate();
  const isAuthenticated = useSessionStore((state) => state.isAuthenticated);
  const resetSession = useSessionStore((state) => state.resetSession);

  return (
    <div className="app-shell">
      <header className="page-header">
        <div className="page-header-row">
          <p className="eyebrow">農遊謎走</p>
          {isAuthenticated ? (
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
          ) : null}
        </div>
        <h1>{title}</h1>
        {description ? <p className="page-description">{description}</p> : null}
      </header>
      <main className="page-content">{children}</main>
      {actions ? <footer className="page-actions">{actions}</footer> : null}
    </div>
  );
}
