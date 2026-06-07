type LoadingStateProps = {
  message?: string;
};

export function LoadingState({ message = '載入中…' }: LoadingStateProps) {
  return <div className="status-card">{message}</div>;
}
