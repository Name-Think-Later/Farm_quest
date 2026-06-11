type InfoStateProps = {
  title?: string;
  message: string;
};

export function InfoState({ title, message }: InfoStateProps) {
  return (
    <div className="status-card">
      {title ? <strong>{title}</strong> : null}
      <p>{message}</p>
    </div>
  );
}