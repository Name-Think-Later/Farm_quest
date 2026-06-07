import { env } from '../config/env';
import { ApiResult, fail, ok } from './apiResult';

export async function apiGet<T>(path: string, init?: RequestInit): Promise<ApiResult<T>> {
  try {
    const response = await fetch(`${env.apiBaseUrl}${path}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(init?.headers ?? {}),
      },
      ...init,
    });

    if (!response.ok) {
      return fail('HTTP_ERROR', `請求失敗（${response.status}）`);
    }

    const data = (await response.json()) as T;
    return ok(data);
  } catch (error) {
    return fail('NETWORK_ERROR', error instanceof Error ? error.message : '無法連線到伺服器');
  }
}

export async function apiPost<TRequest, TResponse>(
  path: string,
  body: TRequest,
  init?: RequestInit,
): Promise<ApiResult<TResponse>> {
  try {
    const response = await fetch(`${env.apiBaseUrl}${path}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(init?.headers ?? {}),
      },
      body: JSON.stringify(body),
      ...init,
    });

    if (!response.ok) {
      return fail('HTTP_ERROR', `請求失敗（${response.status}）`);
    }

    const data = (await response.json()) as TResponse;
    return ok(data);
  } catch (error) {
    return fail('NETWORK_ERROR', error instanceof Error ? error.message : '無法連線到伺服器');
  }
}
