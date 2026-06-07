import { env } from '../../lib/config/env';
import { apiGet } from '../../lib/http/apiClient';
import { ApiResult, ok } from '../../lib/http/apiResult';
import { getMockHealthStatus } from './system.mock';
import { HealthStatus } from './types';

export async function fetchSystemHealth(): Promise<ApiResult<HealthStatus>> {
  if (env.useMockApi) {
    return ok(await getMockHealthStatus());
  }

  return apiGet<HealthStatus>('/api/system/health');
}
