export type HealthStatus = {
  status: 'UP' | 'DOWN';
  checkedAt: string;
  message: string;
  mode: 'mock' | 'remote';
};
