export type HealthStatus = {
  status: 'UP' | 'DEGRADED';
  application: string;
  timestamp: string;
  seedDataReady: boolean;
};
