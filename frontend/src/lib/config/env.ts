export const env = {
  apiBaseUrl: (import.meta.env.VITE_PUBLIC_API_BASE_URL as string | undefined) ?? 'http://localhost:8080',
  useMockApi: ((import.meta.env.VITE_USE_MOCK_API as string | undefined) ?? 'true') !== 'false',
};
