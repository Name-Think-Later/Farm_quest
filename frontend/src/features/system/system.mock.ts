import { HealthStatus } from './types';

export async function getMockHealthStatus(): Promise<HealthStatus> {
  await new Promise((resolve) => setTimeout(resolve, 200));
  return {
    status: 'UP',
    application: 'farm-quest-frontend-mock',
    timestamp: new Date().toISOString(),
    seedDataReady: true,
  };
}
