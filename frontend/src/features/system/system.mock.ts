import { HealthStatus } from './types';

export async function getMockHealthStatus(): Promise<HealthStatus> {
  await new Promise((resolve) => setTimeout(resolve, 200));
  return {
    status: 'UP',
    checkedAt: new Date().toISOString(),
    message: '目前使用 mock health 狀態，之後可切換為真實後端檢查。',
    mode: 'mock',
  };
}
