type ErrorStateProps = {
  title?: string;
  message: string;
  onRetry?: () => void;
};

export function ErrorState({ title = '發生問題', message, onRetry }: ErrorStateProps) {
  return (
    <div className="status-card status-error">
      <strong>{title}</strong>
      <p>{message}</p>
      {onRetry ? (
        <button type="button" className="secondary-button" onClick={onRetry}>
          重新嘗試
        </button>
      ) : null}
    </div>
  );
}
