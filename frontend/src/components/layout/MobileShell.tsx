import type { PropsWithChildren, ReactNode } from 'react';

type MobileShellProps = PropsWithChildren<{
  title: string;
  description?: string;
  actions?: ReactNode;
}>;

export function MobileShell({ title, description, actions, children }: MobileShellProps) {
  return (
    <div className="app-shell">
      <header className="page-header">
        <p className="eyebrow">農遊謎走</p>
        <h1>{title}</h1>
        {description ? <p className="page-description">{description}</p> : null}
      </header>
      <main className="page-content">{children}</main>
      {actions ? <footer className="page-actions">{actions}</footer> : null}
    </div>
  );
}
