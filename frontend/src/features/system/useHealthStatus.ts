import { useQuery } from '@tanstack/react-query';
import { fetchSystemHealth } from './system.api';

export function useHealthStatus() {
  return useQuery({
    queryKey: ['system-health'],
    queryFn: async () => {
      const result = await fetchSystemHealth();
      if (!result.ok) {
        throw new Error(result.message);
      }
      return result.data;
    },
  });
}
